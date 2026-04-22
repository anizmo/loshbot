package com.anizmocreations.loshbot.prompt;

import org.springframework.ai.chat.messages.Message;
import java.util.List;

public interface PromptComposer {

    List<Message> compose(
            String systemPrompt,
            List<com.anizmocreations.loshbot.entity.Message> history,
            String userMessage
    );
}

