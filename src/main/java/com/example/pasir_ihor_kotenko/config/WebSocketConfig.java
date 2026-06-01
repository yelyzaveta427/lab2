package com.example.pasir_ihor_kotenko.config;
import com.example.pasir_ihor_kotenko.websocket.GroupNotificationWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final GroupNotificationWebSocketHandler groupNotificationWebSocketHandler;
    public WebSocketConfig(GroupNotificationWebSocketHandler groupNotificationWebSocketHandler) {
        this.groupNotificationWebSocketHandler = groupNotificationWebSocketHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(groupNotificationWebSocketHandler, "/ws/group-notifications")
                .setAllowedOriginPatterns("http://localhost:*");
    }
}
