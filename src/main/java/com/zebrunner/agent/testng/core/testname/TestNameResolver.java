package com.zebrunner.agent.testng.core.testname;

import org.testng.ITestResult;

public interface TestNameResolver {

    String resolve(ITestResult testResult);

}
