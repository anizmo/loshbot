package com.anizmocreations.loshbot.repo;

import com.anizmocreations.loshbot.entity.Conversation;
import com.anizmocreations.loshbot.entity.Message;
import com.anizmocreations.loshbot.entity.Role;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryConversationRepository implements ConversationRepository {

    private final Map<UUID, Conversation> conversations = new ConcurrentHashMap<>();
    private final Map<UUID, List<Message>> messages = new ConcurrentHashMap<>();

    @Override
    public Conversation create(UUID userId, String title) {
        UUID id = UUID.randomUUID();
        Conversation conversation = new Conversation(id, userId, title, java.time.Instant.now());
        conversations.put(id, conversation);
        messages.put(id, new ArrayList<>());
        return conversation;
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        return Optional.ofNullable(conversations.get(id));
    }

    @Override
    public List<Conversation> findByUserId(UUID userId) {
        return conversations.values().stream()
                .filter(c -> c.getUserId().equals(userId))
                .sorted(Comparator.comparing(Conversation::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void saveMessage(Message message) {
        messages.computeIfAbsent(message.getConversationId(), k -> new ArrayList<>()).add(message);
    }

    @Override
    public List<Message> findMessages(UUID conversationId, int limit) {
        List<Message> allMessages = messages.getOrDefault(conversationId, Collections.emptyList());
        return allMessages.stream()
                .sorted(Comparator.comparing(Message::getTimestamp).reversed())
                .limit(limit)
                .sorted(Comparator.comparing(Message::getTimestamp))
                .collect(Collectors.toList());
    }
}
