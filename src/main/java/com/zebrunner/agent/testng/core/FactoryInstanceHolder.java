package com.zebrunner.agent.testng.core;

import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.internal.ParameterInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Needed to recognize current factory instance on runtime
 */
public class FactoryInstanceHolder {

    /**
     * Collection of factory test class names and all of their instances hash codes
     */
    private static final Map<String, List<Object>> CLASS_NAME_TO_INSTANCES = new ConcurrentHashMap<>();

    /**
     * Test class instance hash codes are registered in ordered fashion - that way can keep explicitly track instances
     * order that would allow to select specific class instance for rerun
     *
     * @param testClasses test classes
     */
    public static void registerInstances(Collection<ITestClass> testClasses) {
        testClasses.forEach(testClass -> {
            String className = testClass.getName();
            List<Object> instances = getInstances(testClass);
            if (!instances.isEmpty()) { // can be empty if not a factory test class - ignored in such case
                CLASS_NAME_TO_INSTANCES.putIfAbsent(className, instances);
            }
        });
    }

    private static List<Object> getInstances(ITestClass testClass) {
        return Arrays.stream(testClass.getInstances(true))
                     .map(o -> o instanceof ParameterInfo ? ((ParameterInfo) o).getInstance() : o)
                     .collect(Collectors.toList());
    }

    /**
     * Returns index of specific factory instance for this test method.
     *
     * @param method test method
     * @return factory instance index. If test method does not belong to factory instance -1 will be returned
     */
    public static int getInstanceIndex(ITestNGMethod method) {
        if (method.getInstance() != null) {
            String className = method.getTestClass().getName();
            List<Object> instances = CLASS_NAME_TO_INSTANCES.getOrDefault(className, Collections.emptyList());
            return instances.indexOf(method.getInstance());
        }
        return -1;
    }

    /**
     * Returns index of specific factory instance that will reported in Zebrunner for this test method.
     *
     * @param method test method
     * @return factory instance index. If test method does not belong to factory instance or there is only one instance of test class, then -1 will be returned
     */
    public static int getDisplayingInstanceIndex(ITestNGMethod method) {
        if (method.getInstance() != null) {
            String className = method.getTestClass().getName();
            List<Object> instances = CLASS_NAME_TO_INSTANCES.getOrDefault(className, Collections.emptyList());
            if (instances.size() > 1) {
                return instances.indexOf(method.getInstance());
            }
        }
        return -1;
    }

}
