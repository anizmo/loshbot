package com.anizmocreations.loshbot.repo;

import com.anizmocreations.loshbot.entity.Conversation;
import com.anizmocreations.loshbot.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {

    Conversation create(UUID userId, String title);

    Optional<Conversation> findById(UUID id);

    List<Conversation> findByUserId(UUID userId);

    void saveMessage(Message message);

    List<Message> findMessages(UUID conversationId, int limit);
}

