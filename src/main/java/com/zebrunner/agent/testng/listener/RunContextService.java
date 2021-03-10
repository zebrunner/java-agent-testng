package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.TestInvocationContext;
import com.zebrunner.agent.testng.core.TestMethodContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.internal.ConstructorOrMethod;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RunContextService {

    private static Map<TestInvocationContext, Long> invocationContextToTestIds = Collections.emptyMap();

    static void setInvocationContextToTestIds(Map<TestInvocationContext, Long> invocationContextToTestIds) {
        RunContextService.invocationContextToTestIds = invocationContextToTestIds;
    }

    public static void incrementMethodInvocationIndex(ITestNGMethod method, ITestContext context) {
        RunContextService.getOrInitRunContext(method, context)
                         .incrementInvocationIndex();
    }

    public static int getMethodInvocationIndex(ITestNGMethod method, ITestContext context) {
        return RunContextService.getOrInitRunContext(method, context)
                                .getCurrentInvocationIndex();
    }

    public static void setDataProviderIndicesForRerun(ITestNGMethod method, ITestContext context, Collection<Integer> indices) {
        RunContextService.getOrInitRunContext(method, context)
                         .setDataProviderIndicesForRerun(indices);
    }

    public static List<Integer> getDataProviderIndicesForRerun(ITestNGMethod method, ITestContext context) {
        return RunContextService.getMethodContext(method, context)
                                .map(TestMethodContext::getDataProviderIndicesForRerun)
                                .orElseGet(Collections::emptyList);
    }

    public static int getDataProviderSize(ITestNGMethod method, ITestContext context) {
        return RunContextService.getOrInitRunContext(method, context)
                                .getDataProviderSize();
    }

    public static void setDataProviderData(ITestNGMethod method, ITestContext context, List<Object[]> dataProviderData) {
        RunContextService.getOrInitRunContext(method, context)
                         .setDataProviderData(dataProviderData);
    }

    public static void setCurrentDataProviderIteratorIndex(ITestNGMethod method, ITestContext context, int currentDataProviderIteratorIndex) {
        RunContextService.getOrInitRunContext(method, context)
                         .setCurrentDataProviderIteratorIndex(currentDataProviderIteratorIndex);
    }

    public static int getCurrentDataProviderIndex(ITestNGMethod method, ITestContext context, Object[] parameters) {
        return RunContextService.getMethodContext(method, context)
                                .map(testMethodContext -> testMethodContext.getCurrentDataProviderIndex(parameters))
                                .orElse(-1);
    }

    private static TestMethodContext getOrInitRunContext(ITestNGMethod method, ITestContext context) {
        return RunContextService.getMethodContext(method, context)
                                .orElseGet(() -> createEmptyRunContext(method, context));
    }

    private static Optional<TestMethodContext> getMethodContext(ITestNGMethod method, ITestContext context) {
        String uniqueNameByInstanceAndSignature = constructMethodUuid(method);
        return Optional.ofNullable((TestMethodContext) context.getAttribute(uniqueNameByInstanceAndSignature));
    }

    private static TestMethodContext createEmptyRunContext(ITestNGMethod method, ITestContext context) {
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
        String pattern = "%s.%s(%s)[%d]";
        ConstructorOrMethod constructorOrMethod = method.getConstructorOrMethod();

        String className = method.getTestClass().getName();
        String methodName = constructorOrMethod.getName();
        String argumentTypes = Arrays.stream(constructorOrMethod.getParameterTypes())
                                     .map(Class::getName)
                                     .collect(Collectors.joining(","));
        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(method);

        return String.format(pattern, className, methodName, argumentTypes, instanceIndex);
    }

    /**
     * Checks if provided test method has corresponding test invocation contexts eligible for rerun
     *
     * @param method test method to be checked
     * @return list of test execution contexts that are eligible for rerun
     */
    public static List<TestInvocationContext> findInvocationsForRerun(ITestNGMethod method) {
        return invocationContextToTestIds.keySet()
                                         .stream()
                                         .filter(Objects::nonNull)
                                         .filter(context -> belongsToMethod(context, method))
                                         .filter(context -> belongsToTheSameFactoryInstance(context, method))
                                         .collect(Collectors.toList());
    }

    public static Optional<Long> getZebrunnerTestIdOnRerun(ITestNGMethod method, Integer dataProviderIndex) {
        return invocationContextToTestIds.keySet()
                                         .stream()
                                         .filter(Objects::nonNull)
                                         .filter(context -> belongsToMethod(context, method))
                                         .filter(context -> belongsToTheSameFactoryInstance(context, method))
                                         .filter(context -> context.getDataProviderIndex() == dataProviderIndex)
                                         .map(invocationContextToTestIds::get)
                                         .findFirst();
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

}
