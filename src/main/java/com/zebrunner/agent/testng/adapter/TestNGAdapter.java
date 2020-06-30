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
import com.zebrunner.agent.testng.listener.RunContextService;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.TestNGMethod;
import org.testng.xml.XmlSuite;

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

            long startedAtMillis = testResult.getStartMillis();
            OffsetDateTime startedAt = ofMillis(startedAtMillis);

            ITestNGMethod testMethod = testResult.getMethod();
            ConstructorOrMethod testConstructorOrMethod = testMethod.getConstructorOrMethod();

            TestInvocationContext testContext = buildUuid(testResult);
            if (RerunContextHolder.isRerun()) {
                recognizeTestContextOnRerun(testContext, testMethod, testResult.getTestContext());
            }

            String uuid = testContext.asJsonString();
            String displayName = testContext.buildUniqueDisplayName();
            String maintainer = rootXmlSuite.getParameter("maintainer");

            Class<?> testClass = testResult.getTestClass().getRealClass();
            TestStartDescriptor testStartDescriptor = new TestStartDescriptor(
                    uuid, displayName, startedAt, maintainer, testClass, testConstructorOrMethod.getMethod()
            );

            String id = generateTestId(testContext);
            registrar.startTest(id, testStartDescriptor);
        }
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
        TestNGMethod testMethod = (TestNGMethod) testResult.getMethod();

        String displayName;
        List<String> parameterClassNames;
        int dataProviderLineIndex = -1;

        displayName = testMethod.getConstructorOrMethod()
                                .getMethod()
                                .getAnnotation(org.testng.annotations.Test.class)
                                .testName();

        parameterClassNames = Arrays.stream(testMethod.getConstructorOrMethod().getParameterTypes())
                                    .map(Class::getName)
                                    .collect(Collectors.toList());

        if (testMethod.isDataDriven()) {
            dataProviderLineIndex = ((org.testng.internal.TestResult) testResult).getParameterIndex();
        }

        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(testMethod);

        int invocationCount = RunContextService.getMethodInvocationCount(testMethod, testResult.getTestContext());

        return TestInvocationContext.builder()
                                    .className(testMethod.getTestClass().getName())
                                    .methodName(testMethod.getMethodName())
                                    .displayName(displayName)
                                    .parameterClassNames(parameterClassNames)
                                    .dataProviderLineIndex(dataProviderLineIndex)
                                    .instanceIndex(instanceIndex)
                                    .invocationIndex(invocationCount)
                                    .build();
    }

    private boolean isRetryFinished(ITestNGMethod method, ITestContext context) {
        return RunContextService.isRetryFinished(method, context);
    }

    private boolean wasRetry(ITestNGMethod method, ITestContext context) {
        return !RunContextService.getRetryFailureReasons(method, context).isEmpty();
    }

    private String collectRetryMessages(ITestNGMethod method, ITestContext context) {
        StringBuilder message = new StringBuilder();
        if (wasRetry(method, context)) {
            Map<Integer, String> failureReasons = RunContextService.getRetryFailureReasons(method, context);

            failureReasons.forEach((index, msg) -> message.append("Retry index is ")
                                                          .append(index)
                                                          .append("\n")
                                                          .append(msg));
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
