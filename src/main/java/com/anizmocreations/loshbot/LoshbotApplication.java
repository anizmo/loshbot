package com.anizmocreations.loshbot;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableCaching
@EnableScheduling
public class LoshbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoshbotApplication.class, args);
	}

	@Bean
	public SimpleVectorStore vectorStore(org.springframework.beans.factory.ObjectProvider<EmbeddingModel> embeddingModelProvider) {
		return SimpleVectorStore.builder(embeddingModelProvider.getIfAvailable()).build();
	}

}
