package machinehead.pushitweb.service

import machinehead.pushitweb.isNotNullOrEmpty
import machinehead.pushitweb.logger
import machinehead.pushitweb.model.*
import machinehead.pushitweb.service.AppleNotificationServers.Companion.urlForStage
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Service


/**
 * Service to create the [Request] for the [OkHttpClient]
 */
interface RequestService {
    /**
     * Creates the [Request] for the [OkHttpClient] for the specific token and the message body.
     * @param token The token to which the message is to be sent.
     * @param notificationBody The notification body for the request.
     * @param stage The apple APNS stage to which the message should be pushed. The token should be valid for the stage.
     * @param onCreated The call back when the [Request] was created.
     */
    fun create(token: String, notificationBody: RequestBody, stage: Stage, onCreated: (Request) -> Unit)

    /**
     * The [RequestBody] for the [Notification] that will be put in each [Request] for pushing the notification to a device token.
     * @param payload The [Payload]
     * @param onCreated The call back when the [RequestBody] was created.
     */
    fun requestBodyFor(payload: Payload, onCreated: (RequestBody) -> Unit)
}

@Service
open class RequestServiceImpl : RequestService {

    private val logger by logger()

    companion object {
        const val TEST_URL_PROPERTY = "localhost.url"
    }

    override fun create(token: String, notificationBody: RequestBody, stage: Stage, onCreated: (Request) -> Unit) {

        val url = getUrl(stage) + token
        logger.debug("final push url is: $url")

        val request = Request.Builder()
            .url(url)
            .post(notificationBody)
            .build()

        onCreated(request)
    }

    override fun requestBodyFor(payload: Payload, onCreated: (RequestBody) -> Unit) {

        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()

        val body: RequestBody = payload
            .notificationAsString()
            .toRequestBody(mediaType)
        logger.info("body created")

        onCreated(body)
    }


    private fun getUrl(stage: Stage): String {
        var url = urlForStage(stage)

        if (System.getProperty(TEST_URL_PROPERTY).isNotNullOrEmpty()) {
            url = urlForStage(Stage.TEST)
            logger.warn("you overwrite the APNS  url to: $url ")
            logger.warn("if you didn't do this for test purposes, please remove the property 'localhost.url' from your ENV")
        }
        return url
    }
}
