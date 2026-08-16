package com.cafestory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompPrincipalHandshakeHandler handshakeHandler;

    public WebSocketConfig(StompPrincipalHandshakeHandler handshakeHandler) {
        this.handshakeHandler = handshakeHandler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // setHandshakeHandler là thứ làm convertAndSendToUser(userId, ...) hoạt
        // động: nó gắn Principal có tên = userId cho phiên. Thiếu nó thì mọi
        // message gửi tới user destination đều bị loại bỏ im lặng.
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(handshakeHandler);
    }
}
