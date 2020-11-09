package machinehead.pushitweb.security.config

import machinehead.pushitweb.repositories.PushUserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

open class UserAuthenticationManager(private val pushUserRepository: PushUserRepository) : AuthenticationManager {
    companion object {
        const val TEST_USER = "test-user"
        const val TEST_ROLE = "TEST_ADMIN"
        const val TEST_PASSWORD = "test-password"
    }

    override fun authenticate(authentication: Authentication?): Authentication? {

        return authentication?.principal.let { userName ->
            val findByUserName = pushUserRepository.findByUserName(userName.toString())
            if (TEST_PASSWORD == findByUserName?.password) {
                return@let UsernamePasswordAuthenticationToken(TEST_USER, TEST_PASSWORD, listOf(GrantedAuthority { TEST_ROLE }));
            }
            return@let null
        }
    }
}
