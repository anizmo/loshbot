package com.anizmocreations.loshbot.entity;

import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {
    private final UUID id;
    private final String name;
    private final String email;
    private final String company;
    private final String industry;
    private final UserRole role;

    public User(UUID id, String name, String email, String company, String industry, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.company = company;
        this.industry = industry;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCompany() {
        return company;
    }

    public String getIndustry() {
        return industry;
    }

    public UserRole getRole() {
        return role;
    }
}
