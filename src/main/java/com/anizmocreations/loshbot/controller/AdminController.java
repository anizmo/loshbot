package com.anizmocreations.loshbot.controller;

import com.anizmocreations.loshbot.aimodel.SettingsManager;
import com.anizmocreations.loshbot.conversation.ConversationManager;
import com.anizmocreations.loshbot.entity.Conversation;
import com.anizmocreations.loshbot.entity.Message;
import com.anizmocreations.loshbot.entity.SystemSettings;
import com.anizmocreations.loshbot.entity.User;
import com.anizmocreations.loshbot.entity.UserRole;
import com.anizmocreations.loshbot.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ConversationManager conversationManager;
    private final SettingsManager settingsManager;

    public AdminController(UserRepository userRepository, ConversationManager conversationManager, SettingsManager settingsManager) {
        this.userRepository = userRepository;
        this.conversationManager = conversationManager;
        this.settingsManager = settingsManager;
    }

    @GetMapping("/config")
    public SystemSettings getConfig() {
        return settingsManager.getSettings();
    }

    @PostMapping("/config")
    public String updateConfig(@RequestBody SystemSettings request) {
        settingsManager.update(request);
        return "Configuration saved and applied instantly!";
    }

    @GetMapping("/visitors")
    public List<User> getVisitors() {
        return userRepository.findByRole(UserRole.VISITOR);
    }

    @GetMapping("/visitors/{id}/conversations")
    public List<Conversation> getVisitorConversations(@PathVariable UUID id) {
        return conversationManager.getConversationsForUser(id);
    }

    @GetMapping("/conversations/{id}/messages")
    public List<Message> getConversationMessages(@PathVariable UUID id) {
        return conversationManager.getMessages(id);
    }
}
