package com.anizmocreations.loshbot.controller;

import com.anizmocreations.loshbot.aimodel.SettingsManager;
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
    private final SettingsManager settingsManager;

    public ChatController(ChatEngine chatEngine, ConversationManager conversationManager, SettingsManager settingsManager) {
        this.chatEngine = chatEngine;
        this.conversationManager = conversationManager;
        this.settingsManager = settingsManager;
    }

    @GetMapping("/info")
    public BotInfo getInfo() {
        return new BotInfo(settingsManager.getSettings().getBotName());
    }

    @PostMapping("/create")
    public UUID createConversation(@RequestParam UUID userId, @RequestBody(required = false) String title) {
        String conversationTitle = (title != null && !title.isEmpty()) ? title : "New Conversation";
        return conversationManager.createConversation(userId, conversationTitle);
    }

    @PostMapping("/{conversationId}")
    public ChatResponse chat(@PathVariable UUID conversationId, @RequestBody String prompt) {
        String botName = settingsManager.getSettings().getBotName();
        
        Persona dynamicPersona = new Persona(
                botName.toLowerCase(),
                "You are " + botName + ", an AI business assistant. " +
                "IMPORTANT: You will be provided with documents (context) to help answer questions. " +
                "These documents describe other people or companies. You are NOT the person described in the documents. " +
                "If a document says 'I did X', it refers to the author, NOT to you. " +
                "Always maintain your identity as " + botName + ". Refer to authors of documents in the third person. " +
                "Provide direct, natural, and helpful answers. DO NOT include internal reasoning."
        );
        
        try {
            String content = chatEngine.chat(conversationId, prompt, dynamicPersona);
            return new ChatResponse(content, true, null);
        } catch (Exception e) {
            return new ChatResponse(null, false, e.getMessage());
        }
    }

    public record BotInfo(String name) {}
    public record ChatResponse(String content, boolean success, String error) {}
}
