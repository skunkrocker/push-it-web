package machinehead.pushitweb.security.util

import io.jsonwebtoken.*
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import java.security.SignatureException

object JWTTokenUtil {
    private const val ROLE = "role"
    private const val BEARER = "Bearer "
    private const val BEARER_INDEX = 7
    private const val EMPTY_CREDENTIALS = ""

    private val LOGGER = LoggerFactory.getLogger(JWTTokenUtil::class.java)

    fun getAuthentication(jwtToken: String, jwtSecret: String?): UsernamePasswordAuthenticationToken {

        val jwtTokenWithoutBearer = removeBearerPrefix(jwtToken)

        val claimsJws: Jws<Claims> = Jwts
                .parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(jwtTokenWithoutBearer)

        val claims: Claims = claimsJws.body
        val userName: String = claims.subject

        return when (claims[ROLE]) {
            null -> {
                val userDetails: UserDetails = User(userName, EMPTY_CREDENTIALS, emptyList())
                UsernamePasswordAuthenticationToken(userDetails, EMPTY_CREDENTIALS, emptyList())
            }
            else -> {
                val simpleGrantedAuthority = listOf(SimpleGrantedAuthority(claims[ROLE].toString()))
                val userDetails: UserDetails = User(userName, EMPTY_CREDENTIALS, simpleGrantedAuthority)
                UsernamePasswordAuthenticationToken(userDetails, EMPTY_CREDENTIALS, simpleGrantedAuthority)
            }
        }
    }

    fun isValid(inJWTToken: String, inJWTSecret: String?): Boolean {
        try {
            val token = removeBearerPrefix(inJWTToken)
            Jwts
                    .parser()
                    .setSigningKey(inJWTSecret)
                    .parseClaimsJws(token)
            return true
        } catch (ex: SignatureException) {
            LOGGER.error("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            LOGGER.error("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            LOGGER.error("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            LOGGER.error("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            LOGGER.error("JWT claims string is empty.")
        }
        return false
    }

    private fun removeBearerPrefix(jwtToken: String): String {
        return jwtToken
                .takeIf { it.startsWith(BEARER) }
                ?.substring(BEARER_INDEX)
                ?: jwtToken
    }
}
