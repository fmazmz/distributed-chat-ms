package org.fmazmz.bff.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** No JWT / OAuth2 resource-server filters on this chain. */
    @Bean
    @Order(1)
    SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(
                        "/",
                        "/index.html",
                        "/app.js",
                        "/styles.css",
                        "/favicon.ico",
                        "/api/v1/public/**",
                        "/api/v1/auth/register/**",
                        "/api/v1/auth/login/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        log.info("BFF security: public filter chain (no JWT) for /api/v1/auth/register/** and /api/v1/auth/login/**");
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain protectedSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        log.info("BFF security: protected filter chain (JWT) for all other routes");
        return http.build();
    }

    static boolean isPublicAuthPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }
        return path.startsWith("/api/v1/auth/register/") || path.startsWith("/api/v1/auth/login/");
    }
}
