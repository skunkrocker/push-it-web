package machinehead.pushitweb.security.config

import machinehead.pushitweb.constants.Constants.Companion.TEST_PASSWORD
import machinehead.pushitweb.constants.Constants.Companion.TEST_ROLE
import machinehead.pushitweb.constants.Constants.Companion.TEST_USER
import machinehead.pushitweb.entity.PushUser
import machinehead.pushitweb.repository.PushUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

@SpringBootTest(classes = [UserAuthenticationManager::class])
class UserAuthenticationManagerTest {
    @MockBean
    lateinit var pushUserRepository: PushUserRepository

    @Autowired
    lateinit var userAuthenticationManager: UserAuthenticationManager

    @Test
    fun `user is valid therefore authentication object is returned and user is authenticated`() {
        given(pushUserRepository.findByUserName(TEST_USER))
                .willReturn(PushUser(TEST_ROLE, TEST_USER, TEST_PASSWORD, 1))

        val authenticate = userAuthenticationManager.authenticate(UsernamePasswordAuthenticationToken(TEST_USER, TEST_PASSWORD, emptyList()))

        assertThat(authenticate).isNotNull
        assertThat(authenticate?.isAuthenticated).isTrue()
    }

    @Test
    fun `user is invalid therefore no authentication object is returned and user is not authenticated`() {

        given(pushUserRepository.findByUserName(TEST_USER))
                .willReturn(null)

        val authenticate = userAuthenticationManager.authenticate(UsernamePasswordAuthenticationToken(TEST_USER, TEST_PASSWORD, emptyList()))

        assertThat(authenticate).isNull()
    }
}