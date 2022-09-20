package com.zebrunner.agent.testng.core.method;

import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public final class DependantMethodResolver {

    /**
     * Resolve dependant methods using tree Depth-first search. There can be both direct dependant methods and dependant groups.
     * In such cases method resolves direct dependant methods and after resolves dependant group methods.
     *
     * @param methodsSuperSet the whole set of methods
     * @param methods         methods that must be resolved with their dependant methods
     * @return dependant methods
     */
    public static Set<IMethodInstance> resolve(Collection<IMethodInstance> methodsSuperSet, Collection<IMethodInstance> methods) {
        Map<String, Set<IMethodInstance>> nameToMethods = DependantMethodResolver.collectMethodsByName(methodsSuperSet);
        Set<IMethodInstance> dependantMethods = methods.stream()
                                                       .map(IMethodInstance::getMethod)
                                                       .map(method -> DependantMethodResolver.getDependantMethods(nameToMethods, method))
                                                       .flatMap(Collection::stream)
                                                       .collect(Collectors.toSet());

        Map<String, Set<IMethodInstance>> groupToMethods = DependantMethodResolver.collectMethodsByDependedGroups(methodsSuperSet);
        Set<IMethodInstance> dependantGroupMethods = methods.stream()
                                                            .map(IMethodInstance::getMethod)
                                                            .map(method -> DependantMethodResolver.getDependantGroupMethods(groupToMethods, method))
                                                            .flatMap(Collection::stream)
                                                            .collect(Collectors.toSet());

        dependantMethods.addAll(dependantGroupMethods);
        return dependantMethods;
    }

    /**
     * Collect methods by qualified name. Qualified name = testClass name + "." + testMethod name.
     * Notice: tests with same name can belong to different factory instances.
     *
     * @return map where key is method's qualified name and value is set with belonging to this name test methods.
     */
    private static Map<String, Set<IMethodInstance>> collectMethodsByName(Collection<IMethodInstance> methods) {
        return methods.stream()
                      .collect(
                              Collectors.groupingBy(
                                      instance -> instance.getMethod().getQualifiedName(),
                                      Collectors.toSet()
                              )
                      );
    }

    private static Map<String, Set<IMethodInstance>> collectMethodsByDependedGroups(Collection<IMethodInstance> methods) {
        Map<String, Set<IMethodInstance>> groupToMethods = new HashMap<>();

        methods.stream()
               .map(IMethodInstance::getMethod)
               .flatMap(method -> Arrays.stream(method.getGroups()))
               .forEach(group -> groupToMethods.put(group, new HashSet<>()));

        methods.forEach(instance -> Arrays.stream(instance.getMethod().getGroups())
                                          .map(groupToMethods::get)
                                          .filter(Objects::nonNull)
                                          .forEach(groupMethods -> groupMethods.add(instance)));
        return groupToMethods;
    }

    private static Set<IMethodInstance> getDependantMethods(Map<String, Set<IMethodInstance>> methods, ITestNGMethod method) {
        Set<String> resolvedMethods = new HashSet<>();
        Set<IMethodInstance> dependantMethods = new HashSet<>();

        Stack<String> methodsToResolve = new Stack<>();
        // init stack with first level dependant methods
        Set<String> nextMethodsToResolveBatch = Arrays.stream(method.getMethodsDependedUpon())
                                                      .collect(Collectors.toSet());
        methodsToResolve.addAll(nextMethodsToResolveBatch);

        while (!methodsToResolve.isEmpty()) {
            String dependantMethodName = methodsToResolve.pop();
            resolvedMethods.add(dependantMethodName);
            Set<IMethodInstance> methodsWithSameFactoryInstance = methods.getOrDefault(dependantMethodName, Collections.emptySet())
                                                                         .stream()
                                                                         .filter(dependantMethod -> belongsToTheSameFactoryInstance(dependantMethod.getMethod(), method))
                                                                         .collect(Collectors.toSet());

            if (!methodsWithSameFactoryInstance.isEmpty()) {
                dependantMethods.addAll(methodsWithSameFactoryInstance);
                nextMethodsToResolveBatch = methodsWithSameFactoryInstance.stream()
                                                                          .map(IMethodInstance::getMethod)
                                                                          .map(ITestNGMethod::getMethodsDependedUpon)
                                                                          .flatMap(Arrays::stream)
                                                                          // filter out methods that are already resolved
                                                                          .filter(methodName -> !resolvedMethods.contains(methodName))
                                                                          .collect(Collectors.toSet());
                methodsToResolve.addAll(nextMethodsToResolveBatch);
            }
        }

        return dependantMethods;
    }

    private static Set<IMethodInstance> getDependantGroupMethods(Map<String, Set<IMethodInstance>> groups, ITestNGMethod method) {
        Set<String> resolvedGroups = new HashSet<>();
        Set<IMethodInstance> dependantMethods = new HashSet<>();

        Stack<String> groupsToResolve = new Stack<>();
        // init stack with first level dependant groups
        Set<String> nextGroupsToResolveBatch = Arrays.stream(method.getGroupsDependedUpon())
                                                     .collect(Collectors.toSet());
        groupsToResolve.addAll(nextGroupsToResolveBatch);

        while (!groupsToResolve.isEmpty()) {
            String dependantGroup = groupsToResolve.pop();
            resolvedGroups.add(dependantGroup);
            Set<IMethodInstance> methodsWithSameFactoryInstance = groups.getOrDefault(dependantGroup, Collections.emptySet())
                                                                        .stream()
                                                                        .filter(dependantMethod -> belongsToTheSameFactoryInstance(dependantMethod.getMethod(), method))
                                                                        .collect(Collectors.toSet());

            if (!methodsWithSameFactoryInstance.isEmpty()) {
                dependantMethods.addAll(methodsWithSameFactoryInstance);
                nextGroupsToResolveBatch = methodsWithSameFactoryInstance.stream()
                                                                         .map(IMethodInstance::getMethod)
                                                                         .map(ITestNGMethod::getGroupsDependedUpon)
                                                                         .flatMap(Arrays::stream)
                                                                         // filter out groups that are already resolved
                                                                         .filter(group -> !resolvedGroups.contains(group))
                                                                         .collect(Collectors.toSet());
                groupsToResolve.addAll(nextGroupsToResolveBatch);
            }
        }

        return dependantMethods;
    }

    private static boolean belongsToTheSameFactoryInstance(ITestNGMethod method1, ITestNGMethod method2) {
        return FactoryInstanceHolder.getInstanceIndex(method1) == FactoryInstanceHolder.getInstanceIndex(method2);
    }

}
