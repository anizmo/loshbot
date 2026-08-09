package com.anizmocreations.loshbot.core.trust;

import com.anizmocreations.loshbot.tenant.quota.QuotaManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Aspect
@Component
public class TrustInterceptorAspect {

    // Regex for basic PII scrubbing
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
    // Restricted keywords for output validation
    private static final String[] RESTRICTED_KEYWORDS = {"INTERNAL_SECRET", "ADMIN_PASSWORD", "ROOT_ACCESS"};
    private final ObjectProvider<QuotaManager> quotaManagerProvider;

    public TrustInterceptorAspect(ObjectProvider<QuotaManager> quotaManagerProvider) {
        this.quotaManagerProvider = quotaManagerProvider;
    }

    @Around("@annotation(trustFilter)")
    public Object protect(ProceedingJoinPoint joinPoint, TrustFilter trustFilter) throws Throwable {
        System.out.println("[DEBUG] TrustInterceptor - Intercepting call: " + joinPoint.getSignature().getName());
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        // 1. Quota Check (Optional - only if QuotaManager is active/REDIS)
        try {
            QuotaManager quotaManager = quotaManagerProvider.getIfAvailable();
            if (quotaManager != null) {
                System.out.println("[DEBUG] TrustInterceptor - Performing quota check...");
                String tenantId = "default-tenant";
                long remaining = quotaManager.consume(tenantId, 10, 0.1, 1);
                if (remaining < 0) {
                    System.out.println("[DEBUG] TrustInterceptor - Rate limit exceeded.");
                    return "⚠️ Rate Limit Exceeded: Your tenant quota has been exhausted. Please wait and try again later.";
                }
                System.out.println("[DEBUG] TrustInterceptor - Quota check passed. Remaining: " + remaining);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] TrustInterceptor - Quota check failed: " + e.getMessage());
            e.printStackTrace();
        }

        // 2. Pre-processing: Scrub PII
        System.out.println("[DEBUG] TrustInterceptor - Scrubbing PII...");
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String && "userMessage".equals(parameterNames[i])) {
                args[i] = scrubPii((String) args[i]);
            }
        }

        // 3. Proceed to execution
        System.out.println("[DEBUG] TrustInterceptor - Proceeding to method execution...");
        Object result;
        try {
            result = joinPoint.proceed(args);
            System.out.println("[DEBUG] TrustInterceptor - Execution complete.");
        } catch (Exception e) {
            System.err.println("[ERROR] TrustInterceptor - Method execution failed: " + e.getMessage());
            throw e;
        }

        // 4. Post-processing: Validate Output
        if (result instanceof String response) {
            System.out.println("[DEBUG] TrustInterceptor - Validating output keywords...");
            for (String keyword : RESTRICTED_KEYWORDS) {
                if (response.contains(keyword)) {
                    System.out.println("[DEBUG] TrustInterceptor - Restricted keyword found: " + keyword);
                    return "⚠️ Security Alert: The AI response was blocked as it contained restricted information.";
                }
            }

            if (trustFilter.jsonOutput() && !isValidJson(response)) {
                System.out.println("[DEBUG] TrustInterceptor - Invalid JSON format detected.");
                return "{\"error\": \"Invalid AI response format. Expected JSON.\"}";
            }
        }

        return result;
    }

    private String scrubPii(String input) {
        if (input == null) return null;
        String scrubbed = EMAIL_PATTERN.matcher(input).replaceAll("[EMAIL_HIDDEN]");
        scrubbed = PHONE_PATTERN.matcher(scrubbed).replaceAll("[PHONE_HIDDEN]");
        return scrubbed;
    }

    private boolean isValidJson(String json) {
        String trimmed = json.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
}
