package org.commonplot.backend.chat;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

// ============================================================
//  ChatMessage.java — document MongoDB
//  Colecție: chat_messages
//  Stochează mesajele de chat în timp real
// ============================================================
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;

    private String username;    // cine a trimis
    private String content;     // textul mesajului
    private LocalDateTime sentAt;
    private String role;        // 'ADMIN' | 'USER' — pentru stilizare în UI

    public ChatMessage() {}

    public ChatMessage(String username, String content, String role) {
        this.username = username;
        this.content  = content;
        this.role     = role;
        this.sentAt   = LocalDateTime.now();
    }

    public String        getId()                      { return id; }
    public void          setId(String id)             { this.id = id; }

    public String        getUsername()                { return username; }
    public void          setUsername(String u)        { this.username = u; }

    public String        getContent()                 { return content; }
    public void          setContent(String c)         { this.content = c; }

    public LocalDateTime getSentAt()                  { return sentAt; }
    public void          setSentAt(LocalDateTime t)   { this.sentAt = t; }

    public String        getRole()                    { return role; }
    public void          setRole(String r)            { this.role = r; }
}