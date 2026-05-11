package org.commonplot.backend.chat;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// ============================================================
//  ChatRepository — Spring Data MongoDB
//  CRUD gratuit + query-uri custom
// ============================================================
@Repository
public interface ChatRepository extends MongoRepository<ChatMessage, String> {

    // Ultimele N mesaje sortate după timp
    List<ChatMessage> findTop50ByOrderBySentAtAsc();

    // Mesajele unui user
    List<ChatMessage> findByUsernameOrderBySentAtAsc(String username);
}