package com.zebrunner.agent.testng.core.method;

import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DependantMethodResolver {

    public static Set<IMethodInstance> resolve(Collection<IMethodInstance> methodsSuperSet, Collection<IMethodInstance> methods) {
        Map<String, IMethodInstance> nameToMethod = DependantMethodResolver.collectMethodsByName(methodsSuperSet);
        Set<IMethodInstance> dependantMethods = methods.stream()
                                                       .map(IMethodInstance::getMethod)
                                                       .map(method -> DependantMethodResolver.getDependantMethods(nameToMethod, method))
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

    private static Map<String, IMethodInstance> collectMethodsByName(Collection<IMethodInstance> methods) {
        return methods.stream()
                      .collect(Collectors.toMap(instance -> instance.getMethod().getQualifiedName(), Function.identity()));
    }

    private static Map<String, Set<IMethodInstance>> collectMethodsByDependedGroups(Collection<IMethodInstance> methods) {
        Map<String, Set<IMethodInstance>> groupToMethod = new HashMap<>();

        methods.stream()
               .map(IMethodInstance::getMethod)
               .flatMap(method -> Arrays.stream(method.getGroups()))
               .forEach(group -> groupToMethod.put(group, new HashSet<>()));

        methods.forEach(instance -> Arrays.stream(instance.getMethod().getGroups())
                                          .map(groupToMethod::get)
                                          .filter(Objects::nonNull)
                                          .forEach(groupMethods -> groupMethods.add(instance)));
        return groupToMethod;
    }

    private static Set<IMethodInstance> getDependantMethods(Map<String, IMethodInstance> methods, ITestNGMethod method) {
        Set<IMethodInstance> dependantMethods = new HashSet<>();

        Stack<String> dependantMethodNames = new Stack<>();
        // init stack with first level dependant methods
        Set<String> dependantMethodNamesBatch = Arrays.stream(method.getMethodsDependedUpon())
                                                      .collect(Collectors.toSet());
        dependantMethodNames.addAll(dependantMethodNamesBatch);

        while (!dependantMethodNames.isEmpty()) {
            String dependantMethodName = dependantMethodNames.pop();
            IMethodInstance dependantMethod = methods.get(dependantMethodName);

            if (dependantMethod != null && !dependantMethods.contains(dependantMethod)) {
                dependantMethods.add(dependantMethod);
                dependantMethodNamesBatch = Arrays.stream(dependantMethod.getMethod().getMethodsDependedUpon())
                                                  .collect(Collectors.toSet());
                dependantMethodNames.addAll(dependantMethodNamesBatch);
            }
        }

        return dependantMethods;
    }

    private static Set<IMethodInstance> getDependantGroupMethods(Map<String, Set<IMethodInstance>> groups, ITestNGMethod method) {
        Set<String> resolvedGroups = new HashSet<>();
        Set<IMethodInstance> dependantMethods = new HashSet<>();

        Stack<String> dependantGroupNames = new Stack<>();
        // init stack with first level dependant groups
        Set<String> dependantGroupNamesBatch = Arrays.stream(method.getGroupsDependedUpon())
                                                     .collect(Collectors.toSet());
        dependantGroupNames.addAll(dependantGroupNamesBatch);

        while (!dependantGroupNames.isEmpty()) {
            String dependantGroup = dependantGroupNames.pop();
            resolvedGroups.add(dependantGroup);
            Set<IMethodInstance> dependantGroupMethods = groups.get(dependantGroup);

            if (dependantGroupMethods != null) {
                dependantMethods.addAll(dependantGroupMethods);
                dependantGroupNamesBatch = dependantGroupMethods.stream()
                                                                .map(IMethodInstance::getMethod)
                                                                .map(ITestNGMethod::getGroupsDependedUpon)
                                                                .flatMap(Arrays::stream)
                                                                // filter out groups that are already resolved
                                                                .filter(group -> !resolvedGroups.contains(group))
                                                                .collect(Collectors.toSet());
                dependantGroupNames.addAll(dependantGroupNamesBatch);
            }
        }

        return dependantMethods;
    }

}
