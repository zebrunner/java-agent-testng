package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.testng.adapter.TestNGAdapter;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;

/**
 * Zebrunner Agent Listener implementation tracking TestNG test run events
 */
public class TestRunListener extends RerunAwareListener implements ISuiteListener, ITestListener, ITestNGListener {

    private final TestNGAdapter adapter;

    public TestRunListener() {
        this.adapter = new TestNGAdapter();
    }

    @Override
    public void onStart(ISuite suite) {
        adapter.registerRunOrSuiteStart(suite);
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
        System.out.println();
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println();
    }

}
