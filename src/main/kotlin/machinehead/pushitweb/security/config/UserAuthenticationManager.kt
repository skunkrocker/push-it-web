package machinehead.pushitweb.security.config

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

open class UserAuthenticationManager : AuthenticationManager {
    companion object {
        const val TEST_USER = "test-user"
        const val TEST_ROLE = "TEST_ADMIN"
        const val TEST_PASSWORD = "test-password"
    }

    override fun authenticate(authentication: Authentication?): Authentication? {
        return authentication?.let {
            val isTestUser = TEST_USER == authentication.principal && TEST_PASSWORD == authentication.credentials
            if (isTestUser) {
                return@let UsernamePasswordAuthenticationToken(TEST_USER, TEST_PASSWORD, listOf(GrantedAuthority { TEST_ROLE }));
            }
            return@let null
        }
    }
}
