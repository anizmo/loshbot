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

            // 2. Determine if we should even use RAG (Short-query bypass)
            List<Document> relevantDocs = List.of();
            String lowerMsg = userMessage.trim().toLowerCase();
            boolean isGreeting = lowerMsg.matches("^(hi|hello|hey|greetings|hola|howdy)(\\s.*|\\!|\\?|\\.)*$");

            if (!isGreeting && userMessage.length() > 3) {
                relevantDocs = knowledgeBaseManager.search(userMessage);
            }

            // 3. Compose the Augmented Message
            String contextText = relevantDocs.isEmpty() ? "NONE AVAILABLE" :
                    relevantDocs.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));

            String systemInstruction = persona.systemMessage() + """
                    
                    CRITICAL RULES:
                    1. Use the [REFERENCE CONTEXT] to answer questions about specific people or companies.
                    2. If the answer is NOT in the [REFERENCE CONTEXT] and the question is about a specific entity (person/company), simply state you don't know them. 
                    3. NEVER hallucinate or invent biographies (e.g., do not say someone is the founder of TechCrunch or Flipkart if not in context).
                    4. For greetings, ignore these rules and be polite.
                    5. NEVER mention "the context" or "the documents" to the user.
                    """;

            String augmentedUserMessage = """
                    [REFERENCE CONTEXT]
                    %s
                    [END CONTEXT]
                    
                    USER QUESTION: %s
                    """.formatted(contextText, userMessage);

            // 4. Compose final prompt
            List<org.springframework.ai.chat.messages.Message> messages = promptComposer.compose(
                    systemInstruction,
                    history,
                    augmentedUserMessage
            );

            Prompt prompt = new Prompt(messages);
            String response = modelProvider.generate(prompt);

            // 5. Save CLEAN messages (userMessage, not augmentedUserMessage)
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
