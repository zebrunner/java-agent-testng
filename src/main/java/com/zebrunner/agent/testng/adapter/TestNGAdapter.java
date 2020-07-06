package com.zebrunner.agent.testng.adapter;

import com.zebrunner.agent.core.registrar.RerunContextHolder;
import com.zebrunner.agent.core.registrar.Status;
import com.zebrunner.agent.core.registrar.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.TestRunRegistrar;
import com.zebrunner.agent.core.registrar.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.TestStartDescriptor;
import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.TestInvocationContext;
import com.zebrunner.agent.testng.listener.RetryService;
import com.zebrunner.agent.testng.listener.RunContextService;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.internal.ConfigurationMethod;
import org.testng.internal.TestResult;
import org.testng.internal.thread.ThreadUtil;
import org.testng.xml.XmlSuite;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter used to convert TestNG test domain to Zebrunner Agent domain
 */
public class TestNGAdapter {

    private final TestRunRegistrar registrar;

    private XmlSuite rootXmlSuite;

    public TestNGAdapter() {
        this.registrar = TestRunRegistrar.registrar();
    }

    public void registerRunOrSuiteStart(ISuite suite) {
        if (rootXmlSuite == null) {
            XmlSuite xmlSuite = suite.getXmlSuite();

            XmlSuite parentXmlSuite = xmlSuite.getParentSuite() != null ? xmlSuite.getParentSuite() : xmlSuite;
            rootXmlSuite = parentXmlSuite;
            while (parentXmlSuite != null) {
                parentXmlSuite = rootXmlSuite.getParentSuite();
                rootXmlSuite = parentXmlSuite != null ? parentXmlSuite : rootXmlSuite;
            }
            String name = rootXmlSuite.getName();

            registrar.start(new TestRunStartDescriptor(name, "testng", OffsetDateTime.now(), name));
        }
    }

    public void registerRunFinish(ISuite suite) {
        if (suite.getXmlSuite().getParentSuite() == null) {
            registrar.finish(new TestRunFinishDescriptor(OffsetDateTime.now()));
        }
    }

    public void registerTestStart(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {

            RunContextService.incrementMethodInvocationCount(testResult.getMethod(), testResult.getTestContext());

            TestInvocationContext testContext = buildTestInvocationContext(testResult);
            TestStartDescriptor testStartDescriptor = buildTestStartDescriptor(testResult, testContext);

            String id = generateTestId(testContext);
            registrar.startTest(id, testStartDescriptor);
        }
    }

    public void registerHeadlessTestStart(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {
            ITestNGMethod testToExecute = getNextInvokedTest(testResult);
            if (testToExecute != null) {
                RunContextService.setHeadlessWasExecuted(testToExecute, testResult.getTestContext());

                TestInvocationContext testContext = buildHeadlessTestInvocationContext(testToExecute, testResult
                        .getTestContext());
                TestStartDescriptor testStartDescriptor = buildHeadlessTestStartDescriptor(testResult, testContext);

                String id = generateTestId(testContext);
                registrar.startHeadlessTest(id, testStartDescriptor);
            }
        }
    }

    /**
     * Finds next method to execute according configuration methods
     *
     * @param tr to check
     * @return next method to execute or null if operation does not supported
     */
    private ITestNGMethod getNextInvokedTest(ITestResult tr) {
        ITestNGMethod result = null;
        if (tr.getMethod() instanceof ConfigurationMethod) {
            ConfigurationMethod configurationMethod = (ConfigurationMethod) tr.getMethod();
            if (configurationMethod.isBeforeMethodConfiguration()) {
                ITestNGMethod[] methods = tr.getTestContext().getAllTestMethods();
                List<ITestNGMethod> candidates = Arrays.stream(methods)
                                                       .filter(this::fromCurrentThread)
                                                       .filter(method -> isPreparedToRun(method, tr.getTestContext()))
                                                       .collect(Collectors.toList());

                if (candidates.size() == 1) {
                    result = candidates.get(0);
                }
            }
        }
        return result;
    }

