package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.core.listener.RerunListener;
import com.zebrunner.agent.core.registrar.RerunContextHolder;
import com.zebrunner.agent.core.registrar.domain.TestDTO;
import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.TestInvocationContext;
import com.zebrunner.agent.testng.core.retry.RetryAnalyzerInterceptor;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RerunAwareListener implements RerunListener, IMethodInterceptor {

    @Override
    public void onRerun(List<TestDTO> tests) {
        Map<TestInvocationContext, Long> invocationContexts = getInvocationContexts(tests);
        RunContextService.setInvocationContextToTestIds(invocationContexts);
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

        if (!methods.isEmpty()) {

            Set<IMethodInstance> methodsToSkipOnRerun = new HashSet<>();
            Set<String> dependantMethods = new HashSet<>(); // dependant methods
            Set<String> dependantGroups = new HashSet<>(); // dependant groups

            for (IMethodInstance methodInstance : methods) {
                ITestNGMethod method = methodInstance.getMethod();

                // proxy retry analyzer class to provide possibility to handle retry invocations count
                Class<? extends IRetryAnalyzer> retryAnalyser = method.getRetryAnalyzerClass();
                addRetryInterceptor(context, method, retryAnalyser);

                if (RerunContextHolder.isRerun()) {

                    List<TestInvocationContext> invocationsForRerun = RunContextService.findInvocationsForRerun(method);
                    if (!invocationsForRerun.isEmpty()) {

                        // Collect dependant methods from tests needed to rerun. Only first hierarchy methods is tracking
                        Set<String> dependUponMethodsFromItem = collectDependantMethods(method);
                        Set<String> dependUponGroupsFromItem = collectDependantGroups(method);
                        dependantMethods.addAll(dependUponMethodsFromItem);
                        dependantGroups.addAll(dependUponGroupsFromItem);

                        // Collect data providers line to rerun
                        collectDataProvidersForRerun(invocationsForRerun, method, runner);
                    } else {
                        // If method is not needed to rerun (according first hierarchy dependant methods logic)
                        methodsToSkipOnRerun.add(methodInstance);
                    }
                }
            }

            resolveDependantMethods(methods, dependantMethods, dependantGroups, methodsToSkipOnRerun);

            methods.removeAll(methodsToSkipOnRerun);
        }

        return methods;
    }

    /**
     * If test method has a retry analyser - register analyser interceptor to keep track of retry count
     *
     * @param context       test context
     * @param method        test method
     * @param retryAnalyser test method's retry analyser
     */
    private void addRetryInterceptor(ITestContext context, ITestNGMethod method, Class<? extends IRetryAnalyzer> retryAnalyser) {
        if (retryAnalyser != null && !retryAnalyser.isAssignableFrom(RetryAnalyzerInterceptor.class)) {
            RetryService.setRetryAnalyzerClass(retryAnalyser, context, method);
            method.setRetryAnalyzerClass(RetryAnalyzerInterceptor.class);
        }
    }

    /**
     * Finds all dependant methods (and their own dependants, if any) and remove their from methodsToSkipOnRerun and dependantNames
     *
     * @param methods              methods discovered by TestNG for this test run
     * @param dependantMethods     dependant method names
     * @param dependantGroups      dependant group names
     * @param methodsToSkipOnRerun initial set of methods to skip on rerun, that can be altered if contains dependant methods
     */
    private void resolveDependantMethods(List<IMethodInstance> methods,
                                         Set<String> dependantMethods,
                                         Set<String> dependantGroups,
                                         Set<IMethodInstance> methodsToSkipOnRerun) {
        for (IMethodInstance methodInstance : methods) {
            ITestNGMethod method = methodInstance.getMethod();

            boolean isMethodDependingUpon = isMethodDependingUpon(dependantMethods, method);
            boolean isGroupDependingUpon = isGroupDependingUpon(dependantGroups, method);

            if (isMethodDependingUpon || isGroupDependingUpon) {
                methodsToSkipOnRerun.remove(methodInstance);

                if (isMethodDependingUpon) {
                    String dependsUponMethodKey = buildDependsUponMethodKey(method);
                    dependantMethods.remove(dependsUponMethodKey);

                    Set<String> dependUponMethodsFromItem = collectDependantMethods(method);
                    dependantMethods.addAll(dependUponMethodsFromItem);
                }

                if (isGroupDependingUpon) {
                    this.filterDependantGroups(dependantGroups, method)
                        .forEach(dependantGroups::remove);

                    Set<String> dependUponGroupsFromItem = collectDependantGroups(method);
                    dependantGroups.addAll(dependUponGroupsFromItem);
                }
            }
        }
    }

    private boolean isMethodDependingUpon(Set<String> dependUponMethods, ITestNGMethod method) {
        String dependsUponMethodKey = buildDependsUponMethodKey(method);
        return dependUponMethods.contains(dependsUponMethodKey);
    }

    private boolean isGroupDependingUpon(Set<String> dependUponGroups, ITestNGMethod method) {
        return filterDependantGroups(dependUponGroups, method)
                .findFirst()
                .isPresent();
    }

    private Stream<String> filterDependantGroups(Set<String> dependantGroups, ITestNGMethod method) {
        return Arrays.stream(method.getGroups())
                     .map(groupName -> buildDependsUponGroupKey(groupName, method))
                     .filter(dependantGroups::contains);
    }

    private String buildDependsUponMethodKey(ITestNGMethod method) {
        String methodPath = method.getTestClass().getName() + "." + method.getMethodName();
        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(method);
        return buildDependsUponKey(methodPath, instanceIndex);
    }

    private String buildDependsUponGroupKey(String groupName, ITestNGMethod method) {
        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(method);
        return buildDependsUponKey(groupName, instanceIndex);
    }

    private Set<String> collectDependantMethods(ITestNGMethod method) {
        Set<String> dependantMethods = new HashSet<>(Arrays.asList(method.getMethodsDependedUpon()));
        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(method);
        return dependantMethods.stream()
                               .map(dependsUponMethod -> buildDependsUponKey(dependsUponMethod, instanceIndex))
                               .collect(Collectors.toSet());
    }

    private Set<String> collectDependantGroups(ITestNGMethod method) {
        Set<String> dependantGroups = new HashSet<>(Arrays.asList(method.getGroupsDependedUpon()));
        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(method);
        return dependantGroups.stream()
                              .map(dependsUponGroup -> buildDependsUponKey(dependsUponGroup, instanceIndex))
                              .collect(Collectors.toSet());
    }

    private String buildDependsUponKey(String name, long instanceHashCode) {
        return String.format("%d:%s", instanceHashCode, name);
    }

    private void collectDataProvidersForRerun(List<TestInvocationContext> invocationContexts, ITestNGMethod method, ITestContext context) {
        Set<Integer> indicesForRerun = invocationContexts.stream()
                                                         .map(TestInvocationContext::getDataProviderIndex)
                                                         .filter(index -> index != -1)
                                                         .collect(Collectors.toSet());
        RunContextService.setDataProviderIndicesForRerun(method, context, indicesForRerun);
    }

}
