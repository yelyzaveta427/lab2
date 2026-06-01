package com.example.pasir_ihor_kotenko.websocket;
import com.example.pasir_ihor_kotenko.security.JwtUtil;
import com.example.pasir_ihor_kotenko.service.GroupNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.List;
@Component
public class GroupNotificationWebSocketHandler extends TextWebSocketHandler {
    private final JwtUtil jwtUtil;
    private final GroupNotificationService groupNotificationService;
    public GroupNotificationWebSocketHandler(JwtUtil jwtUtil, GroupNotificationService groupNotificationService) {
        this.jwtUtil = jwtUtil;
        this.groupNotificationService = groupNotificationService;
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session.getUri());
        if (token == null || !jwtUtil.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        String email = jwtUtil.extractUsername(token);
        session.getAttributes().put("email", email);
        groupNotificationService.register(email, session);
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object email = session.getAttributes().get("email");
        if (email instanceof String userEmail) {
            groupNotificationService.unregister(userEmail, session);
        }
    }
    private String extractToken(URI uri) {
        if (uri == null) {
            return null;
        }
        List<String> tokens = UriComponentsBuilder.fromUri(uri).build().getQueryParams().get("token");
        return tokens == null || tokens.isEmpty() ? null : tokens.getFirst();
    }
}
