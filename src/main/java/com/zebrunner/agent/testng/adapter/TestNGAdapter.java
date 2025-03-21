package com.zebrunner.agent.testng.adapter;

import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.config.provider.SystemPropertiesConfigurationProvider;
import com.zebrunner.agent.core.exception.BlockedTestException;
import com.zebrunner.agent.core.registrar.TestRunRegistrar;
import com.zebrunner.agent.core.registrar.domain.Status;
import com.zebrunner.agent.core.registrar.domain.TestFinish;
import com.zebrunner.agent.core.registrar.domain.TestRunFinish;
import com.zebrunner.agent.core.registrar.domain.TestRunStart;
import com.zebrunner.agent.core.registrar.domain.TestStart;
import com.zebrunner.agent.core.registrar.maintainer.ChainedMaintainerResolver;
import com.zebrunner.agent.testng.core.ExceptionUtils;
import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import com.zebrunner.agent.testng.core.RootXmlSuiteLabelAssigner;
import com.zebrunner.agent.testng.core.TestInvocationContext;
import com.zebrunner.agent.testng.core.config.RootXmlSuiteConfigurationProvider;
import com.zebrunner.agent.testng.core.maintainer.RootXmlSuiteMaintainerResolver;
import com.zebrunner.agent.testng.core.testname.TestNameResolverRegistry;
import com.zebrunner.agent.testng.listener.RetryService;
import com.zebrunner.agent.testng.listener.RunContextService;

/**
 * Adapter used to convert TestNG test domain to Zebrunner Agent domain
 */
@Slf4j
public class TestNGAdapter {

    private final TestRunRegistrar registrar;

    private XmlSuite rootXmlSuite;

    private static final AtomicBoolean CUCUMBER = new AtomicBoolean(true);

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

            ChainedMaintainerResolver.addLast(new RootXmlSuiteMaintainerResolver(rootXmlSuite));

            ConfigurationHolder.addConfigurationProviderAfter(
                    new RootXmlSuiteConfigurationProvider(rootXmlSuite),
                    SystemPropertiesConfigurationProvider.class
            );

