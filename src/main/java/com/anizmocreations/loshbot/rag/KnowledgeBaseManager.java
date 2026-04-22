package com.anizmocreations.loshbot.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
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

        // Split document into chunks
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = textSplitter.apply(documents);

        // Store into Vector Database
        vectorStore.add(splitDocuments);
        
        // Persist to disk
        saveStore();
    }
    
    public List<Document> search(String query) {
        // Performs similarity search
        return vectorStore.similaritySearch(query);
    }
}
