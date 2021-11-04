package com.zebrunner.agent.testng.core.retry;

import lombok.Getter;
import lombok.Setter;
import org.testng.IRetryAnalyzer;

import java.util.Map;
import java.util.Optional;

@Getter
@Setter
public class RetryContext {

    /**
     * Retry analyzer implementation
     */
    private Class<? extends IRetryAnalyzer> originalRetryAnalyzerClass;

    /**
     * Key is a parameter index (if data provider)
     */
    private Map<Integer, RetryItemContext> retryItemContexts;

    @Override
    public String toString() {
        return "RetryContext{" +
                "originalRetryAnalyzerClass=" + Optional.ofNullable(originalRetryAnalyzerClass).map(Class::getName).orElse("--") +
                ", retryItemContexts=" + retryItemContexts +
                '}';
    }

}
