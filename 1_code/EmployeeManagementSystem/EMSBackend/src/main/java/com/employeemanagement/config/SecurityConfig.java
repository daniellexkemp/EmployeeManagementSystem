
package com.employeemanagement.config;

import com.employeemanagement.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new ChangeSessionIdAuthenticationStrategy();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            UserRepository userRepository)
            throws Exception {

        CsrfTokenRequestAttributeHandler csrfHandler =
                new CsrfTokenRequestAttributeHandler();

        http
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository)
            )
            .csrf(csrf -> csrf
                .csrfTokenRequestHandler(csrfHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/api/auth/login",
                    "/api/auth/csrf"
                ).permitAll()
                .requestMatchers("/api/auth/register")
                    .hasAnyAuthority("HR_ADMIN", "SYS_ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(
                    (request, response, authException) -> {
                        response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Authentication required"
                        );
                    }
                )
                .accessDeniedHandler(
                    (request, response, accessDeniedException) -> {
                        response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "Access denied"
                        );
                    }
                )
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .addFilterBefore(
                new CurrentRoleFilter(userRepository),
                AuthorizationFilter.class
            );

        return http.build();
    }
}
