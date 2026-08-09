package com.anizmocreations.loshbot.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.File;
import java.io.IOException;

public class CacheCondition implements Condition {
    private static final String SETTINGS_FILE = "system_settings.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String expectedProvider = (String) metadata.getAllAnnotationAttributes(ConditionalOnCacheProvider.class.getName()).getFirst("value");

        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            return "IN_MEMORY".equals(expectedProvider);
        }

        try {
            JsonNode node = objectMapper.readTree(file);
            String actualProvider = node.has("cacheProvider") ? node.get("cacheProvider").asText() : "IN_MEMORY";
            return actualProvider.equals(expectedProvider);
        } catch (IOException e) {
            return "IN_MEMORY".equals(expectedProvider);
        }
    }
}
