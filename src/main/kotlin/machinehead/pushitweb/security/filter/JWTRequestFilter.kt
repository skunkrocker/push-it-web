package machinehead.pushitweb.security.filter

import machinehead.pushitweb.security.util.JWTTokenUtil
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value

/**
 * Filter for the authorizing request with JWT-Token
 */
@Component
class JWTRequestFilter(@Value("\${jwtSecret}") private val jwtSecret: String) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, filterChain: FilterChain) {

        val header: String? = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)

        header
                ?.let { jwt: String? ->
                    if (JWTTokenUtil.isValid(jwt!!, jwtSecret)) {
                        SecurityContextHolder.getContext().authentication = JWTTokenUtil.getAuthentication(jwt, jwtSecret)
                        LOGGER.debug("The authentication for JWT-Token was successful: {} ", jwt)
                    }
                } ?: LOGGER.error("There is no JWT-Token in the Authorization header.")

        filterChain.doFilter(httpServletRequest, httpServletResponse)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(JWTRequestFilter::class.java)
    }
}