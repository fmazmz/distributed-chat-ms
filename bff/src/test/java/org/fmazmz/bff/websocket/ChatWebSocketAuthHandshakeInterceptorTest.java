package org.fmazmz.bff.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.ServletServerHttpRequest;

class ChatWebSocketAuthHandshakeInterceptorTest {

    private JwtDecoder jwtDecoder;
    private ChatWebSocketAuthHandshakeInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtDecoder = mock(JwtDecoder.class);
        interceptor = new ChatWebSocketAuthHandshakeInterceptor(jwtDecoder);
    }

    @Test
    void rejectsHandshakeWithoutCredentials() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        boolean accepted = interceptor.beforeHandshake(request, mock(ServerHttpResponse.class), mock(WebSocketHandler.class), new HashMap<>());

        assertThat(accepted).isFalse();
    }

    @Test
    void acceptsHandshakeWithAuthorizationHeader() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer test.jwt.token");
        when(request.getHeaders()).thenReturn(headers);
        when(jwtDecoder.decode(anyString()))
                .thenReturn(Jwt.withTokenValue("test.jwt.token")
                        .header("alg", "none")
                        .subject("41cd4938-5789-4816-8d65-42f47995e0b6")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build());

        Map<String, Object> attrs = new HashMap<>();
        boolean accepted = interceptor.beforeHandshake(request, mock(ServerHttpResponse.class), mock(WebSocketHandler.class), attrs);

        assertThat(accepted).isTrue();
        assertThat(attrs.get(ChatWebSocketAuthHandshakeInterceptor.USER_ID_ATTR))
                .isEqualTo("41cd4938-5789-4816-8d65-42f47995e0b6");
        assertThat(attrs.get(ChatWebSocketAuthHandshakeInterceptor.BEARER_ATTR)).isEqualTo("Bearer test.jwt.token");
    }

    @Test
    void acceptsHandshakeWithHttpOnlyCookie() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setCookies(new jakarta.servlet.http.Cookie("chat_access_token", "cookie.jwt.token"));
        ServerHttpRequest request = new ServletServerHttpRequest(servletRequest);

        when(jwtDecoder.decode(anyString()))
                .thenReturn(Jwt.withTokenValue("cookie.jwt.token")
                        .header("alg", "none")
                        .subject("cb0e8e42-24f5-4395-91f0-dec6ba374b3e")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build());

        Map<String, Object> attrs = new HashMap<>();
        boolean accepted = interceptor.beforeHandshake(request, mock(ServerHttpResponse.class), mock(WebSocketHandler.class), attrs);

        assertThat(accepted).isTrue();
        assertThat(attrs.get(ChatWebSocketAuthHandshakeInterceptor.BEARER_ATTR)).isEqualTo("Bearer cookie.jwt.token");
    }
}
