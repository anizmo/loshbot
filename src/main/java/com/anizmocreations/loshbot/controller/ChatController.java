package com.anizmocreations.loshbot.controller;

import com.anizmocreations.loshbot.conversation.ConversationManager;
import com.anizmocreations.loshbot.engine.ChatEngine;
import com.anizmocreations.loshbot.persona.Persona;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatEngine chatEngine;
    private final ConversationManager conversationManager;

    // A default persona for the chatbot. This can be externalized later.
    private final Persona defaultPersona = new Persona(
            "loshbot",
            "You are Loshbot, an AI business assistant. You are running locally on the user's machine. " +
            "You were created by the Loshbot Open Source project. " +
            "Provide direct, natural, and helpful answers. " +
            "DO NOT include internal reasoning. Only output the final response."
    );

    public ChatController(ChatEngine chatEngine, ConversationManager conversationManager) {
        this.chatEngine = chatEngine;
        this.conversationManager = conversationManager;
    }

    @PostMapping("/create")
    public UUID createConversation(@RequestParam UUID userId, @RequestBody(required = false) String title) {
        String conversationTitle = (title != null && !title.isEmpty()) ? title : "New Conversation";
        return conversationManager.createConversation(userId, conversationTitle);
    }

    @PostMapping("/{conversationId}")
    public String chat(@PathVariable UUID conversationId, @RequestBody String prompt) {
        return chatEngine.chat(conversationId, prompt, defaultPersona);
    }
}
