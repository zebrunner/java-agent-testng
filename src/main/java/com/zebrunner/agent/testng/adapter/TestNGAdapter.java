package com.zebrunner.agent.testng.adapter;

import com.zebrunner.agent.core.registrar.RerunContextHolder;
import com.zebrunner.agent.core.registrar.TestRunRegistrar;
import com.zebrunner.agent.core.registrar.descriptor.Status;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.core.registrar.maintainer.ChainedMaintainerResolver;
import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.RootXmlSuiteMaintainerResolver;
import com.zebrunner.agent.testng.core.TestInvocationContext;
import com.zebrunner.agent.testng.core.testname.TestNameResolver;
import com.zebrunner.agent.testng.core.testname.TestNameResolverRegistry;
import com.zebrunner.agent.testng.listener.RetryService;
import com.zebrunner.agent.testng.listener.RunContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadUtil;
import org.testng.xml.XmlSuite;

import java.lang.invoke.MethodHandles;
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

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TestRunRegistrar registrar;

    private XmlSuite rootXmlSuite;

    public TestNGAdapter() {
        this.registrar = TestRunRegistrar.getInstance();
    }

    public void registerRunStart(ISuite suite) {
        if (rootXmlSuite == null) {
            XmlSuite xmlSuite = suite.getXmlSuite();

            XmlSuite parentXmlSuite = xmlSuite.getParentSuite() != null ? xmlSuite.getParentSuite() : xmlSuite;
            rootXmlSuite = parentXmlSuite;
            while (parentXmlSuite != null) {
                parentXmlSuite = rootXmlSuite.getParentSuite();
                rootXmlSuite = parentXmlSuite != null ? parentXmlSuite : rootXmlSuite;
            }
            String name = rootXmlSuite.getName();

            RootXmlSuiteMaintainerResolver maintainerResolver = new RootXmlSuiteMaintainerResolver(rootXmlSuite);
            ChainedMaintainerResolver.addFirst(maintainerResolver);

            registrar.registerStart(new TestRunStartDescriptor(name, "testng", OffsetDateTime.now(), name));
        }
    }

    public void registerRunFinish(ISuite suite) {
        if (suite.getXmlSuite().getParentSuite() == null) {
            registrar.registerFinish(new TestRunFinishDescriptor(OffsetDateTime.now()));
        }
    }

    public void registerTestStart(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {
            log.debug("TestNGAdapter -> registerTestStart: retry is finished");

            TestInvocationContext testContext = buildTestInvocationContext(testResult);
            String correlationData = testContext.asJsonString();
            TestStartDescriptor testStartDescriptor = buildTestStartDescriptor(correlationData, testResult);

            setZebrunnerTestIdOnRerun(testResult, testResult.getMethod(), testStartDescriptor);

            String id = generateTestId(testContext);
            registrar.registerTestStart(id, testStartDescriptor);
        } else {
            log.debug("TestNGAdapter -> registerTestStart: retry is NOT finished");
        }
    }

    public void registerHeadlessTestStart(ITestResult testResult) {
        ITestNGMethod nextInvokedTest = getNextInvokedTest(testResult);

        if (isRetryFinished(nextInvokedTest, testResult.getTestContext())) {
            log.debug("TestNGAdapter -> registerHeadlessTestStart: retry is finished");

            TestInvocationContext testContext = buildTestInvocationContext(testResult);
            TestStartDescriptor testStartDescriptor = buildTestStartDescriptor(null, testResult);

            setZebrunnerTestIdOnRerun(testResult, nextInvokedTest, testStartDescriptor);

            String id = generateTestId(testContext);
            registrar.registerHeadlessTestStart(id, testStartDescriptor);
        } else {
            log.debug("TestNGAdapter -> registerHeadlessTestStart: retry is NOT finished");
        }
    }

    private void setZebrunnerTestIdOnRerun(ITestResult testResult, ITestNGMethod nextInvokedTest, TestStartDescriptor testStartDescriptor) {
        if (RerunContextHolder.isRerun()) {
            ITestContext context = testResult.getTestContext();
            Object[] parameters = testResult.getParameters();

            int dataProviderIndex = RunContextService.getCurrentDataProviderIndex(nextInvokedTest, context, parameters);

            RunContextService.getZebrunnerTestIdOnRerun(nextInvokedTest, dataProviderIndex)
                             .ifPresent(testStartDescriptor::setZebrunnerId);
        }
    }

    // Id value is set to a test right before execution. All the tests are executed sequentially.
    // Thus, we can consider the last test with id equal to current thread as the next test to be executed.
    private ITestNGMethod getNextInvokedTest(ITestResult testResult) {
        ITestNGMethod[] methods = testResult.getTestContext().getAllTestMethods();

        for (int i = methods.length - 1; i >= 0; i--) {
            ITestNGMethod method = methods[i];
            if (method.getId() != null && ThreadUtil.currentThreadInfo().equals(method.getId())) {
                return method;
            }
        }

        return null;
    }

    private TestStartDescriptor buildTestStartDescriptor(String correlationData, ITestResult testResult) {
        long startedAtMillis = testResult.getStartMillis();
        OffsetDateTime startedAt = ofMillis(startedAtMillis);

        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        Class<?> realClass = testResult.getTestClass().getRealClass();

        TestNameResolver testNameResolver = TestNameResolverRegistry.get();
        String displayName = testNameResolver.resolve(testResult);

        return new TestStartDescriptor(correlationData, displayName, startedAt, realClass, method);
    }

    public void registerTestFinish(ITestResult testResult) {
        long endedAtMillis = testResult.getEndMillis();
        OffsetDateTime endedAt = ofMillis(endedAtMillis);

        String retryMessage = collectRetryMessages(testResult.getMethod(), testResult.getTestContext());

        TestFinishDescriptor testFinishDescriptor = new TestFinishDescriptor(Status.PASSED, endedAt, retryMessage);

        TestInvocationContext testContext = buildTestInvocationContext(testResult);
        String id = generateTestId(testContext);
        registrar.registerTestFinish(id, testFinishDescriptor);

        // forcibly disable retry otherwise passed can't be registered in reporting tool!
        RetryService.setRetryFinished(testResult.getMethod(), testResult.getTestContext());
    }

    public void registerFailedTestFinish(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {
            log.debug("TestNGAdapter -> registerFailedTestFinish: retry is finished");

            TestInvocationContext testContext = buildTestInvocationContext(testResult);
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
            registrar.registerTestFinish(id, result);
        } else {
            log.debug("TestNGAdapter -> registerFailedTestFinish: retry is NOT finished");
        }
    }

    public void registerSkippedTestFinish(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {
            log.debug("TestNGAdapter -> registerSkippedTestFinish: retry is finished");

            long endedAtMillis = testResult.getEndMillis();
            OffsetDateTime endedAt = ofMillis(endedAtMillis);

            TestInvocationContext testContext = buildTestInvocationContext(testResult);

            String message;
            if (wasRetry(testResult.getMethod(), testResult.getTestContext())) {
                message = collectRetryMessages(testResult.getMethod(), testResult.getTestContext());
            } else {
                message = testResult.getThrowable().getMessage();
            }

            TestFinishDescriptor result = new TestFinishDescriptor(Status.SKIPPED, endedAt, message);
            String id = generateTestId(testContext);
            registrar.registerTestFinish(id, result);
        } else {
            log.debug("TestNGAdapter -> registerSkippedTestFinish: retry is NOT finished");
        }
    }

    private TestInvocationContext buildTestInvocationContext(ITestResult testResult) {
        ITestNGMethod testMethod = testResult.getMethod();
        ITestContext testContext = testResult.getTestContext();
        Object[] parameters = testResult.getParameters();

        int dataProviderIndex = RunContextService.getCurrentDataProviderIndex(testMethod, testContext, parameters);
        int invocationCount = RunContextService.getMethodInvocationCount(testMethod, testContext);

        return buildTestInvocationContext(testMethod, dataProviderIndex, parameters, invocationCount);
    }

    private TestInvocationContext buildTestInvocationContext(ITestNGMethod testMethod, int dataProviderIndex, Object[] parameters, int invocationCount) {
        String displayName = null;
        Test testAnnotation = testMethod.getConstructorOrMethod()
                                        .getMethod()
                                        .getAnnotation(org.testng.annotations.Test.class);
        if (testAnnotation != null) {
            displayName = testAnnotation.testName();
        }

        List<String> stringParameters = Arrays.stream(parameters)
                                              .map(Object::toString)
                                              .collect(Collectors.toList());
        List<String> parameterClassNames = Arrays.stream(testMethod.getConstructorOrMethod().getParameterTypes())
                                                 .map(Class::getName)
                                                 .collect(Collectors.toList());
        int instanceIndex = FactoryInstanceHolder.getInstanceIndex(testMethod);

        return TestInvocationContext.builder()
                                    .className(testMethod.getTestClass().getName())
                                    .methodName(testMethod.getMethodName())
                                    .displayName(displayName)
                                    .parameters(stringParameters)
                                    .parameterClassNames(parameterClassNames)
                                    .dataProviderIndex(dataProviderIndex)
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
