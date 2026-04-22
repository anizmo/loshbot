package com.anizmocreations.loshbot.entity;

import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {
    private final UUID id;
    private final String name;
    private final String company;
    private final String industry;

    public User(UUID id, String name, String company, String industry) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.industry = industry;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getIndustry() {
        return industry;
    }
}
