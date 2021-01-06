package machinehead.pushitweb.service

import machinehead.pushitweb.logger
import machinehead.pushitweb.repository.ApplicationRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.*

/**
 * Creates a [OkHttpClient] instance with preconfigured certificate associated with the app name.
 */
interface HttpClientService {
    /**
     * Creates a [OkHttpClient] instance for the application name.
     *
     * @param appName the application name to which the payload will be pushed.
     * @param headers Header map of headers to be inserted before the request is executed.
     * @param onCreated function called upon successful http client creation.
     */
    fun httpClient(appName: String, headers: Map<String, Any>, onCreated: (httpClient: OkHttpClient) -> Unit)

    /**
     * Releases the resources the [OkHttpClient] instance is holding.
     * @param httpClient The instance of [OkHttpClient] whose releases
     */
    fun releaseResources(httpClient: OkHttpClient?)
}

/**
 * Service to create the [Interceptor.Chain] for the [OkHttpClient] where the headers are set before request.
 */
interface InterceptorChainService {
    /**
     * @param headers Header map of headers to be inserted before the request is executed.
     * @return The [Interceptor.Chain] interface implementation where the headers are added.
     */
    fun createInterceptor(headers: Map<String, Any>): (Interceptor.Chain) -> Response
}

/**
 * Creates the [SSLSocketFactory] and [X509TrustManager] for an application name.
 * This are needed to create the http client that will authenticate on apple servers.
 */
interface CredentialsService {
    /**
     * Creates the [SSLSocketFactory] and [X509TrustManager] for an application name.
     * @param appName the application name to which the payload will be pushed.
     * @param onCreated function called upon successful [SSLSocketFactory] and [X509TrustManager] creation.
     */
    fun factoryAndTrustManager(
        appName: String,
        onCreated: (factory: SSLSocketFactory, manager: X509TrustManager) -> Unit
    )
}

@Service
open class HttpClientServiceImpl(
    private val credentialsService: CredentialsService,
    private val interceptorService: InterceptorChainService
) : HttpClientService {

    private val logger by logger()

    override fun httpClient(appName: String, headers: Map<String, Any>, onCreated: (httpClient: OkHttpClient) -> Unit) {

        credentialsService.factoryAndTrustManager(appName) { factory: SSLSocketFactory, manager: X509TrustManager ->
            logger.debug("The factory and manager were creaded")
            createOkClient(headers, factory, manager) {
                logger.debug("The ok client instance was created")
                onCreated(it)
            }
        }
    }

    private fun createOkClient(
        headers: Map<String, Any>,
        factory: SSLSocketFactory,
        manager: X509TrustManager,
        onCreated: (httpClient: OkHttpClient) -> Unit
    ) {
        try {
            val okClientBuilder = OkHttpClient().newBuilder()
            logger.debug("ok client builder created")

            okClientBuilder.sslSocketFactory(factory, manager)
            logger.debug("added ssl factory and trust manager to the ok client builder")

            okClientBuilder.addInterceptor(interceptorService.createInterceptor(headers))
            logger.debug("ok client builder added request interceptor for headers")

            val okClient = okClientBuilder.build()
            logger.debug("ok client was built and will be returned")

            onCreated(okClient)
        } catch (e: Exception) {
            logger.error("Could not create ok client. exception occurred: $e")
            throw IllegalStateException("Could not create http client")
        }
    }

    override fun releaseResources(httpClient: OkHttpClient?) {
        val dispatcher = httpClient?.dispatcher
        if (dispatcher?.queuedCallsCount() == 0 && dispatcher.runningCallsCount() == 0) {
            httpClient.dispatcher.executorService.shutdownNow()
            logger.debug("ok client executor service shutting down")
            httpClient.connectionPool.evictAll()
            logger.debug("ok client evict all from connection pool")
            httpClient.cache?.close()
            logger.debug("ok client clean cache")
        }
    }
}

@Service
open class InterceptorChainServiceImpl : InterceptorChainService {

    override fun createInterceptor(headers: Map<String, Any>): (Interceptor.Chain) -> Response {
        return { chain: Interceptor.Chain ->
            val original: Request = chain.request()
            val responseBuilder: Request.Builder = original.newBuilder()

            headers.forEach() {
                responseBuilder.addHeader(it.key, it.value.toString())
            }

            val request = responseBuilder
                .method(original.method, original.body)
                .build()

            chain.proceed(request)
        }
    }
}

@Service
open class CredentialsServiceImpl(private val applicationRepository: ApplicationRepository) : CredentialsService {

    private val logger by logger()

    override fun factoryAndTrustManager(
        appName: String,
        onCreated: (factory: SSLSocketFactory, manager: X509TrustManager) -> Unit
    ) {
        val application = applicationRepository.findByAppName(appName)
            ?: throw IllegalStateException("No application with the name: $appName is registered.")

        sslFactoryWithTrustManager(application.certificate, application.certPass) { factory, manager ->
            onCreated(factory, manager)
        }
    }


    private fun sslFactoryWithTrustManager(
        certificate: String,
        password: String,
        onCreated: (factory: SSLSocketFactory, manager: X509TrustManager) -> Unit
    ) {
        try {
            val keyStore = KeyStore.getInstance("PKCS12")

            val decodedCertificate = Base64
                .getDecoder()
                .decode(certificate)
                .inputStream()

            keyStore.load(decodedCertificate, password.toCharArray())

            val keyFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
            )

            keyFactory.init(keyStore, password.toCharArray())

            val keyManagers = keyFactory.keyManagers
            val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            )

            val trustManager: X509TrustManager = getX509TrustManager(keyStore, trustManagerFactory)

            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(keyManagers, null, SecureRandom())
            val socketFactory = sslContext.socketFactory
            onCreated(socketFactory, trustManager)

        } catch (e: Exception) {
            logger.error("Could not create credentials.", e)
            throw IllegalStateException("Could not create credentials.")
        }
    }

    @Throws(KeyStoreException::class)
    private fun getX509TrustManager(keyStore: KeyStore, trustManagerFactory: TrustManagerFactory): X509TrustManager {

        trustManagerFactory.init(keyStore)

        val trustManagers = trustManagerFactory.trustManagers

        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            ("Unexpected default trust managers:" + Arrays.toString(trustManagers))
        }
        return trustManagers[0] as X509TrustManager
    }
}
