package com.anizmocreations.loshbot.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class Conversation implements Serializable {

    private final UUID id;
    private final UUID userId;
    private final String title;
    private final Instant createdAt;
    private Instant updatedAt;

    public Conversation(UUID id, UUID userId, String title, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
