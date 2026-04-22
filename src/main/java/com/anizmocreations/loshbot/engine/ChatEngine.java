package com.anizmocreations.loshbot.engine;

import com.anizmocreations.loshbot.aimodel.ModelProvider;
import com.anizmocreations.loshbot.conversation.ConversationManager;
import com.anizmocreations.loshbot.entity.Message;
import com.anizmocreations.loshbot.persona.Persona;
import com.anizmocreations.loshbot.prompt.PromptComposer;
import com.anizmocreations.loshbot.rag.KnowledgeBaseManager;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatEngine {

    private final ConversationManager conversationManager;
    private final ModelProvider modelProvider;
    private final PromptComposer promptComposer;
    private final KnowledgeBaseManager knowledgeBaseManager;

    public ChatEngine(
            ConversationManager conversationManager,
            ModelProvider modelProvider,
            PromptComposer promptComposer,
            KnowledgeBaseManager knowledgeBaseManager
    ) {
        this.conversationManager = conversationManager;
        this.modelProvider = modelProvider;
        this.promptComposer = promptComposer;
        this.knowledgeBaseManager = knowledgeBaseManager;
    }

    public String chat(UUID conversationId, String userMessage, Persona persona) {
        try {
            // 1. Get history BEFORE adding the current message
            List<Message> history = conversationManager.getMessages(conversationId);

            // 2. Retrieve relevant context from Knowledge Base
            List<Document> relevantDocs = knowledgeBaseManager.search(userMessage);
            
            // 3. Augment ONLY the current turn's message with context
            String currentTurnMessage = userMessage;
            if (!relevantDocs.isEmpty()) {
                String contextText = relevantDocs.stream()
                        .map(Document::getText)
                        .collect(Collectors.joining("\n---\n"));
                
                currentTurnMessage = """
                        [EXTERNAL REFERENCE MATERIAL START]
                        %s
                        [EXTERNAL REFERENCE MATERIAL END]
                        
                        Instruction: Use the reference material above to answer the user's question about the author or company described. Do not adopt their identity.
                        
                        User: %s
                        """.formatted(contextText, userMessage);
            }

            // 4. Compose final prompt using history (clean) and the augmented current turn
            List<org.springframework.ai.chat.messages.Message> messages = promptComposer.compose(
                    persona.systemMessage(),
                    history,
                    currentTurnMessage
            );

            Prompt prompt = new Prompt(messages);
            String response = modelProvider.generate(prompt);

            // 5. Save the CLEAN messages to history (original user message + AI response)
            conversationManager.addMessage(conversationId, "USER", userMessage);
            conversationManager.addMessage(conversationId, "ASSISTANT", response);

            return response;
        } catch (Exception e) {
            // Clean up the technical error for the user
            String errorMsg = "The AI service is currently unavailable.";
            if (e.getMessage().contains("Connection refused") || e.getMessage().contains("embed")) {
                errorMsg = "Could not connect to the AI model. Please ensure your provider (Ollama/OpenAI) is active.";
            }
            throw new RuntimeException(errorMsg);
        }
    }
}
