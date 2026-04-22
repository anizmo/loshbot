package com.anizmocreations.loshbot.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class Message implements Serializable {

    private final UUID id;
    private final UUID conversationId;
    private final Role role;
    private final String content;
    private final Instant timestamp;

    public Message(UUID id, UUID conversationId, Role role, String content, Instant timestamp) {
        this.id = id;
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
