package machinehead.pushitweb.security.filter

import machinehead.pushitweb.constants.Constants
import machinehead.pushitweb.constants.Constants.Companion.TEST_ROLE
import machinehead.pushitweb.constants.Constants.Companion.TEST_USER
import machinehead.pushitweb.service.JWTokenService
import machinehead.pushitweb.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [JWTRequestFilter::class, JWTokenService::class])
internal class JWTRequestFilterTest {

    @MockBean
    lateinit var filterChain: FilterChain

    @Autowired
    lateinit var jwTokenService: JWTokenService

    @Autowired
    lateinit var jwtRequestFilter: JWTRequestFilter

    @MockBean
    lateinit var httpServletResponse: HttpServletResponse

    @MockBean
    lateinit var httpServletRequest: HttpServletRequest

    @Test
    fun `call controller with valid authentication request and assert security context authenticated`() {

        val token = jwTokenService.generateToken(TEST_USER, TEST_ROLE, 999999999)

        given(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .willReturn(Constants.BEARER + token)

        jwtRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        val userName = (authentication.principal as User).username
        val role = TokenUtil.extractAuthority(authentication)


        assertThat(authentication).isNotNull
        assertThat(role).isEqualTo(TEST_ROLE)
        assertThat(userName).isEqualTo(TEST_USER)
        assertThat(authentication.isAuthenticated).isTrue()

        verify(filterChain, atLeastOnce()).doFilter(httpServletRequest, httpServletResponse)
    }


    @Test
    fun `no auth header assert no security context is set`() {
        given(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .willReturn(null)

        jwtRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain)

        assertThat(SecurityContextHolder.getContext().authentication).isNull()

        verify(filterChain, atLeastOnce()).doFilter(httpServletRequest, httpServletResponse)
    }
}