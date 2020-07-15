package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.TestInvocationContext;
import com.zebrunner.agent.testng.core.TestMethodContext;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.internal.ConstructorOrMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RunContextService {

    private static List<TestInvocationContext> invocationContexts;

    private RunContextService() {
    }

    static void setInvocationContexts(List<TestInvocationContext> invocationContexts) {
        RunContextService.invocationContexts = invocationContexts;
    }

    public static void incrementMethodInvocationCount(ITestNGMethod method, ITestContext context) {
        getOrInitRerunContext(method, context)
                .incrementInvocationCount();
    }

    public static int getMethodInvocationCount(ITestNGMethod method, ITestContext context) {
        return getOrInitRerunContext(method, context)
                .getCurrentInvocationCount();
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
        return getRerunContext(method, context)
                .map(TestMethodContext::getOriginalDataProviderIndices)
                .orElseGet(Collections::emptySet);
    }

    public static void setDataProviderSize(ITestNGMethod method, ITestContext context, int size) {
        getOrInitRerunContext(method, context)
                .setDataProviderSize(size);
    }

    public static void setDataProviderCurrentIndex(ITestNGMethod method, ITestContext context, int index) {
        getOrInitRerunContext(method, context)
                .setDataProviderCurrentIndex(index);
    }

    public static int getDataProviderCurrentIndex(ITestNGMethod method, ITestContext context) {
        return getRerunContext(method, context)
                .map(TestMethodContext::getDataProviderCurrentIndex)
                .orElse(-1);
    }

    public static void setForceRerun(ITestNGMethod method, ITestContext context) {
        getOrInitRerunContext(method, context)
                .setForceRerun(true);
    }

    public static boolean isForceRerun(ITestNGMethod method, ITestContext context) {
        return getRerunContext(method, context)
                .map(TestMethodContext::isForceRerun)
                .orElse(false);
    }

    private static TestMethodContext getOrInitRerunContext(ITestNGMethod method, ITestContext context) {
        return getRerunContext(method, context)
                .orElseGet(() -> createEmptyRerunContext(method, context));
    }

    private static Optional<TestMethodContext> getRerunContext(ITestNGMethod method, ITestContext context) {
        String uniqueNameByInstanceAndSignature = constructMethodUuid(method);
        return Optional.ofNullable((TestMethodContext) context.getAttribute(uniqueNameByInstanceAndSignature));
    }

    private static TestMethodContext createEmptyRerunContext(ITestNGMethod method, ITestContext context) {
        TestMethodContext testMethodContext = new TestMethodContext();
        String uniqueNameByInstanceAndSignature = constructMethodUuid(method);

        context.setAttribute(uniqueNameByInstanceAndSignature, testMethodContext);
        return testMethodContext;
    }

    /**
     * Build unique method signature that ties specific method to specific class instance
     *
     * @param method test method
     * @return method uuid in the following format: "fully-qualified-class-name.method-name(argType1,argType2)[instanceNumber]"
     */
    private static String constructMethodUuid(ITestNGMethod method) {
        String pattern = "[%s]: %s.%s(%s)[%d]";
        ConstructorOrMethod constructorOrMethod = method.getConstructorOrMethod();

        String thread = Thread.currentThread().getName();
        String className = method.getTestClass().getName();
        String methodName = constructorOrMethod.getName();
        String argumentTypes = Arrays.stream(constructorOrMethod.getParameterTypes())
                                     .map(Class::getName)
                                     .collect(Collectors.joining(","));
        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(method);

        return String.format(pattern, thread, className, methodName, argumentTypes, instanceIndex);
    }

    /**
     * Checks if provided test method has corresponding test invocation contexts eligible for rerun
     *
     * @param method test method to be checked
     * @return list of test execution contexts that are eligible for rerun
     */
    public static List<TestInvocationContext> findInvocationsForRerun(ITestNGMethod method) {
        return invocationContexts.stream()
                                 .filter(context -> belongsToMethod(context, method))
                                 .filter(context -> belongsToTheSameFactoryInstance(context, method))
                                 .collect(Collectors.toList());
    }

    /**
     * Checks if test execution context has the same method signature as test method provided
     */
    private static boolean belongsToMethod(TestInvocationContext invocationContext, ITestNGMethod method) {
        String contextParameters = String.join(", ", invocationContext.getParameterClassNames());
        String methodParameters = String.join(", ", getMethodParameterNames(method));

        return invocationContext.getClassName().equals(method.getTestClass().getName())
                && invocationContext.getMethodName().equals(method.getMethodName())
                && contextParameters.equals(methodParameters);
    }

    private static List<String> getMethodParameterNames(ITestNGMethod method) {
        return Arrays.stream(method.getConstructorOrMethod().getParameterTypes())
                     .map(Class::getName)
                     .collect(Collectors.toList());
    }

    private static boolean belongsToTheSameFactoryInstance(TestInvocationContext invocationContext, ITestNGMethod method) {
        int index = FactoryInstanceHolder.getInstanceIndex(method);
        return invocationContext.getInstanceIndex() == index;
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

}
