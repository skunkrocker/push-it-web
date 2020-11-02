package machinehead.pushitweb.service

import io.jsonwebtoken.Header
import io.jsonwebtoken.Header.JWT_TYPE
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm.HS256
import io.jsonwebtoken.SignatureAlgorithm.HS512
import machinehead.pushitweb.constants.Constants.Companion.TEST_APP
import machinehead.pushitweb.constants.Constants.Companion.TEST_ROLE
import machinehead.pushitweb.constants.Constants.Companion.TEST_USER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.User
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter
import kotlin.collections.HashMap

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [JWTokenService::class])
open class JWTokenServiceTest {

    @Value("\${jwtSecret}")
    lateinit var jwtSecret: String

    @Autowired
    lateinit var jwTokenService: JWTokenService

    @Test
    fun jwtValidated_Authorization_IsValid() {
        val testToken = generateTestToken()
        val authentication = jwTokenService.getAuthentication(testToken)

        val authority = authentication.authorities
                .stream()
                .findAny()
                .map { it.authority }
                .orElse(null)

        val user = authentication.principal as User

        assertThat(authority).isEqualTo(TEST_ROLE)
        assertThat(user.username).isEqualTo(TEST_USER)
    }


    @Test
    fun jwtValidated_ItIsValid() {
        val testToken = generateTestToken()

        val isValid = jwTokenService.isValid(testToken)
        assertThat(isValid).isTrue()
    }


    @Test
    fun jwtValidated_ItIsNotValid() {
        val testToken = generateTestToken(now = dateDaysAgo(10), expired = 0)

        val isValid = jwTokenService.isValid(testToken)
        assertThat(isValid).isFalse()
    }

    private fun generateTestToken(now: Date = Date(), expired: Int = 999999999): String {

        val apiKeySecretBytes: ByteArray = DatatypeConverter.parseBase64Binary(jwtSecret)
        val signingKey = SecretKeySpec(apiKeySecretBytes, HS256.jcaName)
        val expiryDate = Date(now.time + expired)

        val map = HashMap<String, Any>()
        map[Header.TYPE] = JWT_TYPE

        return Jwts.builder()
                .setHeader(map)
                .setId(UUID.randomUUID().toString())
                .claim("role", TEST_ROLE)
                .setSubject(TEST_USER)
                .setIssuedAt(now)
                .setIssuer(TEST_APP)
                .setExpiration(expiryDate)
                .signWith(HS512, signingKey)
                .compact()
    }

    private fun dateDaysAgo(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)

        return calendar.time
    }
}