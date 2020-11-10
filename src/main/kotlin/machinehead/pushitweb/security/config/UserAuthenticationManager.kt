package machinehead.pushitweb.security.config

import machinehead.pushitweb.constants.Constants.Companion.TEST_PASSWORD
import machinehead.pushitweb.constants.Constants.Companion.TEST_ROLE
import machinehead.pushitweb.constants.Constants.Companion.TEST_USER
import machinehead.pushitweb.repositories.PushUserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

open class UserAuthenticationManager(private val pushUserRepository: PushUserRepository) : AuthenticationManager {


    override fun authenticate(authentication: Authentication?): Authentication? {

        return authentication?.principal.let { userName ->
            val findByUserName = pushUserRepository.findByUserName(userName.toString())
            //TODO password validation needed
            if (TEST_PASSWORD == findByUserName?.password) {
                return@let UsernamePasswordAuthenticationToken(TEST_USER, TEST_PASSWORD, listOf(GrantedAuthority { TEST_ROLE }));
            }
            return@let null
        }
    }
}
