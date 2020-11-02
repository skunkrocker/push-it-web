package machinehead.pushitweb.controller

import machinehead.pushitweb.constants.Constants
import machinehead.pushitweb.model.api.JWTApiRequest
import machinehead.pushitweb.model.api.JWTApiResponse
import machinehead.pushitweb.service.JWTokenService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
open class JWTController(private val jwTokenService: JWTokenService, private val authenticationManager: AuthenticationManager) {


    @PostMapping(value = ["/auth"])
    fun createAuthenticationToken(@RequestBody loginRequest: JWTApiRequest): ResponseEntity<JWTApiResponse?>? {
        if (loginRequest.userName.isEmpty() || loginRequest.password.isBlank()) {
            return ResponseEntity.badRequest().build()
        }

        val authUser = authenticationManager
                .authenticate(UsernamePasswordAuthenticationToken(loginRequest.userName, loginRequest.password))

        if (authUser.isAuthenticated) {
            val authority = authUser.authorities
                    .stream()
                    .findAny()
                    .map { it.authority }
                    .orElse(Constants.UNKNOWN_ROLE)

            val token = jwTokenService.generateToken(authUser.principal.toString(), authority, 999999999)

            return ResponseEntity.ok(JWTApiResponse(token!!))
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }
}