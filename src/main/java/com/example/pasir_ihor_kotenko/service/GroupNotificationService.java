package com.example.pasir_ihor_kotenko.service;
import com.example.pasir_ihor_kotenko.dto.GroupNotificationMessage;
import com.example.pasir_ihor_kotenko.model.Group;
import com.example.pasir_ihor_kotenko.model.Membership;
import com.example.pasir_ihor_kotenko.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class GroupNotificationService {
    private final ObjectMapper objectMapper;
    private final Map<String, Set<WebSocketSession>> sessionsByEmail = new ConcurrentHashMap<>();
    public GroupNotificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public void register(String email, WebSocketSession session) {
        sessionsByEmail.computeIfAbsent(email, k -> ConcurrentHashMap.newKeySet()).add(session);
    }
    public void unregister(String email, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByEmail.get(email);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionsByEmail.remove(email);
            }
        }
    }
    public void notifyGroupExpenseAdded(Group group, User creator, String title, double amount, List<Membership> participants) {
        int participantCount = participants.size();
        double share = participantCount > 0 ? amount / participantCount : amount;
        for (Membership membership : participants) {
            User participant = membership.getUser();
            if (participant.getId().equals(creator.getId())) {
                continue;
            }
            GroupNotificationMessage notification = new GroupNotificationMessage();
            notification.setGroupId(group.getId());
            notification.setGroupName(group.getName());
            notification.setTitle(title);
            notification.setAmount(amount);
            notification.setUserShare(share);
            notification.setCreatedByEmail(creator.getEmail());
            notification.setMessage(String.format(
                    Locale.ROOT,
                    "%s dodał wydatek \"%s\" w grupie %s. Twoja część: %.2f zł.",
                    creator.getEmail(),
                    title,
                    group.getName(),
                    share
            ));
            sendToUser(participant.getEmail(), notification);
        }
    }
    private void sendToUser(String email, GroupNotificationMessage notification) {
        Set<WebSocketSession> sessions = sessionsByEmail.get(email);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(notification);
            TextMessage message = new TextMessage(payload);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
