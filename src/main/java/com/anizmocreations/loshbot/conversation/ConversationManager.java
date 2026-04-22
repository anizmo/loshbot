package com.anizmocreations.loshbot.conversation;

import com.anizmocreations.loshbot.entity.Conversation;
import com.anizmocreations.loshbot.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationManager {

    UUID createConversation(UUID userId, String title);

    List<Conversation> getConversationsForUser(UUID userId);

    Optional<Conversation> getConversationById(UUID conversationId);

    void addMessage(UUID conversationId, String role, String content);

    List<Message> getMessages(UUID conversationId);
}
