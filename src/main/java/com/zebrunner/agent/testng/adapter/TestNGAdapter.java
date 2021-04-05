package com.zebrunner.agent.testng.adapter;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.config.provider.SystemPropertiesConfigurationProvider;
import com.zebrunner.agent.core.registrar.RerunContextHolder;
import com.zebrunner.agent.core.registrar.TestRunRegistrar;
import com.zebrunner.agent.core.registrar.descriptor.Status;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.core.registrar.maintainer.ChainedMaintainerResolver;
import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.RootXmlSuiteLabelAssigner;
import com.zebrunner.agent.testng.core.TestInvocationContext;
import com.zebrunner.agent.testng.core.config.RootXmlSuiteConfigurationProvider;
import com.zebrunner.agent.testng.core.maintainer.RootXmlSuiteMaintainerResolver;
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
import org.testng.xml.XmlSuite;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
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

            ChainedMaintainerResolver.addFirst(new RootXmlSuiteMaintainerResolver(rootXmlSuite));

            ConfigurationHolder.addConfigurationProviderAfter(
                    new RootXmlSuiteConfigurationProvider(rootXmlSuite),
                    SystemPropertiesConfigurationProvider.class
            );

            registrar.registerStart(new TestRunStartDescriptor(name, "testng", OffsetDateTime.now(), name));
            RootXmlSuiteLabelAssigner.getInstance().assignTestRunLabels(rootXmlSuite);
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

    public void registerHeadlessTestStart(ITestResult testResult, ITestNGMethod nextTestMethod) {
        if (!registrar.isTestStarted()) { // we should not register the same headless test twice
            if (isRetryFinished(nextTestMethod, testResult.getTestContext())) {
                log.debug("TestNGAdapter -> registerHeadlessTestStart: retry is finished");

                TestInvocationContext testContext = buildTestInvocationContext(testResult);
                TestStartDescriptor testStartDescriptor = buildTestStartDescriptor(null, testResult);

                setZebrunnerTestIdOnRerun(testResult, nextTestMethod, testStartDescriptor);

                String id = generateTestId(testContext);
                registrar.registerHeadlessTestStart(id, testStartDescriptor);
            } else {
                log.debug("TestNGAdapter -> registerHeadlessTestStart: retry is NOT finished");
            }
        }
    }

    private void setZebrunnerTestIdOnRerun(ITestResult testResult, ITestNGMethod testMethod, TestStartDescriptor testStartDescriptor) {
        if (RerunContextHolder.isRerun()) {
            ITestContext context = testResult.getTestContext();
            Object[] parameters = testResult.getParameters();

            int dataProviderIndex = RunContextService.getCurrentDataProviderIndex(testMethod, context, parameters);

            RunContextService.getZebrunnerTestIdOnRerun(testMethod, dataProviderIndex)
                             .ifPresent(testStartDescriptor::setZebrunnerId);
        }
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

        TestFinishDescriptor testFinishDescriptor = new TestFinishDescriptor(Status.PASSED, endedAt);

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
            String errorMessage = getPrintedStackTrace(testResult.getThrowable());

            TestFinishDescriptor result = new TestFinishDescriptor(Status.FAILED, endedAt, errorMessage);
            registrar.registerTestFinish(id, result);
        } else {
            log.debug("TestNGAdapter -> registerFailedTestFinish: retry is NOT finished");
        }
    }

    public void registerSkippedTestFinish(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {
            log.debug("TestNGAdapter -> registerSkippedTestFinish: retry is finished");

            TestInvocationContext testContext = buildTestInvocationContext(testResult);
            String id = generateTestId(testContext);

            OffsetDateTime endedAt = ofMillis(testResult.getEndMillis());
            String errorMessage = getPrintedStackTrace(testResult.getThrowable());

            TestFinishDescriptor result = new TestFinishDescriptor(Status.SKIPPED, endedAt, errorMessage);
            registrar.registerTestFinish(id, result);
        } else {
            log.debug("TestNGAdapter -> registerSkippedTestFinish: retry is NOT finished");
        }
    }

    private String getPrintedStackTrace(Throwable throwable) {
        if (throwable != null) {
            StringWriter errorMessageStringWriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(errorMessageStringWriter));
            return errorMessageStringWriter.toString();
        } else {
            return "";
        }
    }

    public void registerAfterTestStart() {
        registrar.registerAfterTestStart();
    }

    public void registerAfterTestFinish() {
        registrar.registerAfterTestFinish();
    }

    private TestInvocationContext buildTestInvocationContext(ITestResult testResult) {
        ITestNGMethod testMethod = testResult.getMethod();
        ITestContext testContext = testResult.getTestContext();
        Object[] parameters = testResult.getParameters();

        int dataProviderIndex = RunContextService.getCurrentDataProviderIndex(testMethod, testContext, parameters);
        int invocationIndex = RunContextService.getMethodInvocationIndex(testMethod, testContext);

        return buildTestInvocationContext(testMethod, dataProviderIndex, parameters, invocationIndex);
    }

    private TestInvocationContext buildTestInvocationContext(ITestNGMethod testMethod, int dataProviderIndex, Object[] parameters, int invocationIndex) {
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
                                    .invocationIndex(invocationIndex)
                                    .build();
    }

    private boolean isRetryFinished(ITestNGMethod method, ITestContext context) {
        return RetryService.isRetryFinished(method, context);
    }

    private String generateTestId(TestInvocationContext testInvocationContext) {
        return testInvocationContext.toString();
    }

    private OffsetDateTime ofMillis(long epochMillis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

}
