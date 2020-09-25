package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.retry.RetryContext;
import com.zebrunner.agent.testng.core.retry.RetryItemContext;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.TestResult;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RetryService {

    private RetryService() {
    }

    public static void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> retryAnalyzerClass, ITestContext context, ITestNGMethod method) {
        String key = buildRetryAnalyzerClassKey(method);
        context.setAttribute(key, retryAnalyzerClass);
    }

    public static void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> retryAnalyzerClass, ITestResult result) {
        String key = buildRetryAnalyzerClassKey(result);
        result.getTestContext().setAttribute(key, retryAnalyzerClass);
    }

    public static Optional<Class<? extends IRetryAnalyzer>> getRetryAnalyzerClass(ITestContext context, ITestNGMethod method) {
        String key = buildRetryAnalyzerClassKey(method);
        return Optional.ofNullable(((Class<? extends IRetryAnalyzer>) context.getAttribute(key)));
    }

    public static Optional<Class<? extends IRetryAnalyzer>> getRetryAnalyzerClass(ITestResult result) {
        String key = buildRetryAnalyzerClassKey(result);
        return Optional.ofNullable(((Class<? extends IRetryAnalyzer>) result.getTestContext().getAttribute(key)));
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

    private static String buildRetryAnalyzerClassKey(ITestResult result) {
        String pattern = "retry-analyzer-class-%s.%s(%s)[%d][%d]";
        ConstructorOrMethod constructorOrMethod = result.getMethod().getConstructorOrMethod();

        String className = result.getMethod().getTestClass().getName();
        String methodName = constructorOrMethod.getName();
        String argumentTypes = Arrays.stream(constructorOrMethod.getParameterTypes())
                                     .map(Class::getName)
                                     .collect(Collectors.joining(","));
        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(result.getMethod());
        int parameterIndex = ((TestResult) result).getParameterIndex();

        return String.format(pattern, className, methodName, argumentTypes, instanceIndex, parameterIndex);
    }

    public static Map<Integer, String> getRetryFailureReasons(ITestNGMethod method, ITestContext context) {
        return getRetryContext(context)
                .map(RetryContext::getRetryItemContexts)
                .map(retryItemContext -> retryItemContext.get(method.getParameterInvocationCount()))
                .map(RetryItemContext::getRetryFailedReasons)
                .orElseGet(ConcurrentHashMap::new);
    }

    public static void setRetryFailureReason(int retryIndex, String failureReason, ITestNGMethod method, ITestContext context) {
        RetryItemContext retryItemContext = getOrInitRetryItemContext(method, context);

        Map<Integer, String> failureReasons = retryItemContext.getRetryFailedReasons();
        if (failureReasons == null) {
            failureReasons = new ConcurrentHashMap<>();
        }

        failureReasons.put(retryIndex, failureReason);
        retryItemContext.setRetryFailedReasons(failureReasons);
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
