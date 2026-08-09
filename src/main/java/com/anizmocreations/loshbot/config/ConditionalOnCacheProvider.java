package com.anizmocreations.loshbot.config;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(CacheCondition.class)
public @interface ConditionalOnCacheProvider {
    String value();
}
