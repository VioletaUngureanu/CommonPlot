package com.commonplot.backend.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// ============================================================
//  WebSocketConfig.java — Silver challenge
//  STOMP over WebSocket pentru notificări real-time
//  Clienții Vue se abonează la /topic/meetups
// ============================================================
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix pentru mesaje server → client
        registry.enableSimpleBroker("/topic");
        // Prefix pentru mesaje client → server
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173")  // Vue frontend
                .withSockJS();  // fallback pentru browsere fără WS nativ
    }
}