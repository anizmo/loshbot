package com.anizmocreations.loshbot.aimodel;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class UniversalModelProvider implements ModelProvider {

    private final ChatModel chatModel;

    public UniversalModelProvider(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String generate(Prompt prompt) {
        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
