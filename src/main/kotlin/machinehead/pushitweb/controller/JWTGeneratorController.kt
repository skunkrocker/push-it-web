package machinehead.pushitweb.controller

import machinehead.pushitweb.constants.Constants
import machinehead.pushitweb.constants.Constants.Companion.AUTH_MANAGER_QUALIFIER
import machinehead.pushitweb.model.api.JWTApiRequest
import machinehead.pushitweb.model.api.JWTApiResponse
import machinehead.pushitweb.service.JWTokenService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Controller
class LoginController(private val authenticationManager: AuthenticationManager, private val jwTokenService: JWTokenService) {


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
                    .orElse(Constants.TEST_ROLE)

            val token = jwTokenService.generateToken(authUser.principal.toString(), authority, 999999999)

            return ResponseEntity.ok(JWTApiResponse(token!!))
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }
}

