package machinehead.pushitweb.service

import io.jsonwebtoken.*
import io.jsonwebtoken.Header.JWT_TYPE
import io.jsonwebtoken.SignatureAlgorithm.HS256
import io.jsonwebtoken.SignatureAlgorithm.HS512
import machinehead.pushitweb.constants.Constants.Companion.BEARER
import machinehead.pushitweb.constants.Constants.Companion.BEARER_INDEX
import machinehead.pushitweb.constants.Constants.Companion.EMPTY_CREDENTIALS
import machinehead.pushitweb.constants.Constants.Companion.ROLE
import machinehead.pushitweb.constants.Constants.Companion.UNKNOWN_ROLE
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.SignatureException
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter
import kotlin.collections.HashMap

@Service
open class JWTokenService(@Value("\${jwtSecret}") var jwtSecret: String) {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(JWTokenService::class.java)
    }

    fun getAuthentication(jwtToken: String): UsernamePasswordAuthenticationToken {

        val jwtTokenWithoutBearer = removeBearerPrefix(jwtToken)

        val claimsJws: Jws<Claims> = Jwts
                .parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(jwtTokenWithoutBearer)

        val claims: Claims = claimsJws.body
        val userName: String = claims.subject

        val grantedAuthority = when (claims[ROLE]) {
            null -> {
                listOf(SimpleGrantedAuthority(UNKNOWN_ROLE))
            }
            else -> {
                listOf(SimpleGrantedAuthority(claims[ROLE].toString()))
            }
        }

        val userDetails: UserDetails = User(userName, EMPTY_CREDENTIALS, grantedAuthority)
        return UsernamePasswordAuthenticationToken(userDetails, EMPTY_CREDENTIALS, grantedAuthority)
    }

    fun generateToken(username: String?, role: String?, expiration: Int): String? {

        val now = Date(System.currentTimeMillis())

        val apiKeySecretBytes: ByteArray = DatatypeConverter.parseBase64Binary(jwtSecret)
        val signingKey = SecretKeySpec(apiKeySecretBytes, HS256.jcaName)
        val expiryDate = Date(now.time + expiration)

        val map = HashMap<String, Any>()
        map[Header.TYPE] = JWT_TYPE

        return Jwts.builder()
                .setHeader(map)
                .setId(UUID.randomUUID().toString())
                .claim("role", role)
                .setSubject(username)
                .setIssuedAt(now)
                .setIssuer("test-app")
                .setExpiration(expiryDate)
                .signWith(HS512, signingKey)
                .compact()
    }

    fun isNotExpired(jwtToken: String): Boolean {
        try {
            val token = removeBearerPrefix(jwtToken)
            val claims = Jwts
                    .parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)

            return !claims
                    .body
                    .expiration
                    .before(Date(System.currentTimeMillis()))
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