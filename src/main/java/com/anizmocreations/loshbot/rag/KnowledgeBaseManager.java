package com.anizmocreations.loshbot.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class KnowledgeBaseManager {

    private final SimpleVectorStore vectorStore;
    private static final String STORE_PATH = "vector_store.json";

    public KnowledgeBaseManager(SimpleVectorStore vectorStore) {
        this.vectorStore = vectorStore;
        loadStore();
    }

    private void loadStore() {
        File file = new File(STORE_PATH);
        if (file.exists()) {
            vectorStore.load(file);
        }
    }

    private void saveStore() {
        vectorStore.save(new File(STORE_PATH));
    }

    public void addPdfDocument(Resource pdfResource) {
        // Read PDF
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                pdfResource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfBottomTextLinesToDelete(0)
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .build());
        
        List<Document> documents = pdfReader.get();

        // Drastically smaller chunks (200 tokens) for better specific term matching (names, companies)
        TokenTextSplitter textSplitter = new TokenTextSplitter(200, 50, 5, 10000, true, List.of(' ', '\n', '\t'));
        List<Document> splitDocuments = textSplitter.apply(documents);

        // Store into Vector Database
        vectorStore.add(splitDocuments);
        
        // Persist to disk
        saveStore();
    }

    public void clearStore() {
        File file = new File(STORE_PATH);
        if (file.exists()) {
            boolean deleted = file.delete();
            System.out.println("Knowledge Base file deleted: " + deleted);
        }
        // Note: Memory state in SimpleVectorStore cannot be cleared easily without a restart.
        // The reset command ensures the NEXT start is clean.
    }
    
    public List<Document> search(String query) {
        System.out.println("Searching Knowledge Base for: " + query);
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(10)
                        .similarityThreshold(0.4)
                        .build()
        );
        System.out.println("Found " + results.size() + " highly relevant documents.");
        for (Document doc : results) {
            String snippet = doc.getText().substring(0, Math.min(100, doc.getText().length())).replace("\n", " ");
            System.out.println("Match snippet: " + snippet);
        }
        return results;
    }
}
