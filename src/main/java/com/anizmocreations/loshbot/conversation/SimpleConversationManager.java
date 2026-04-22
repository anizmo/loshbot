package com.anizmocreations.loshbot.conversation;

import com.anizmocreations.loshbot.entity.Conversation;
import com.anizmocreations.loshbot.entity.Message;
import com.anizmocreations.loshbot.entity.Role;
import com.anizmocreations.loshbot.repo.ConversationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SimpleConversationManager implements ConversationManager {

    private final ConversationRepository repository;

    public SimpleConversationManager(ConversationRepository repository) {
        this.repository = repository;
    }

    @Override
    public UUID createConversation(UUID userId, String title) {
        return repository.create(userId, title).getId();
    }

    @Override
    public List<Conversation> getConversationsForUser(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Optional<Conversation> getConversationById(UUID conversationId) {
        return repository.findById(conversationId);
    }

    @Override
    public void addMessage(UUID conversationId, String role, String content) {
        Message message = new Message(
                UUID.randomUUID(),
                conversationId,
                Role.valueOf(role),
                content,
                Instant.now()
        );
        repository.saveMessage(message);
    }

    @Override
    public List<Message> getMessages(UUID conversationId) {
        return repository.findMessages(conversationId, 50);
    }
}
