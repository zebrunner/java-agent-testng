package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.TestInvocationContext;
import com.zebrunner.agent.testng.core.TestMethodContext;
import com.zebrunner.agent.testng.core.retry.RetryContext;
import com.zebrunner.agent.testng.core.retry.RetryItemContext;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.internal.ConstructorOrMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RunContextService {

    private static List<TestInvocationContext> invocationContexts;

    private RunContextService() {
    }

    public static void incrementMethodInvocationCount(ITestNGMethod method, ITestContext context) {
        TestMethodContext testMethodContext = getOrInitRerunContext(method, context);
        testMethodContext.incrementInvocationCount();
    }

    public static int getMethodInvocationCount(ITestNGMethod method, ITestContext context) {
        TestMethodContext testMethodContext = getOrInitRerunContext(method, context);
        return testMethodContext.getCurrentInvocationCount();
    }

    public static void setHeadlessWasExecuted(ITestNGMethod method, ITestContext context) {
        TestMethodContext testMethodContext = getOrInitRerunContext(method, context);
        testMethodContext.setHeadlessWasExecuted(true);
    }

    public static boolean isHeadlessWasExecuted(ITestNGMethod method, ITestContext context) {
        TestMethodContext testMethodContext = getOrInitRerunContext(method, context);
        return testMethodContext.isHeadlessWasExecuted();
    }

    public static void setOriginalDataProviderIndex(Integer index, ITestNGMethod method, ITestContext context) {
        TestMethodContext testMethodContext = getOrInitRerunContext(method, context);

        Set<Integer> indices = testMethodContext.getOriginalDataProviderIndices();
        if (indices == null) {
            indices = Collections.synchronizedSet(new TreeSet<>());
        }
        indices.add(index);
        testMethodContext.setOriginalDataProviderIndices(indices);
    }

    public static Set<Integer> getDataProviderIndicesForRerun(ITestNGMethod method, ITestContext context) {
        Optional<TestMethodContext> maybeRerunContext = getRerunContext(method, context);
        Optional<Set<Integer>> maybeIndices = maybeRerunContext.map(TestMethodContext::getOriginalDataProviderIndices);
        return maybeIndices.orElse(Collections.emptySet());
    }

    public static void setDataProviderSize(ITestNGMethod method, ITestContext context, int size) {
        TestMethodContext testMethodContext = getOrInitRerunContext(method, context);
        testMethodContext.setDataProviderSize(size);
    }

    public static int getDataProviderSize(ITestNGMethod method, ITestContext context) {
        Optional<TestMethodContext> maybeRerunContext = getRerunContext(method, context);
        Optional<Integer> maybeDataProviderSize = maybeRerunContext.map(TestMethodContext::getDataProviderSize);
        return maybeDataProviderSize.orElse(0);
    }

    public static void setDataProviderCurrentIndex(ITestNGMethod method, ITestContext context, int index) {
        TestMethodContext testMethodContext = getOrInitRerunContext(method, context);
        testMethodContext.setDataProviderCurrentIndex(index);
    }

    public static int getDataProviderCurrentIndex(ITestNGMethod method, ITestContext context) {
        Optional<TestMethodContext> maybeRerunContext = getRerunContext(method, context);
        Optional<Integer> maybeDataProviderCurrentIndex = maybeRerunContext.map(TestMethodContext::getDataProviderCurrentIndex);
        return maybeDataProviderCurrentIndex.orElse(-1);
    }

    public static void setForceRerun(ITestNGMethod method, ITestContext context) {
        TestMethodContext testMethodContext = getOrInitRerunContext(method, context);
        testMethodContext.setForceRerun(true);
    }

    public static boolean isForceRerun(ITestNGMethod method, ITestContext context) {
        Optional<TestMethodContext> maybeRerunContext = getRerunContext(method, context);
        Optional<Boolean> maybeForceRerun = maybeRerunContext.map(TestMethodContext::isForceRerun);
        return maybeForceRerun.orElse(false);
    }

    private static RetryContext createRetryContext(ITestNGMethod method, ITestContext context) {
        TestMethodContext testMethodContext = getOrInitRerunContext(method, context);
        RetryContext retryContext = testMethodContext.getRetryContext();
        if (retryContext == null) {
            retryContext = new RetryContext();
        }

        testMethodContext.setRetryContext(retryContext);
        return retryContext;
    }

    public static void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> retryAnalyzerClass, ITestNGMethod method, ITestContext context) {
        Optional<RetryContext> maybeRetryContext = getRetryContext(method, context);
        RetryContext retryContext = maybeRetryContext.orElseGet(() -> createRetryContext(method, context));
        retryContext.setOriginalRetryAnalyzerClass(retryAnalyzerClass);
    }

    public static Class<? extends IRetryAnalyzer> getRetryAnalyzerClass(ITestNGMethod method, ITestContext context) {
        Optional<RetryContext> maybeRetryContext = getRetryContext(method, context);
        Optional<Class<? extends IRetryAnalyzer>> maybeAnalyzer = maybeRetryContext.map(RetryContext::getOriginalRetryAnalyzerClass);
        return maybeAnalyzer.orElse(null);
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

    public static Map<Integer, String> getRetryFailureReasons(ITestNGMethod method, ITestContext context) {
        Optional<RetryContext> maybeRetryContext =  getRetryContext(method, context);
        Optional<Map<Integer, RetryItemContext>> maybeRetryItemContexts = maybeRetryContext.map(RetryContext::getRetryItemContexts);
        Optional<RetryItemContext> maybeRetryItemContext = maybeRetryItemContexts.map(retryItemContext -> retryItemContext.get(method.getParameterInvocationCount()));
        return maybeRetryItemContext.map(RetryItemContext::getRetryFailedReasons).orElse(new ConcurrentHashMap<>());
    }

    public static void setRetryStarted(ITestNGMethod method, ITestContext context) {
        RetryItemContext retryItemContext = getOrInitRetryItemContext(method, context);
        retryItemContext.setStarted(true);
    }

    public static void setRetryFinished(ITestNGMethod method, ITestContext context) {
        RetryItemContext retryItemContext = getOrInitRetryItemContext(method, context);
        retryItemContext.setFinished(true);
    }

    public static boolean isRetryFinished(ITestNGMethod method, ITestContext context) {
        Optional<RetryContext> maybeRetryContext =  getRetryContext(method, context);
        Optional<Map<Integer, RetryItemContext>> maybeRetryItemContexts = maybeRetryContext.map(RetryContext::getRetryItemContexts);
        Optional<RetryItemContext> maybeRetryItemContext = maybeRetryItemContexts.map(retryItemContext -> retryItemContext.get(method.getParameterInvocationCount()));
        return maybeRetryItemContext.map(RetryItemContext::isFinished).orElse(true);
    }

    private static RetryItemContext getOrInitRetryItemContext(ITestNGMethod method, ITestContext context) {
        Optional<RetryContext> maybeRetryContext = getRetryContext(method, context);
        RetryContext retryContext = maybeRetryContext.orElseGet(() -> createRetryContext(method, context));

        Map<Integer, RetryItemContext> retryItemContexts = retryContext.getRetryItemContexts();
        if (retryItemContexts == null) {
            retryItemContexts = Collections.synchronizedMap(new ConcurrentHashMap<>());
            retryContext.setRetryItemContexts(retryItemContexts);
        }

        RetryItemContext retryItemContext = retryItemContexts.get(method.getParameterInvocationCount());
        if (retryItemContext == null) {
            retryItemContext = new RetryItemContext();
            retryItemContexts.put(method.getParameterInvocationCount(), retryItemContext);
        }
        return retryItemContext;
    }

    private static Optional<RetryContext> getRetryContext(ITestNGMethod method, ITestContext context) {
        Optional<TestMethodContext> maybeRerunContext = getRerunContext(method, context);
        return maybeRerunContext.map(TestMethodContext::getRetryContext);
    }

    private static TestMethodContext getOrInitRerunContext(ITestNGMethod method, ITestContext context) {
        Optional<TestMethodContext> maybeRerunContext = getRerunContext(method, context);
        if (maybeRerunContext.isEmpty()) {
            return createEmptyRerunContext(method, context);
        }
        return maybeRerunContext.get();
    }

    private static Optional<TestMethodContext> getRerunContext(ITestNGMethod method, ITestContext context) {
        String uniqueNameByInstanceAndSignature = constructMethodUuid(method);
        return Optional.ofNullable((TestMethodContext) context.getAttribute(uniqueNameByInstanceAndSignature));
    }

    private static TestMethodContext createEmptyRerunContext(ITestNGMethod method, ITestContext context) {
        TestMethodContext testMethodContext = new TestMethodContext();
        setRerunContext(testMethodContext, method, context);
        return testMethodContext;
    }

    private static void setRerunContext(TestMethodContext testMethodContext, ITestNGMethod method, ITestContext context) {
        String uniqueNameByInstanceAndSignature = constructMethodUuid(method);
        context.setAttribute(uniqueNameByInstanceAndSignature, testMethodContext);
    }

    /**
     * Build unique method signature that ties specific method to specific class instance
     * @param method test method
     * @return method uuid in the following format: "fully-qualified-class-name.method-name(argType1,argType2)[instanceNumber]"
     */
    private static String constructMethodUuid(ITestNGMethod method) {
        String pattern = "%s.%s(%s)[%d]";

        ConstructorOrMethod m = method.getConstructorOrMethod();
        String className = method.getTestClass().getName();
        String methodName = m.getName();
        String argumentTypes = Arrays.stream(m.getParameterTypes()).map(Class::getName).collect(Collectors.joining(","));

        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(method);

        return String.format(pattern, className, methodName, argumentTypes, instanceIndex);
    }

    /**
     * Checks if provided test method has corresponding test invocation contexts eligible for rerun
     * @param method test method to be checked
     * @return list of test execution contexts that are eligible for rerun
     */
    public static List<TestInvocationContext> findInvocationsForRerun(ITestNGMethod method) {
        return invocationContexts.stream()
                                 .filter(context -> belongsToMethod(context, method))
                                 .filter(context -> belongsToTheSameFactoryInstance(context, method))
                                 .collect(Collectors.toList());
    }

    private static boolean belongsToTheSameFactoryInstance(TestInvocationContext invocationContext, ITestNGMethod method) {
        int index = FactoryInstanceHolder.getInstanceIndex(method);
        return invocationContext.getInstanceIndex() == index;
    }

    /**
     * Checks if test execution context has the same method signature as test method provided
     */
    private static boolean belongsToMethod(TestInvocationContext invocationContext, ITestNGMethod method) {
        return invocationContext.getClassName().equals(method.getTestClass().getName())
                && invocationContext.getMethodName().equals(method.getMethodName())
                && getMethodParameterNamesAsString(invocationContext.getParameterClassNames()).equals(getMethodParameterNamesAsString(method));
    }

    public static int getOriginDataProviderIndex(int newIndex, ITestNGMethod method, ITestContext context) {
        Optional<TestMethodContext> maybeRerunContext = getRerunContext(method, context);
        return maybeRerunContext.map(testMethodContext ->
                IntStream.range(0, testMethodContext.getOriginalDataProviderIndices().size())
                         .filter(index -> index == newIndex)
                         .findFirst()
                         .orElse(-1)
        ).orElse(-1);
    }

    static void setInvocationContexts(List<TestInvocationContext> invocationContexts) {
        RunContextService.invocationContexts = invocationContexts;
    }

    private static String getMethodParameterNamesAsString(ITestNGMethod method) {
        List<String> methodParameterNames = getMethodParameterNames(method);
        return getMethodParameterNamesAsString(methodParameterNames);
    }

    private static List<String> getMethodParameterNames(ITestNGMethod method) {
        return Arrays.stream(method.getConstructorOrMethod().getParameterTypes())
                     .map(Class::getName)
                     .collect(Collectors.toList());
    }

    private static String getMethodParameterNamesAsString(List<String> methodParameterNames) {
        return String.join(", ", methodParameterNames);
    }

}
