package org.fmazmz.messagemanager.adapter.web.socket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatHandler chatHandler;
    private final ChatJwtHandshakeInterceptor chatJwtHandshakeInterceptor;

    public WebSocketConfig(ChatHandler chatHandler, ChatJwtHandshakeInterceptor chatJwtHandshakeInterceptor) {
        this.chatHandler = chatHandler;
        this.chatJwtHandshakeInterceptor = chatJwtHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/chat")
                .addInterceptors(chatJwtHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
