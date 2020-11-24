package machinehead.pushitweb.security.config

import machinehead.pushitweb.repositories.PushUserRepository
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

open class UserAuthenticationManager(private val pushUserRepository: PushUserRepository) : AuthenticationManager {

    override fun authenticate(authentication: Authentication?): Authentication? {

        return authentication?.principal.let { userName ->
            val findByUserName = pushUserRepository.findByUserName(userName.toString())

            return@let findByUserName?.let { user ->
                return UsernamePasswordAuthenticationToken(user.userName, user.password, listOf(GrantedAuthority { user.role }));
            }
        }
    }
}
