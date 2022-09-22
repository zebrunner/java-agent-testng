package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.core.listener.RerunListener;
import com.zebrunner.agent.core.registrar.RerunService;
import com.zebrunner.agent.core.registrar.RunContextHolder;
import com.zebrunner.agent.core.registrar.domain.TestDTO;
import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.TestInvocationContext;
import com.zebrunner.agent.testng.core.method.DependantMethodResolver;
import com.zebrunner.agent.testng.core.retry.RetryAnalyzerInterceptor;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RerunAwareListener implements RerunListener, IMethodInterceptor {

    @Override
    public void onRerun(List<TestDTO> tests) {
        Map<TestInvocationContext, Long> invocationContexts = getInvocationContexts(tests);
        RunContextService.addInvocationContexts(invocationContexts);
    }

    /**
     * Processes test correlation data in order to restore original test execution context for appropriate test
     *
     * @param testDTOs tests
     * @return collection of test execution contexts
     */
    private Map<TestInvocationContext, Long> getInvocationContexts(List<TestDTO> testDTOs) {
        return testDTOs.stream()
                       .collect(Collectors.toMap(
                               test -> TestInvocationContext.fromJsonString(test.getCorrelationData()),
                               TestDTO::getId,
                               (testId1, testId2) -> testId1
                       ));
    }

    /**
     * Used to alter TestNG test run plan - before every test method invocation this interceptor will be called to check
     * if this test method is present in rerun plan. If method is not present or does not depends on method in rerun plan -
     * it will be dropped from test run plan.
     * <p>
     * This interceptor is executed every time test method is discovered.
     *
     * @param methods initial set of test methods discovered by TestNG for this test run
     * @param context test context
     * @return list of methods for rerun by TestNG
     */
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        // Collect factory instances to resolve a sequence and mark run items with sequence index
        TestRunner runner = (TestRunner) context;
        FactoryInstanceHolder.registerInstances(runner.getTestClasses());
        methods.forEach(methodInstance -> this.addRetryInterceptor(methodInstance.getMethod(), context));

        if (!RunContextHolder.isRerun()) {
            return methods;
        }

        Set<IMethodInstance> actualMethodsForRerun = this.getMethodsForRerun(methods);
        if (RunContextService.countInvocationContexts() < actualMethodsForRerun.size()) {
            List<TestDTO> tests = RerunService.retrieveFullExecutionContextTests();
            Map<TestInvocationContext, Long> invocationContexts = getInvocationContexts(tests);
            RunContextService.addInvocationContexts(invocationContexts);
        }

        actualMethodsForRerun.forEach(methodInstance -> this.setDataProviderForRerun(methodInstance.getMethod(), runner));

        // We must have the same execution order as it was before manipulations.
        return methods.stream()
                      .filter(actualMethodsForRerun::contains)
                      .collect(Collectors.toList());
    }

    /**
     * If test method has a retry analyser - register analyser interceptor to keep track of retry count
     *
     * @param context test context
     * @param method  test method
     */
    private void addRetryInterceptor(ITestNGMethod method, ITestContext context) {
        Class<? extends IRetryAnalyzer> retryAnalyser = method.getRetryAnalyzerClass();
        if (retryAnalyser != null && !retryAnalyser.isAssignableFrom(RetryAnalyzerInterceptor.class)) {
            RetryService.setRetryAnalyzerClass(retryAnalyser, context, method);
            method.setRetryAnalyzerClass(RetryAnalyzerInterceptor.class);
        }
    }

    private Set<IMethodInstance> getMethodsForRerun(List<IMethodInstance> methods) {
        Set<IMethodInstance> methodsForRerun = methods.stream()
                                                      .filter(instance -> RunContextService.isEligibleForRerun(instance.getMethod()))
                                                      .collect(Collectors.toSet());

        if (methodsForRerun.isEmpty()) {
            return Collections.emptySet();
        }

        Set<IMethodInstance> dependantMethods = DependantMethodResolver.resolve(methods, methodsForRerun);
        methodsForRerun.addAll(dependantMethods);

        return methodsForRerun;
    }

    private void setDataProviderForRerun(ITestNGMethod method, ITestContext context) {
        List<TestInvocationContext> invocationContexts = RunContextService.findInvocationsForRerun(method);
        Set<Integer> indicesForRerun = invocationContexts.stream()
                                                         .map(TestInvocationContext::getDataProviderIndex)
                                                         .filter(index -> index != -1)
                                                         .collect(Collectors.toSet());
        RunContextService.setDataProviderIndicesForRerun(method, context, indicesForRerun);
    }

}
