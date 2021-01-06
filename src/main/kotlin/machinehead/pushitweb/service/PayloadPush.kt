package machinehead.pushitweb.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import machinehead.pushitweb.gson
import machinehead.pushitweb.logger
import machinehead.pushitweb.model.APNSResponseApi
import machinehead.pushitweb.model.Payload
import machinehead.pushitweb.model.PlatformResponseApi
import machinehead.pushitweb.model.PushResultApi
import machinehead.pushitweb.repository.ApplicationRepository
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.concurrent.CountDownLatch
import javax.xml.bind.JAXBElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.suspendCoroutine

interface ApplePushNotificationService {
    fun push(payload: Payload): Flux<ServerSentEvent<PushResultApi>>
}

interface ValidatePayloadService {
    fun validate(payload: Payload)
}

@Service
open class ApplePushNotificationImpl(
    private val requestService: RequestService,
    private val httpClientService: HttpClientService,
    private val validatePayloadService: ValidatePayloadService
) :
    ApplePushNotificationService {

    private val logger by logger()

    override fun push(payload: Payload): Flux<ServerSentEvent<PushResultApi>> {

        validatePayloadService.validate(payload)

        logger.debug("Create the flux sink")
        return Flux.create<PushResultApi> { consumer ->
            //run non blocking
            GlobalScope.launch {

                logger.debug("Flux sink was created, create the http client.")
                httpClientService.httpClient(payload.appName, payload.headers) { httpClient ->

                    logger.debug("The httpClient was created., prepare and push.")
                    prepareAndPushPayload(payload, consumer, httpClient)
                }
            }
        }.map { pushResultApi: PushResultApi? ->
            ServerSentEvent.builder<PushResultApi>().data(pushResultApi).build()
        }
    }

    private fun prepareAndPushPayload(
        payload: Payload,
        consumer: FluxSink<PushResultApi?>,
        httpClient: OkHttpClient
    ) {

        val countDownLatch = CountDownLatch(payload.tokens.size)

        requestService.requestBodyFor(payload) { requestBody ->
            payload.tokens.forEach { token ->

                requestService.create(token, requestBody, payload.stage) { request ->

                    val responseCallback = PlatformCallback(token, countDownLatch, consumer = consumer)
                    //let ok http client handle request threads
                    httpClient.newCall(request).enqueue(responseCallback)
                }
            }
        }
        countDownLatch.await()
        //dispose of consumer when all are done
        consumer.complete()
        //release http client resources when consumer done
        httpClientService.releaseResources(httpClient)
    }
}

@Component
open class ValidatePayloadImpl(private val applicationRepository: ApplicationRepository) : ValidatePayloadService {

    override fun validate(payload: Payload) {

        if (payload.tokens.isEmpty()) {
            throw IllegalArgumentException("Invalid payload, at least one token is expected.")
        }

        if (payload.notification?.aps?.alert == null) {
            throw IllegalArgumentException("Invalid payload, notification -> aps -> alert dictionary must be present.")
        }

        if (payload.headers.isEmpty()) {
            throw IllegalArgumentException("Invalid payload, apns topic must be set. This is usually the bundle name of your app.")
        }

        applicationRepository.findByAppName(payload.appName)
            ?: throw IllegalArgumentException("Invalid payload, the App with the name: ${payload.appName} is not registered.")

    }
}

class PlatformCallback(
    private val token: String,
    private val countDownLatch: CountDownLatch,
    private val consumer: FluxSink<PushResultApi?>
) : Callback {

    private val logger by logger()

    override fun onFailure(call: Call, e: IOException) {
        logger.error("Error occurred pushing the message for token: $token", e)

        val errorApnsResponse = getErrorApnsResponse(e)

        logger.debug("Notify the consumer about the failed push.")
        consumer.next(PushResultApi(token, errorApnsResponse))

        countDownLatch.countDown()
        logger.debug("count down latch called")
    }

    override fun onResponse(call: Call, response: Response) {
        logger.debug("response received for $token")
        try {
            val platformResponse = getPlatformResponse(response)

            logger.debug("the push response: $platformResponse for token: $token received")

            logger.debug("Push result api sent to the consumer.")
            consumer.next(PushResultApi(token, platformResponse))

        } catch (e: IOException) {
            logger.error("could not execute request for token $token . error was: $e")
        } finally {
            response.close()
        }
        countDownLatch.countDown()
        logger.debug("count down latch called")
    }

    private fun getPlatformResponse(response: Response) =
        PlatformResponseApi(response.code, getAPNSResponse(response))

    private fun getAPNSResponse(response: Response): APNSResponseApi {
        val body = response.body?.string().orEmpty()

        if (body.isNotEmpty()) {
            return gson.fromJson(body, APNSResponseApi::class.java)
        }
        return gson.fromJson("{\"reason\":\"Success\"}", APNSResponseApi::class.java)
    }

    private fun getErrorApnsResponse(e: Exception): PlatformResponseApi {
        val fromJson =
            gson.fromJson("{\"reason\":\"Request Failure. Message: ${e.message}\"}", APNSResponseApi::class.java)
        return PlatformResponseApi(500, fromJson)
    }
}