            registrar.registerStart(new TestRunStart(name, "testng", Instant.now(), name));
            RootXmlSuiteLabelAssigner.getInstance().assignTestRunLabels(rootXmlSuite);
        }
    }

    public void registerRunFinish(ISuite suite) {
        if (suite.getXmlSuite().getParentSuite() == null) {
            registrar.registerFinish(new TestRunFinish(Instant.now()));
        }
    }

    public void registerTestStart(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {
            log.debug("TestNGAdapter -> registerTestStart: retry is finished");

            TestInvocationContext testContext = this.buildTestStartInvocationContext(testResult);
            String correlationData = testContext.asJsonString();
            TestStart testStart = this.buildTestStart(correlationData, testResult);

            this.setZebrunnerTestIdOnRerun(testResult, testResult.getMethod(), testStart);

            String id = this.generateTestId(testContext);
            registrar.registerTestStart(id, testStart);
        } else {
            log.debug("TestNGAdapter -> registerTestStart: retry is NOT finished");
        }
    }

    public void registerHeadlessTestStart(ITestResult testResult, ITestNGMethod nextTestMethod) {
        if (!registrar.isTestStarted()) { // we should not register the same headless test twice
            if (isRetryFinished(nextTestMethod, testResult.getTestContext())) {
                log.debug("TestNGAdapter -> registerHeadlessTestStart: retry is finished");

                TestInvocationContext testContext = this.buildTestStartInvocationContext(testResult);
                TestStart testStart = this.buildTestStart(null, testResult);

                this.setZebrunnerTestIdOnRerun(testResult, nextTestMethod, testStart);

                String id = generateTestId(testContext);
                registrar.registerHeadlessTestStart(id, testStart);
            } else {
                log.debug("TestNGAdapter -> registerHeadlessTestStart: retry is NOT finished");
            }
        }
    }

    private TestInvocationContext buildTestStartInvocationContext(ITestResult testResult) {
        ITestNGMethod testMethod = testResult.getMethod();
        ITestContext testContext = testResult.getTestContext();
        Object[] parameters = testResult.getParameters();

        int dataProviderIndex = RunContextService.getCurrentDataProviderIndex(testMethod, testContext, parameters);
        RunContextService.setCurrentDataProviderData(testMethod, testContext, parameters, dataProviderIndex);

        int invocationIndex = RunContextService.getMethodInvocationIndex(testMethod, testContext);

        return buildTestInvocationContext(testMethod, dataProviderIndex, parameters, invocationIndex);
    }

    private void setZebrunnerTestIdOnRerun(ITestResult testResult, ITestNGMethod testMethod, TestStart testStart) {
        if (com.zebrunner.agent.core.registrar.RunContextService.isRerun()) {
            ITestContext context = testResult.getTestContext();
            Object[] parameters = testResult.getParameters();

            // when parametrized method has at least on @BeforeMethod,
            // then parameters array contains argument(s) of the @BeforeMethod(s).
            //
            // thus, if current run is a rerun, then we will not be able to figure out
            // what method should be restarted in Zebrunner,
            // because arguments index constitutes identity of a test in Zebrunner
            //
            // the workaround here is to add not used argument for the @BeforeMethod(s) with type Object[].
            // in this case, TestNG will propagate the test method's arguments in this variable
            // and the agent will be able to identify appropriate existing Zebrunner test
            int dataProviderIndex = RunContextService.getCurrentDataProviderIndex(testMethod, context, parameters);
            if (dataProviderIndex == -1 && testMethod.isDataDriven() && parameters != null) {
                for (Object parameter : parameters) {
                    if (parameter instanceof Object[]) {
                        dataProviderIndex = RunContextService.getCurrentDataProviderIndex(testMethod, context, ((Object[]) parameter));
                    }
                }
            }

            RunContextService.getZebrunnerTestIdOnRerun(testMethod, dataProviderIndex)
                             .ifPresent(testStart::setId);
        }
    }

    private TestStart buildTestStart(String correlationData, ITestResult testResult) {
        ITestNGMethod testMethod = testResult.getMethod();
        ITestContext context = testResult.getTestContext();
        Object[] parameters = testResult.getParameters();

        String displayName = TestNameResolverRegistry.get().resolve(testResult);
        Instant startedAt = Instant.ofEpochMilli(testResult.getStartMillis());
        Class<?> realClass = testResult.getTestClass().getRealClass();
        String realClassName = null;
        Method method = testMethod.getConstructorOrMethod().getMethod();
        String methodName = null;

        Integer dataProviderIndex = RunContextService.getCurrentDataProviderIndex(testMethod, context, parameters);
        if (dataProviderIndex == -1) {
            dataProviderIndex = null;
        }

        if (CUCUMBER.get()) {
            try {
                Class<?> pickleWrapperClass = Class.forName("io.cucumber.testng.PickleWrapper");
                Class<?> featureWrapperClass = Class.forName("io.cucumber.testng.FeatureWrapper");
                String featureName = Arrays.stream(parameters)
                                           .filter(featureWrapperClass::isInstance)
                                           .map(Object::toString)
                                           .map(feature -> feature.replaceAll("^\"|\"$", ""))
                                           .findAny()
                                           .orElseThrow(ClassNotFoundException::new);
                String pickleName = Arrays.stream(parameters)
                                          .filter(pickleWrapperClass::isInstance)
                                          .map(Object::toString)
                                          .map(pickle -> pickle.replaceAll("^\"|\"$", ""))
                                          .findAny()
                                          .orElseThrow(ClassNotFoundException::new);
                realClassName = featureName;
                methodName = pickleName;
            } catch (ClassNotFoundException e) {
                CUCUMBER.set(false);
            }
        }

        return TestStart.builder()
                        .correlationData(correlationData)
                        .name(displayName)
                        .startedAt(startedAt)
                        .testClass(realClass)
                        .testClassName(realClassName)
                        .testMethod(method)
                        .testMethodName(methodName)
                        .argumentsIndex(dataProviderIndex)
                        .testGroups(Arrays.asList(testMethod.getGroups()))
                        .build();
    }

    public void registerTestFinish(ITestResult testResult) {
        long endedAtMillis = testResult.getEndMillis();
        Instant endedAt = Instant.ofEpochMilli(endedAtMillis);

        TestFinish testFinish = new TestFinish(Status.PASSED, endedAt);

        TestInvocationContext testContext = buildTestFinishInvocationContext(testResult);
        String id = generateTestId(testContext);
        registrar.registerTestFinish(id, testFinish);

        // forcibly disable retry otherwise passed can't be registered in reporting tool!
        RetryService.setRetryFinished(testResult.getMethod(), testResult.getTestContext());
    }

    public void registerFailedTestFinish(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {
            log.debug("TestNGAdapter -> registerFailedTestFinish: retry is finished");

            TestInvocationContext testContext = buildTestFinishInvocationContext(testResult);
            String id = generateTestId(testContext);

            if (!registrar.isTestStarted(id)) {
                registerTestStart(testResult);
            }

            long endedAtMillis = testResult.getEndMillis();
            Instant endedAt = Instant.ofEpochMilli(endedAtMillis);
            String errorMessage = ExceptionUtils.getStacktrace(testResult.getThrowable());

            Status status = testResult.getThrowable() instanceof BlockedTestException
                    ? Status.BLOCKED
                    : Status.FAILED;
            TestFinish result = new TestFinish(status, endedAt, errorMessage);
            registrar.registerTestFinish(id, result);
        } else {
            log.debug("TestNGAdapter -> registerFailedTestFinish: retry is NOT finished");
        }
    }

    public void registerSkippedTestFinish(ITestResult testResult) {
        if (isRetryFinished(testResult.getMethod(), testResult.getTestContext())) {
            log.debug("TestNGAdapter -> registerSkippedTestFinish: retry is finished");

            TestInvocationContext testContext = buildTestFinishInvocationContext(testResult);
            String id = generateTestId(testContext);

            Instant endedAt = Instant.ofEpochMilli(testResult.getEndMillis());
            String errorMessage = ExceptionUtils.getStacktrace(testResult.getThrowable());

            TestFinish result = new TestFinish(Status.SKIPPED, endedAt, errorMessage);
            registrar.registerTestFinish(id, result);
        } else {
            log.debug("TestNGAdapter -> registerSkippedTestFinish: retry is NOT finished");
        }
    }

    private TestInvocationContext buildTestFinishInvocationContext(ITestResult testResult) {
        ITestNGMethod testMethod = testResult.getMethod();
        ITestContext testContext = testResult.getTestContext();
        Object[] parameters = testResult.getParameters();

        int dataProviderIndex = RunContextService.getCurrentDataProviderIndex(testMethod, testContext, parameters);
//        if (dataProviderIndex == -1) {
//            // if the data provider data has been modified during the test,
//            // then the parameters at the end of the test will not be equal to the parameters at the start of the test.
//            // for such cases, we try to memorize the original method arguments in thread-local variable
//            // and search by them here
//            parameters = testStartDataProviderData.get();
//            RunContextService.getCurrentDataProviderIndex(testMethod, testContext, parameters);
//        }
        int invocationIndex = RunContextService.getMethodInvocationIndex(testMethod, testContext);

        return buildTestInvocationContext(testMethod, dataProviderIndex, parameters, invocationIndex);
    }

    public void registerAfterTestStart() {
        registrar.registerAfterTestStart();
    }

    public void registerAfterTestFinish() {
        registrar.registerAfterTestFinish();
    }

    public void clearConfigurationLogs() {
        registrar.clearConfigurationLogs();
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
                                              .map(Objects::toString)
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

}