    /**
     * Indicated that method was prepared and will be executed next
     *
     * @param method  to check
     * @param context to check
     * @return true if method will be executed next
     */
    private boolean isPreparedToRun(ITestNGMethod method, ITestContext context) {
        int currentInvocationCount = method.getCurrentInvocationCount();
        boolean methodWasCompletedOrPreparedToStart = methodWasCompletedOrPreparedToStart(method);
        boolean notCompleted = method.isDataDriven() ? currentInvocationCount < RunContextService
                .getDataProviderSize(method, context)
                : currentInvocationCount == 0;
        return methodWasCompletedOrPreparedToStart && notCompleted;
    }

    private boolean fromCurrentThread(ITestNGMethod method) {
        return method.getId().equals(ThreadUtil.currentThreadInfo());
    }

    /**
     * Indicates that method was completed or was prepared to run.
     * Not prepared or not executed methods don't have id parameter
     *
     * @param method to check
     * @return true if method was executed or prepared to run
     */
    private boolean methodWasCompletedOrPreparedToStart(ITestNGMethod method) {
        return method.getId() != null && !method.getId().isBlank();
    }

    private TestStartDescriptor buildTestStartDescriptor(ITestResult testResult, TestInvocationContext testContext) {
        String uuid = testContext.asJsonString();
        String displayName = testContext.buildUniqueDisplayName();
        return buildTestStartDescriptor(uuid, testResult, displayName);
    }

    private TestStartDescriptor buildHeadlessTestStartDescriptor(ITestResult testResult, TestInvocationContext testContext) {
        String uuid = testContext.asJsonString();
        testContext.setMethodName(testResult.getMethod().getMethodName());
        String displayName = testContext.buildUniqueDisplayName();
        return buildTestStartDescriptor(uuid, testResult, displayName);
    }

    private TestStartDescriptor buildTestStartDescriptor(String uuid, ITestResult testResult, String displayName) {
        long startedAtMillis = testResult.getStartMillis();
        OffsetDateTime startedAt = ofMillis(startedAtMillis);

        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        Class<?> realClass = testResult.getTestClass().getRealClass();
        String maintainer = rootXmlSuite.getParameter("maintainer");

        return new TestStartDescriptor(uuid, displayName, startedAt, maintainer, realClass, method);
    }

    private TestInvocationContext buildTestInvocationContext(ITestResult testResult) {
        TestInvocationContext testContext = buildUuid(testResult);
        return buildTestInvocationContext(testContext, testResult.getMethod(), testResult.getTestContext());
    }

    private TestInvocationContext buildHeadlessTestInvocationContext(ITestNGMethod testMethod, ITestContext context) {
        int dataProviderLineIndex = RunContextService.getDataProviderCurrentIndex(testMethod, context);
        int invocationCount = RunContextService.getMethodInvocationCount(testMethod, context);
        if (RunContextService.isHeadlessWasExecuted(testMethod, context)) {
            invocationCount++;
        }
        TestInvocationContext testContext = buildUuid(testMethod, dataProviderLineIndex, invocationCount);
        return buildTestInvocationContext(testContext, testMethod, context);
    }

    private TestInvocationContext buildTestInvocationContext(TestInvocationContext testContext, ITestNGMethod testMethod, ITestContext context) {
        if (RerunContextHolder.isRerun()) {
            recognizeTestContextOnRerun(testContext, testMethod, context);
        }
        return testContext;
    }

    public void registerTestFinish(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {

            long endedAtMillis = testResult.getEndMillis();
            OffsetDateTime endedAt = ofMillis(endedAtMillis);

            String retryMessage = collectRetryMessages(testResult.getMethod(), testResult.getTestContext());

            TestFinishDescriptor testFinishDescriptor = new TestFinishDescriptor(Status.PASSED, endedAt, retryMessage);

            TestInvocationContext testContext = buildUuid(testResult);
            String id = generateTestId(testContext);
            registrar.finishTest(id, testFinishDescriptor);
        }
    }

