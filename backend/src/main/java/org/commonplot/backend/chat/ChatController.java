package org.commonplot.backend.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ============================================================
//  ChatController.java — chat real-time cu WebSocket + MongoDB
//
//  WebSocket:
//    SEND   /app/chat.send   → trimite mesaj
//    TOPIC  /topic/chat      → primești mesajele live
//
//  REST:
//    GET  /api/chat/history  → ultimele 50 mesaje (la conectare)
// ============================================================
@RestController
@CrossOrigin(originPatterns = "*")
public class ChatController {

    private final ChatRepository       chatRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatRepository chatRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.chatRepository    = chatRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ── WebSocket: primește mesaj, salvează în MongoDB, broadcast ──
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, String> payload) {
        String username = payload.getOrDefault("username", "Anonymous");
        String content  = payload.getOrDefault("content", "");
        String role     = payload.getOrDefault("role", "USER");

        if (content.isBlank()) return;

        // Salvează în MongoDB
        ChatMessage message = new ChatMessage(username, content, role);
        ChatMessage saved   = chatRepository.save(message);

        // Broadcast la toți clienții conectați
        messagingTemplate.convertAndSend("/topic/chat", saved);
    }

    // ── REST: istoricul mesajelor la conectare ─────────────────────
    @GetMapping("/api/chat/history")
    public List<ChatMessage> getHistory() {
        return chatRepository.findTop50ByOrderBySentAtAsc();
    }
}