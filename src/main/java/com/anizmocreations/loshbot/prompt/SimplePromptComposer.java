package com.anizmocreations.loshbot.prompt;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SimplePromptComposer implements PromptComposer {

    @Override
    public List<Message> compose(String systemPrompt, List<com.anizmocreations.loshbot.entity.Message> history, String userMessage) {
        List<Message> messages = new ArrayList<>();
        
        // 1. Add System Message
        messages.add(new SystemMessage(systemPrompt));
        
        // 2. Add History
        for (com.anizmocreations.loshbot.entity.Message msg : history) {
            switch (msg.getRole()) {
                case USER -> messages.add(new UserMessage(msg.getContent()));
                case ASSISTANT -> messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        
        // 3. Add Current User Message (if not already last in history)
        if (history.isEmpty() || !history.get(history.size() - 1).getContent().equals(userMessage)) {
             messages.add(new UserMessage(userMessage));
        }

        return messages;
    }
}
