package machinehead.pushitweb.security.config

import machinehead.pushitweb.security.filter.JWTRequestFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
open class WebSecurityConfig(@Value("\${jwtSecret}") private val jwtSecret: String) : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(inHttp: HttpSecurity) {
        inHttp
                .addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter::class.java)
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().mvcMatchers(*AUTH_WHITELIST)
    }

    companion object {
        private val AUTH_WHITELIST = arrayOf(
                "/webjars/**",
                "/v2/api-docs",
                "/admin/check",
                "/actuator/**",
                "/swagger-ui.html",
                "/swagger-resources/**"
        )
    }

    @Bean
    open fun jwtRequestFilter(): JWTRequestFilter {
        return JWTRequestFilter(jwtSecret)
    }
}
