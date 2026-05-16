package org.fmazmz.bff.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestDiagnosticFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        boolean hasBearer = auth != null && auth.startsWith("Bearer ");
        log.info(
                "BFF request: {} {} | servletPath={} | hasBearer={} | publicAuth={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getServletPath(),
                hasBearer,
                SecurityConfig.isPublicAuthPath(request));

        filterChain.doFilter(request, response);

        log.info("BFF response: {} {} -> {}", request.getMethod(), request.getRequestURI(), response.getStatus());
    }
}
