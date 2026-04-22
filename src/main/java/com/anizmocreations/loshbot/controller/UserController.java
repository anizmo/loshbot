package com.anizmocreations.loshbot.controller;

import com.anizmocreations.loshbot.conversation.ConversationManager;
import com.anizmocreations.loshbot.entity.Conversation;
import com.anizmocreations.loshbot.entity.User;
import com.anizmocreations.loshbot.entity.UserRole;
import com.anizmocreations.loshbot.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final ConversationManager conversationManager;

    public UserController(UserRepository userRepository, ConversationManager conversationManager) {
        this.userRepository = userRepository;
        this.conversationManager = conversationManager;
    }

    @PostMapping("/onboard")
    public User onboard(@RequestBody UserRequest request) {
        User user = new User(
                UUID.randomUUID(),
                request.name(),
                null,
                request.company(),
                request.industry(),
                UserRole.ADMIN
        );
        return userRepository.save(user);
    }

    @PostMapping("/visitor")
    public User registerVisitor(@RequestBody VisitorRequest request) {
        User user = new User(
                UUID.randomUUID(),
                request.name(),
                request.email(),
                null,
                null,
                UserRole.VISITOR
        );
        return userRepository.save(user);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable UUID id) {
        return userRepository.findById(id).orElseThrow();
    }

    @GetMapping("/{id}/conversations")
    public List<Conversation> getConversations(@PathVariable UUID id) {
        return conversationManager.getConversationsForUser(id);
    }

    public record UserRequest(String name, String company, String industry) {}
    public record VisitorRequest(String name, String email) {}
}
