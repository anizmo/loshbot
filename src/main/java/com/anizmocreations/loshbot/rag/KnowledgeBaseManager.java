package com.anizmocreations.loshbot.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeBaseManager {

    private final VectorStore vectorStore;

    public KnowledgeBaseManager(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
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
    }
    
    public List<Document> search(String query) {
        // Performs similarity search
        return vectorStore.similaritySearch(query);
    }
}
