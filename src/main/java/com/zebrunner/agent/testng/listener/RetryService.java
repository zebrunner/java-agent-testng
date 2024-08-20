package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.retry.RetryContext;
import com.zebrunner.agent.testng.core.retry.RetryItemContext;
import lombok.extern.slf4j.Slf4j;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class RetryService {

    private RetryService() {
    }

    public static void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> retryAnalyzerClass, ITestContext context, ITestNGMethod method) {
        String key = buildRetryAnalyzerClassKey(method);
        context.setAttribute(key, retryAnalyzerClass);
    }

    public static Optional<Class<? extends IRetryAnalyzer>> getRetryAnalyzerClass(ITestContext context, ITestNGMethod method) {
        String key = buildRetryAnalyzerClassKey(method);
        return Optional.ofNullable(((Class<? extends IRetryAnalyzer>) context.getAttribute(key)));
    }

    private static String buildRetryAnalyzerClassKey(ITestNGMethod method) {
        String pattern = "retry-analyzer-class-%s.%s(%s)";
        ConstructorOrMethod constructorOrMethod = method.getConstructorOrMethod();

        String className = method.getTestClass().getName();
        String methodName = constructorOrMethod.getName();
        String argumentTypes = Arrays.stream(constructorOrMethod.getParameterTypes())
                                     .map(Class::getName)
                                     .collect(Collectors.joining(","));
        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(method);

        return String.format(pattern, className, methodName, argumentTypes, instanceIndex);
    }

    public static String buildRetryAnalyzerClassKey(ITestResult result) {
        ITestNGMethod method = result.getMethod();
        ITestContext testContext = result.getTestContext();
        Object[] parameters = result.getParameters();

        String pattern = "[%s]-retry-analyzer-class-%s.%s(%s)[%d][%d]";
        ConstructorOrMethod constructorOrMethod = method.getConstructorOrMethod();

        String thread = Thread.currentThread().getName();
        String className = method.getTestClass().getName();
        String methodName = constructorOrMethod.getName();
        String argumentTypes = Arrays.stream(constructorOrMethod.getParameterTypes())
                                     .map(Class::getName)
                                     .collect(Collectors.joining(","));
        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(method);
        int dataProviderIndex = RunContextService.getCurrentDataProviderIndex(method, testContext, parameters);

        return String.format(pattern, thread, className, methodName, argumentTypes, instanceIndex, dataProviderIndex);
    }

    public static void setRetryStarted(ITestNGMethod method, ITestContext context) {
        getOrInitRetryItemContext(method, context)
                .setStarted();
    }

    public static void setRetryFinished(ITestNGMethod method, ITestContext context) {
        getOrInitRetryItemContext(method, context)
                .setFinished();
    }

    private static RetryItemContext getOrInitRetryItemContext(ITestNGMethod method, ITestContext context) {
        RetryContext retryContext = getRetryContext(context)
                .orElseGet(() -> createRetryContext(context));

        Map<Integer, RetryItemContext> retryItemContexts = retryContext.getRetryItemContexts();
        if (retryItemContexts == null) {
            retryItemContexts = new ConcurrentHashMap<>();
            retryContext.setRetryItemContexts(retryItemContexts);
        }

        return retryItemContexts.computeIfAbsent(method.getParameterInvocationCount(), count -> new RetryItemContext());
    }

    public static boolean isRetryFinished(ITestNGMethod method, ITestContext context) {
        return getRetryContext(context)
                .map(RetryContext::getRetryItemContexts)
                .map(retryItemContext -> retryItemContext.get(method.getParameterInvocationCount()))
                .map(RetryItemContext::isFinished)
                .orElse(true);
    }

    private static Optional<RetryContext> getRetryContext(ITestContext context) {
        String retryContextName = "retry-context-" + Thread.currentThread().getName();
        return Optional.ofNullable((RetryContext) context.getAttribute(retryContextName));
    }

    private static RetryContext createRetryContext(ITestContext context) {
        RetryContext retryContext = new RetryContext();
        String retryContextName = "retry-context-" + Thread.currentThread().getName();
        context.setAttribute(retryContextName, retryContext);
        return retryContext;
    }

}