    public void registerFailedTestFinish(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {

            TestInvocationContext testContext = buildUuid(testResult);
            String id = generateTestId(testContext);

            if (!registrar.isTestStarted(id)) {
                registerTestStart(testResult);
            }

            long endedAtMillis = testResult.getEndMillis();
            OffsetDateTime endedAt = ofMillis(endedAtMillis);

            String message = wasRetry(testResult.getMethod(), testResult.getTestContext())
                    ? collectRetryMessages(testResult.getMethod(), testResult.getTestContext())
                    : testResult.getThrowable().getMessage();

            TestFinishDescriptor result = new TestFinishDescriptor(Status.FAILED, endedAt, message);
            registrar.finishTest(id, result);
        }
    }

    public void registerSkippedTestFinish(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {

            long endedAtMillis = testResult.getEndMillis();
            OffsetDateTime endedAt = ofMillis(endedAtMillis);

            TestInvocationContext testContext = buildUuid(testResult);

            String message;
            if (wasRetry(testResult.getMethod(), testResult.getTestContext())) {
                message = collectRetryMessages(testResult.getMethod(), testResult.getTestContext());
            } else {
                message = testResult.getThrowable().getMessage();
            }

            TestFinishDescriptor result = new TestFinishDescriptor(Status.SKIPPED, endedAt, message);
            String id = generateTestId(testContext);
            registrar.finishTest(id, result);
        }
    }

    private void recognizeTestContextOnRerun(TestInvocationContext currentRunContext, ITestNGMethod method, ITestContext context) {
        int dataProviderLineIndex = currentRunContext.getDataProviderLineIndex();
        boolean isDataDriven = dataProviderLineIndex != -1;
        if (isDataDriven) {
            int originalIndex = RunContextService.getOriginDataProviderIndex(dataProviderLineIndex, method, context);
            if (originalIndex != -1) {
                currentRunContext.setDataProviderLineIndex(originalIndex);
            }
        }
    }

    private TestInvocationContext buildUuid(ITestResult testResult) {
        ITestNGMethod testMethod = testResult.getMethod();
        int dataProviderLineIndex = ((TestResult) testResult).getParameterIndex();
        int invocationCount = RunContextService.getMethodInvocationCount(testMethod, testResult.getTestContext());
        return buildUuid(testMethod, dataProviderLineIndex, invocationCount);
    }

    private TestInvocationContext buildUuid(ITestNGMethod testMethod, int dataProviderLineIndex, int invocationCount) {
        String displayName = null;
        List<String> parameterClassNames;
        int lineIndex = testMethod.isDataDriven() ? dataProviderLineIndex : -1;

        Test testAnnotation = testMethod.getConstructorOrMethod()
                                        .getMethod()
                                        .getAnnotation(org.testng.annotations.Test.class);
        if (testAnnotation != null) {
            displayName = testAnnotation.testName();
        }

        parameterClassNames = Arrays.stream(testMethod.getConstructorOrMethod().getParameterTypes())
                                    .map(Class::getName)
                                    .collect(Collectors.toList());

        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(testMethod);

        return TestInvocationContext.builder()
                                    .className(testMethod.getTestClass().getName())
                                    .methodName(testMethod.getMethodName())
                                    .displayName(displayName)
                                    .parameterClassNames(parameterClassNames)
                                    .dataProviderLineIndex(lineIndex)
                                    .instanceIndex(instanceIndex)
                                    .invocationIndex(invocationCount)
                                    .build();
    }

    private boolean isRetryFinished(ITestNGMethod method, ITestContext context) {
        return RetryService.isRetryFinished(method, context);
    }

    private boolean wasRetry(ITestNGMethod method, ITestContext context) {
        return !RetryService.getRetryFailureReasons(method, context).isEmpty();
    }

    private String collectRetryMessages(ITestNGMethod method, ITestContext context) {
        StringBuilder message = new StringBuilder();
        if (wasRetry(method, context)) {
            Map<Integer, String> failureReasons = RetryService.getRetryFailureReasons(method, context);

            failureReasons.forEach((index, msg) -> message.append("Retry index is ")
                                                          .append(index)
                                                          .append("\n")
                                                          .append(msg)
                                                          .append("\n"));
        }
        return message.toString();
    }

    private String generateTestId(TestInvocationContext testInvocationContext) {
        return testInvocationContext.toString();
    }

    private OffsetDateTime ofMillis(long epochMillis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

}
