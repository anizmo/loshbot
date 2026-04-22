package com.anizmocreations.loshbot.aimodel;

import com.anizmocreations.loshbot.entity.SystemSettings;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
public class UniversalModelProvider implements ModelProvider {

    private final SettingsManager settingsManager;
    private final OllamaChatModel defaultOllamaModel;
    
    // Injected Spring AI defaults to help build dynamic models
    private final ObjectProvider<RetryTemplate> retryTemplateProvider;
    private final ObjectProvider<ObservationRegistry> observationRegistryProvider;
    private final ObjectProvider<ToolCallingManager> toolCallingManagerProvider;

    private OpenAiChatModel cachedOpenAiModel;
    private String cachedApiKey;

    public UniversalModelProvider(
            SettingsManager settingsManager, 
            OllamaChatModel defaultOllamaModel,
            ObjectProvider<RetryTemplate> retryTemplateProvider,
            ObjectProvider<ObservationRegistry> observationRegistryProvider,
            ObjectProvider<ToolCallingManager> toolCallingManagerProvider) {
        this.settingsManager = settingsManager;
        this.defaultOllamaModel = defaultOllamaModel;
        this.retryTemplateProvider = retryTemplateProvider;
        this.observationRegistryProvider = observationRegistryProvider;
        this.toolCallingManagerProvider = toolCallingManagerProvider;
    }

    @Override
    public String generate(Prompt prompt) {
        SystemSettings settings = settingsManager.getSettings();
        ChatModel activeModel;

        if ("openai".equalsIgnoreCase(settings.getAiProvider())) {
            activeModel = getOpenAiModel(settings.getOpenAiApiKey());
        } else {
            activeModel = defaultOllamaModel;
        }

        return activeModel.call(prompt).getResult().getOutput().getText();
    }

    private synchronized ChatModel getOpenAiModel(String apiKey) {
        if (cachedOpenAiModel == null || !apiKey.equals(cachedApiKey)) {
            OpenAiApi api = OpenAiApi.builder().apiKey(apiKey).build();
            
            cachedOpenAiModel = OpenAiChatModel.builder()
                    .openAiApi(api)
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("gpt-4o-mini")
                            .temperature(0.4)
                            .build())
                    .retryTemplate(retryTemplateProvider.getIfAvailable(() -> new RetryTemplate()))
                    .observationRegistry(observationRegistryProvider.getIfAvailable(() -> ObservationRegistry.NOOP))
                    .toolCallingManager(toolCallingManagerProvider.getIfAvailable())
                    .build();
            
            cachedApiKey = apiKey;
        }
        return cachedOpenAiModel;
    }
}
