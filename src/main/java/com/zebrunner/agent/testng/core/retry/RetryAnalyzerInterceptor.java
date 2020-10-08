package com.zebrunner.agent.testng.core.retry;

import com.zebrunner.agent.testng.listener.RetryService;
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

    private final AtomicInteger index = new AtomicInteger(0);
    private IRetryAnalyzer retryAnalyzer;

    @Override
    public boolean retry(ITestResult result) {
        ITestNGMethod method = result.getMethod();
        ITestContext context = result.getTestContext();

        retryAnalyzer = getOriginalRetryAnalyzer(context, method);
        boolean needRetry = retryAnalyzer.retry(result);
        if (needRetry) {
            RetryService.setRetryStarted(method, context);

            String message = result.getThrowable().getMessage();
            RetryService.setRetryFailureReason(index.incrementAndGet(), message, method, context);
        } else {
            RetryService.setRetryFinished(method, context);
        }
        return needRetry;
    }

    private IRetryAnalyzer getOriginalRetryAnalyzer(ITestContext context, ITestNGMethod method) {
        return retryAnalyzer != null
                ? retryAnalyzer
                : RetryService.getRetryAnalyzerClass(context, method)
                              .map(InstanceCreator::newInstance)
                              .orElseThrow(() -> new RuntimeException("There are no retry analyzer to apply."));
    }

}
