package com.anizmocreations.loshbot.entity;

import java.io.Serializable;

public class SystemSettings implements Serializable {
    private String botName = "Loshbot";
    private String aiProvider = "ollama";
    private String openAiApiKey = "";

    public String getBotName() { return botName; }
    public void setBotName(String botName) { this.botName = botName; }

    public String getAiProvider() { return aiProvider; }
    public void setAiProvider(String aiProvider) { this.aiProvider = aiProvider; }

    public String getOpenAiApiKey() { return openAiApiKey; }
    public void setOpenAiApiKey(String openAiApiKey) { this.openAiApiKey = openAiApiKey; }
}
