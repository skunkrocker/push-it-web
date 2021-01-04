package machinehead.pushitweb.controller

import machinehead.pushitweb.constants.Constants
import machinehead.pushitweb.logger
import machinehead.pushitweb.model.JWTApiRequest
import machinehead.pushitweb.model.JWTApiResponse
import machinehead.pushitweb.service.JWTokenService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
open class JWTController(
    private val jwTokenService: JWTokenService,
    private val authenticationManager: AuthenticationManager
) {

    private val logger by logger()

    @PostMapping(value = ["/auth"])
    fun createAuthenticationToken(@RequestBody loginRequest: JWTApiRequest): ResponseEntity<JWTApiResponse?>? {
        if (loginRequest.userName.isEmpty() || loginRequest.password.isBlank()) {
            logger.debug(
                "The user name: {} or password: {} were blank",
                loginRequest.userName.isEmpty(),
                loginRequest.password.isBlank()
            )
            return ResponseEntity.badRequest().build()
        }

        val authUser: Authentication? = authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(loginRequest.userName, loginRequest.password))

        authUser?.let {
            if (it.isAuthenticated) {
                logger.debug("Generate token for the user with the name: {} is authenticated.", loginRequest.userName)
                val authority = extractAuthority(it)

                val token = jwTokenService.generateToken(it.principal.toString(), authority, 999999999)

                return ResponseEntity.ok(JWTApiResponse(accessToken = token!!))
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

    private fun extractAuthority(authUser: Authentication): String? {
        return authUser.authorities
            .stream()
            .findAny()
            .map { it.authority }
            .orElse(Constants.UNKNOWN_ROLE)
    }
}