package machinehead.pushitweb.service

import machinehead.pushitweb.logger
import machinehead.pushitweb.repositories.ApplicationRepository
import okhttp3.OkHttpClient
import org.hibernate.exception.JDBCConnectionException
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
     * @param onCreated function called upon successful http client creation.
     */
    fun httpClient(appName: String, onCreated: (httpClient: OkHttpClient) -> Unit)
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
open class HttpClientServiceImpl(private val credentialsService: CredentialsService) : HttpClientService {

    override fun httpClient(appName: String, onCreated: (httpClient: OkHttpClient) -> Unit) {

        credentialsService.factoryAndTrustManager(appName) { factory: SSLSocketFactory, manager: X509TrustManager ->

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
