package machinehead.pushitweb.security.config

import machinehead.pushitweb.repositories.PushUserRepository
import machinehead.pushitweb.security.filter.JWTRequestFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
open class WebSecurityConfig(private val jwtRequestFilter: JWTRequestFilter, private val pushUserRepository: PushUserRepository) : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers(
                "/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/**",
                "/swagger-ui.html",
                "/h2",
                "/webjars/**",
                "/auth"
        );
    }


    @Bean
    @Throws(java.lang.Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager? {
        return UserAuthenticationManager(pushUserRepository)
    }
}
