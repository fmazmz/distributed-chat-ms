package org.fmazmz.bff.websocket;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fmazmz.bff.security.ChatAccessTokenCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTR = "wsUserId";
    public static final String BEARER_ATTR = "wsBearer";

    private final JwtDecoder jwtDecoder;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        String bearer = resolveBearer(request);
        if (bearer == null) {
            log.warn("WebSocket handshake rejected: missing credentials");
            return false;
        }
        try {
            String token = bearer.startsWith("Bearer ") ? bearer.substring(7) : bearer;
            Jwt jwt = jwtDecoder.decode(token);
            attributes.put(USER_ID_ATTR, jwt.getSubject());
            attributes.put(BEARER_ATTR, bearer.startsWith("Bearer ") ? bearer : "Bearer " + token);
            return true;
        } catch (JwtException ex) {
            log.warn("WebSocket handshake rejected: invalid JWT ({})", ex.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {}

    static String resolveBearer(ServerHttpRequest request) {
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && !auth.isBlank()) {
            return auth.trim();
        }
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String fromCookie = ChatAccessTokenCookie.readRawToken(servletRequest.getServletRequest());
            if (fromCookie != null) {
                return fromCookie.startsWith("Bearer ") ? fromCookie : "Bearer " + fromCookie;
            }
        }
        String fromCookieHeader = ChatAccessTokenCookie.readRawTokenFromHandshake(request.getHeaders());
        if (fromCookieHeader != null) {
            return fromCookieHeader.startsWith("Bearer ") ? fromCookieHeader : "Bearer " + fromCookieHeader;
        }
        return null;
    }
}
