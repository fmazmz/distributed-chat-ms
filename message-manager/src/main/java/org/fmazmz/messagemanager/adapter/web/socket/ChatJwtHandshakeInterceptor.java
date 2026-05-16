package org.fmazmz.messagemanager.adapter.web.socket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class ChatJwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String BEARER_ATTRIBUTE = "bearerToken";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth == null || auth.isBlank()) {
            String token = request.getURI().getQuery();
            if (token != null && token.startsWith("token=")) {
                auth = "Bearer " + token.substring("token=".length());
            }
        }
        if (auth != null && !auth.isBlank()) {
            attributes.put(BEARER_ATTRIBUTE, auth);
        }
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {}
}
