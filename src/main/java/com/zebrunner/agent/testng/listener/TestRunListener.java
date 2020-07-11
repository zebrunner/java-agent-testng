package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.testng.adapter.TestNGAdapter;
import org.testng.IConfigurationListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConfigurationMethod;

/**
 * Zebrunner Agent Listener implementation tracking TestNG test run events
 */
public class TestRunListener extends RerunAwareListener implements ISuiteListener, ITestListener, ITestNGListener, IConfigurationListener {

    private final TestNGAdapter adapter;

    public TestRunListener() {
        this.adapter = new TestNGAdapter();
    }

    @Override
    public void onStart(ISuite suite) {
        adapter.registerRunStart(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        adapter.registerRunFinish(suite);
    }

    @Override
    public void onTestStart(ITestResult testResult) {
        adapter.registerTestStart(testResult);
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        adapter.registerTestFinish(testResult);
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        adapter.registerFailedTestFinish(testResult);
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        adapter.registerSkippedTestFinish(testResult);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult testResult) {
        adapter.registerTestFinish(testResult);
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
