package org.fmazmz.usermanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;

/**
 * gRPC is cluster-internal only (port {@code spring.grpc.server.port}, not exposed via BFF).
 * Callers such as message-manager validate the end-user JWT themselves; they do not forward
 * the user bearer on gRPC. Trust boundary = private network / K8s service mesh.
 * User-facing APIs stay on HTTP with JWT via {@link SecurityConfig}.
 */
@Slf4j
@Configuration
public class GrpcSecurityConfig {

    @Bean
    @GlobalServerInterceptor
    AuthenticationProcessInterceptor grpcSecurityInterceptor(GrpcSecurity grpc) throws Exception {
        log.info("user-manager gRPC: permitAll on internal port (HTTP REST still requires JWT)");
        return grpc.authorizeRequests(requests -> requests.allRequests().permitAll()).build();
    }
}
