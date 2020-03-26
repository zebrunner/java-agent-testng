package com.zebrunner.agent.testng.core.retry;

import lombok.Getter;
import lombok.Setter;
import org.testng.IRetryAnalyzer;

import java.util.Map;

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

}
