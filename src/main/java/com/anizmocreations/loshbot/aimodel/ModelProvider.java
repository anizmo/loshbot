package com.anizmocreations.loshbot.aimodel;

import org.springframework.ai.chat.prompt.Prompt;

public interface ModelProvider {

    String generate(Prompt prompt);

}
