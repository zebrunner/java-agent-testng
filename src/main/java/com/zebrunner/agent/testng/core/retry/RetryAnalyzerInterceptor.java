package com.zebrunner.agent.testng.core.retry;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.registrar.TestRunRegistrar;
import com.zebrunner.agent.testng.listener.RetryService;
import lombok.extern.slf4j.Slf4j;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.InstanceCreator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Retry analyzer interceptor that keeps track of invocation index and checks if all test method retries has
 * been executed. Method with retries is only registered once to Zebrunner
 */
@Slf4j
public class RetryAnalyzerInterceptor implements IRetryAnalyzer {

    private static final Map<String, IRetryAnalyzer> RETRY_ANALYZER_KEY_TO_IDENTITY = new ConcurrentHashMap<>();
    private final TestRunRegistrar registrar;

    public RetryAnalyzerInterceptor() {
        this.registrar = TestRunRegistrar.getInstance();
    }

    @Override
    public boolean retry(ITestResult result) {
        ITestNGMethod method = result.getMethod();
        ITestContext context = result.getTestContext();

        IRetryAnalyzer retryAnalyzer = getOriginalRetryAnalyzer(result);
        boolean needRetry = retryAnalyzer.retry(result);

        // checking whether there are some known issues were mapped for stacktrace and skipping retry logic in such case
        if ((ConfigurationHolder.getRunRetryKnownIssues() != null) && !ConfigurationHolder.getRunRetryKnownIssues()) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                result.getThrowable().printStackTrace(pw);
            }
            if (registrar.isKnownIssueAttachedToTest(sw.toString())) {
                log.info("Known issue is attached to test for current failure stacktrace. Hence skipping retry logic");
                needRetry = false;
            } else {
                log.debug("No known issues are attached");
            }
        } else {
            log.debug("Feature with not retrying known issues is disabled");
        }

        if (needRetry) {
            RetryService.setRetryStarted(method, context);
        } else {
            RetryService.setRetryFinished(method, context);
        }
        return needRetry;
    }

    private IRetryAnalyzer getOriginalRetryAnalyzer(ITestResult result) {
        return RETRY_ANALYZER_KEY_TO_IDENTITY.computeIfAbsent(
                RetryService.buildRetryAnalyzerClassKey(result),
                $ -> RetryService.getRetryAnalyzerClass(result.getTestContext(), result.getMethod())
                                 .map(InstanceCreator::newInstance)
                                 .orElseThrow(() -> new RuntimeException("There are no retry analyzer to apply."))
        );
    }

}
