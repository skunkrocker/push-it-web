package machinehead.pushitweb.util

import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import machinehead.pushitweb.constants.Constants
import org.springframework.security.core.Authentication
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter
import kotlin.collections.HashMap

class TokenUtil {
    companion object {
        fun generateTestToken(role: String? = Constants.TEST_ROLE, now: Date = Date(), expired: Int = 999999999, jwtSecret: String = "bigSecret"): String {

            val apiKeySecretBytes: ByteArray = DatatypeConverter.parseBase64Binary(jwtSecret)
            val signingKey = SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.jcaName)
            val expiryDate = Date(now.time + expired)

            val map = HashMap<String, Any>()
            map[Header.TYPE] = Header.JWT_TYPE

            return Jwts.builder()
                    .setHeader(map)
                    .setId(UUID.randomUUID().toString())
                    .claim("role", role)
                    .setSubject(Constants.TEST_USER)
                    .setIssuedAt(now)
                    .setIssuer(Constants.TEST_APP)
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS512, signingKey)
                    .compact()
        }

        fun dateDaysAgo(daysAgo: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)

            return calendar.time
        }

        fun extractAuthority(authUser: Authentication): String? {
            return authUser.authorities
                    .stream()
                    .findAny()
                    .map { it.authority }
                    .orElse(Constants.UNKNOWN_ROLE)
        }
    }
}