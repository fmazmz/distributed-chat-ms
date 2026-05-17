package org.fmazmz.bff.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketProxyConfig implements WebSocketConfigurer {

    private final MessageManagerWebSocketProxy messageManagerWebSocketProxy;
    private final ChatWebSocketAuthHandshakeInterceptor chatWebSocketAuthHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageManagerWebSocketProxy, "/ws/chat")
                .addInterceptors(chatWebSocketAuthHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
