package com.zebrunner.agent.testng.core.retry;

import com.zebrunner.agent.testng.listener.RunContextService;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.InstanceCreator;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Retry analyzer interceptor that keeps track of invocation index and checks if all test method retries has
 * been executed. Method with retries is only registered once to Zebrunner
 */
public class RetryAnalyzerInterceptor implements IRetryAnalyzer {

    private final AtomicInteger index;
    private IRetryAnalyzer retryAnalyzer;

    public RetryAnalyzerInterceptor() {
        this.index = new AtomicInteger(0);
    }

    @Override
    public boolean retry(ITestResult result) {
        ITestNGMethod method = result.getMethod();
        ITestContext context = result.getTestContext();

        retryAnalyzer = getOriginalRetryAnalyzer(method, context);
        boolean needRetry = retryAnalyzer.retry(result);
        if (needRetry) {
            RunContextService.setRetryStarted(method, context);
            RunContextService.setRetryFailureReason(index.incrementAndGet(), result.getThrowable().getMessage(), method, context);
        } else {
            RunContextService.setRetryFinished(method, context);
        }
        return needRetry;
    }

    private IRetryAnalyzer getOriginalRetryAnalyzer(ITestNGMethod method, ITestContext context) {
        Class<? extends IRetryAnalyzer> retryAnalyzerClass = RunContextService.getRetryAnalyzerClass(method, context);
        return retryAnalyzer == null ? InstanceCreator.newInstance(retryAnalyzerClass) : retryAnalyzer;
    }

}
