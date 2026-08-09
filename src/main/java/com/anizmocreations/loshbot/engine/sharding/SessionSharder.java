package com.anizmocreations.loshbot.engine.sharding;

import com.anizmocreations.loshbot.entity.Message;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SessionSharder {

    private final EmbeddingModel embeddingModel;
    // Ephemeral stores for each session to keep RAM low
    private final Map<UUID, SimpleVectorStore> sessionStores = new ConcurrentHashMap<>();

    public SessionSharder(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * Adds a message to the session-specific vector index.
     */
    public void indexMessage(UUID conversationId, Message message) {
        SimpleVectorStore store = sessionStores.computeIfAbsent(conversationId,
                id -> SimpleVectorStore.builder(embeddingModel).build());

        Document doc = new Document(message.getContent(), Map.of(
                "role", message.getRole().name(),
                "timestamp", message.getTimestamp().toString()
        ));

        store.add(List.of(doc));
    }

    /**
     * Retrieves the top-3 most semantically relevant messages from history.
     */
    public List<Message> getRelevantShards(UUID conversationId, String query) {
        SimpleVectorStore store = sessionStores.get(conversationId);
        if (store == null) return List.of();

        List<Document> documents = store.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3)
                        .build()
        );

        return documents.stream()
                .map(doc -> {
                    Message m = new Message();
                    m.setContent(doc.getText());
                    m.setRole(com.anizmocreations.loshbot.entity.Role.valueOf((String) doc.getMetadata().get("role")));
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * Cleanup session store to free RAM.
     */
    public void clearSession(UUID conversationId) {
        sessionStores.remove(conversationId);
    }
}
