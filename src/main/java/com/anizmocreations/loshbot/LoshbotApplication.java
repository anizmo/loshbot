package com.anizmocreations.loshbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;

@SpringBootApplication
public class LoshbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoshbotApplication.class, args);
	}

	@Bean
	public VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}

}
