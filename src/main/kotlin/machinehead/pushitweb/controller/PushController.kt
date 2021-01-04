package machinehead.pushitweb.controller

import machinehead.pushitweb.model.Payload
import machinehead.pushitweb.model.PushResultApi
import machinehead.pushitweb.service.ApplePushNotificationService
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

import reactor.core.publisher.Flux

@Controller
open class PushController(private val applePushNotificationService: ApplePushNotificationService) {

    @PostMapping(value = ["/push"])
    fun pushPayload(@RequestBody payload: Payload): Flux<ServerSentEvent<PushResultApi>> {

        return applePushNotificationService.push(payload)
    }
}