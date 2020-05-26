package com.zebrunner.agent.testng.core;

import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.internal.IParameterInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Needed to recognize current factory instance on runtime
 */
public class FactoryInstanceHolder {

    /**
     * Collection of factory test class names and all of their instances hash codes
     */
    private static final Map<String, List<Long>> instancesHashCodes = new ConcurrentHashMap<>();

    /**
     * Test class instance hash codes are registered in ordered fashion - that way can keep explicitly track instances
     * order that would allow to select specific class instance for rerun
     * @param testClasses test classes
     */
    public static void registerInstances(Collection<ITestClass> testClasses) {
        testClasses.forEach(testClass -> {
            String className = testClass.getName();
            long[] hashCodes = testClass.getInstanceHashCodes();
            if (hashCodes.length > 0) { // can be empty if not a factory test class - ignored in such case
                instancesHashCodes.putIfAbsent(className, LongStream.of(hashCodes).boxed().collect(Collectors.toList()));
            }
        });
    }

    /**
     * Returns index of specific factory instance for this test method.
     * @param method test method
     * @return factory instance index. If test method does not belong to factory instance -1 will be returned
     */
    public static int getInstanceIndex(ITestNGMethod method) {
        IParameterInfo factoryParamsInfo = method.getFactoryMethodParamsInfo();
        if (factoryParamsInfo != null) {
            List<Long> hashCodes = instancesHashCodes.get(method.getTestClass().getName());
            return hashCodes.indexOf((long) factoryParamsInfo.getInstance().hashCode());
        }
        return -1;
    }

}
