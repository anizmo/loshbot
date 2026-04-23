package com.anizmocreations.loshbot.controller;

import com.anizmocreations.loshbot.rag.KnowledgeBaseManager;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeBaseController {

    private final KnowledgeBaseManager knowledgeBaseManager;

    public KnowledgeBaseController(KnowledgeBaseManager knowledgeBaseManager) {
        this.knowledgeBaseManager = knowledgeBaseManager;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
            Resource resource = file.getResource();
            knowledgeBaseManager.addPdfDocument(resource);
            return ResponseEntity.ok("Successfully uploaded and processed: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to process file: " + e.getMessage());
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetKnowledgeBase() {
        try {
            knowledgeBaseManager.clearStore();
            return ResponseEntity.ok("Knowledge base file deleted. Please restart the application for a fully clean state.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to clear knowledge base: " + e.getMessage());
        }
    }
}
