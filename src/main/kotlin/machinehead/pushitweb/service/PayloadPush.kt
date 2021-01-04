package machinehead.pushitweb.service

import machinehead.pushitweb.model.Payload
import machinehead.pushitweb.model.PushResultApi
import machinehead.pushitweb.repositories.ApplicationRepository
import machinehead.pushitweb.service.AppleNotificationServers.Companion.urlForStage
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.lang.IllegalArgumentException

interface ApplePushNotificationService {
    fun push(payload: Payload): Flux<ServerSentEvent<PushResultApi>>
}

interface ValidatePayloadService {
    fun validate(payload: Payload)
}

@Service
open class ApplePushNotificationImpl(private val validatePayloadService: ValidatePayloadService) :
    ApplePushNotificationService {

    override fun push(payload: Payload): Flux<ServerSentEvent<PushResultApi>> {

        validatePayloadService.validate(payload)

        val urlForStage = urlForStage(payload.stage)

        return Flux.create<PushResultApi> {
            prepareAndPushPayload(payload, it)
        }.map { pushResultApi: PushResultApi? ->
            ServerSentEvent.builder<PushResultApi>().data(pushResultApi).build()
        }
    }

    private fun prepareAndPushPayload(payload: Payload, consumer: FluxSink<PushResultApi?>) {


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