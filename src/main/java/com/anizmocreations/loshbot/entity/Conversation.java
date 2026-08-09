package com.anizmocreations.loshbot.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class Conversation implements Serializable {

    private UUID id;
    private UUID userId;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;

    // Default constructor for JSON deserialization (Redis)
    public Conversation() {
    }

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

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
