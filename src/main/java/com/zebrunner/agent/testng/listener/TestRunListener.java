package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.testng.adapter.TestNGAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IConfigurationListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConfigurationMethod;

import java.lang.invoke.MethodHandles;

/**
 * Zebrunner Agent Listener implementation tracking TestNG test run events
 */
public class TestRunListener extends RerunAwareListener implements ISuiteListener, ITestListener, ITestNGListener, IConfigurationListener {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TestNGAdapter adapter;

    public TestRunListener() {
        this.adapter = new TestNGAdapter();
    }

    @Override
    public void onStart(ISuite suite) {
        log.debug("Beginning TestRunListener -> onStart(ISuite suite)");
        adapter.registerRunStart(suite);
        log.debug("Finishing TestRunListener -> onStart(ISuite suite)");
    }

    @Override
    public void onFinish(ISuite suite) {
        log.debug("Beginning TestRunListener -> onFinish(ISuite suite)");
        adapter.registerRunFinish(suite);
        log.debug("Finishing TestRunListener -> onFinish(ISuite suite)");
    }

    @Override
    public void onTestStart(ITestResult testResult) {
        log.debug("Beginning TestRunListener -> onTestStart");
        RunContextService.incrementMethodInvocationCount(testResult.getMethod(), testResult.getTestContext());
        adapter.registerTestStart(testResult);
        log.debug("Finishing TestRunListener -> onTestStart");
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        log.debug("Beginning TestRunListener -> onTestSuccess");
        adapter.registerTestFinish(testResult);
        log.debug("Finishing TestRunListener -> onTestSuccess");
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        log.debug("Beginning TestRunListener -> onTestFailure");
        adapter.registerFailedTestFinish(testResult);
        log.debug("Finishing TestRunListener -> onTestFailure");
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        log.debug("Beginning TestRunListener -> onTestSkipped");
        adapter.registerSkippedTestFinish(testResult);
        log.debug("Finishing TestRunListener -> onTestSkipped");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult testResult) {
        log.debug("Beginning TestRunListener -> onTestFailedButWithinSuccessPercentage");
        adapter.registerTestFinish(testResult);
        log.debug("Finishing TestRunListener -> onTestFailedButWithinSuccessPercentage");
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }

    @Override
    public void beforeConfiguration(ITestResult tr) {
        ITestNGMethod resultMethod = tr.getMethod();
        if (resultMethod instanceof ConfigurationMethod) {
            ConfigurationMethod method = (ConfigurationMethod) resultMethod;

            if (method.isBeforeMethodConfiguration()) {
                adapter.registerHeadlessTestStart(tr);
            }
        }
    }

}
