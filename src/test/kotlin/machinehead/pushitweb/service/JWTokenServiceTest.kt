package machinehead.pushitweb.service

import machinehead.pushitweb.constants.Constants.Companion.TEST_ROLE
import machinehead.pushitweb.constants.Constants.Companion.TEST_USER
import machinehead.pushitweb.constants.Constants.Companion.UNKNOWN_ROLE
import machinehead.pushitweb.util.TokenUtil.Companion.dateDaysAgo
import machinehead.pushitweb.util.TokenUtil.Companion.generateTestToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.User
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [JWTokenService::class])
open class JWTokenServiceTest {

    @Value("\${jwtSecret}")
    lateinit var jwtSecret: String

    @Autowired
    lateinit var jwTokenService: JWTokenService

    @Test
    fun getAuthentication_AuthorizationIsValid() {
        val testToken = generateTestToken(jwtSecret = jwtSecret)
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
    fun getAuthentication_NoRole_AuthorizationIsValid_UnknownRole() {
        val testToken = generateTestToken(role = null, jwtSecret = jwtSecret)
        val authentication = jwTokenService.getAuthentication(testToken)

        val authority = authentication.authorities
                .stream()
                .findAny()
                .map { it.authority }
                .orElse(null)

        val user = authentication.principal as User

        assertThat(authority).isEqualTo(UNKNOWN_ROLE)
        assertThat(user.username).isEqualTo(TEST_USER)
    }

    @Test
    fun isNotExpired_ItIsNotExpired() {
        val testToken = generateTestToken(jwtSecret = jwtSecret)

        val isValid = jwTokenService.isNotExpired(testToken)
        assertThat(isValid).isTrue()
    }

    @Test
    fun isNotExpired_ItIsExpired() {
        val testToken = generateTestToken(now = dateDaysAgo(10), expired = 0, jwtSecret = jwtSecret)

        val isValid = jwTokenService.isNotExpired(testToken)
        assertThat(isValid).isFalse()
    }
}