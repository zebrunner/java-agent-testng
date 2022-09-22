package com.zebrunner.agent.testng.core.method;

import com.zebrunner.agent.testng.core.FactoryInstanceHolder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testng.IMethodInstance;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zebrunner.agent.testng.CollectionUtils.setOf;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.ParameterizedTest.DISPLAY_NAME_PLACEHOLDER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DependantMethodResolverTest {

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER)
    @MethodSource("provideMethodsTreeWithOnlyDependantMethods")
    public void resolve_ShouldResolveDependantMethods_WhenRootTestContainsOnlyDependantMethodsWithoutGroups(
            Collection<IMethodInstance> methodsSuperSet,
            Collection<IMethodInstance> methodToResolve,
            Collection<IMethodInstance> expectedResult
    ) {
        Set<IMethodInstance> dependantMethods = DependantMethodResolver.resolve(methodsSuperSet, methodToResolve);

        assertEquals(dependantMethods.size(), expectedResult.size());
        dependantMethods.forEach(dependantMethod -> assertTrue(expectedResult.contains(dependantMethod)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER)
    @MethodSource("provideMethodsTreeWithOnlyDependantGroups")
    public void resolve_ShouldResolveDependantMethods_WhenRootTestContainsOnlyDependantGroupsWithoutDependantMethods(
            Collection<IMethodInstance> methodsSuperSet,
            Collection<IMethodInstance> methodToResolve,
            Collection<IMethodInstance> expectedResult
    ) {
        Set<IMethodInstance> dependantMethods = DependantMethodResolver.resolve(methodsSuperSet, methodToResolve);

        assertEquals(dependantMethods.size(), expectedResult.size());
        dependantMethods.forEach(dependantMethod -> assertTrue(expectedResult.contains(dependantMethod)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER)
    @MethodSource("provideMethodsTreeWithBothDependantMethodsAndGroups")
    public void resolve_ShouldResolveDependantMethods_WhenRootTestContainsBothDependantMethodsAndGroups(
            Collection<IMethodInstance> methodsSuperSet,
            Collection<IMethodInstance> methodToResolve,
            Collection<IMethodInstance> expectedResult
    ) {
        Set<IMethodInstance> dependantMethods = DependantMethodResolver.resolve(methodsSuperSet, methodToResolve);

        assertEquals(dependantMethods.size(), expectedResult.size());
        dependantMethods.forEach(dependantMethod -> assertTrue(expectedResult.contains(dependantMethod)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER)
    @MethodSource("provideMethodsTreeWithoutDependantMethodsAndGroups")
    public void resolve_ShouldResolveDependantMethods_WhenRootTestDoesNotContainDependantMethodsAndGroups(
            Collection<IMethodInstance> methodsSuperSet,
            Collection<IMethodInstance> methodToResolve,
            Collection<IMethodInstance> expectedResult
    ) {
        Set<IMethodInstance> dependantMethods = DependantMethodResolver.resolve(methodsSuperSet, methodToResolve);

        assertEquals(dependantMethods.size(), expectedResult.size());
        dependantMethods.forEach(dependantMethod -> assertTrue(expectedResult.contains(dependantMethod)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER)
    @MethodSource("provideMethodsTreesWithDifferentFactoryInstances")
    public void resolve_ShouldResolveDependantMethods_WhenTestsBelongToDifferentFactoryInstances(
            Collection<ITestClass> factoryInstances,
            Collection<IMethodInstance> methodsSuperSet,
            Collection<IMethodInstance> methodToResolve,
            Collection<IMethodInstance> expectedResult
    ) {
        FactoryInstanceHolder.registerInstances(factoryInstances);
        Set<IMethodInstance> dependantMethods = DependantMethodResolver.resolve(methodsSuperSet, methodToResolve);

        assertEquals(dependantMethods.size(), expectedResult.size());
        dependantMethods.forEach(dependantMethod -> assertTrue(expectedResult.contains(dependantMethod)));
    }

    //------------------------------------------------------------------------------------------------------------------
    // DATA PROVIDERS
    //------------------------------------------------------------------------------------------------------------------

    private static Stream<Arguments> provideMethodsTreeWithOnlyDependantMethods() {
        ITestClass testClass = mockTestClass();

        IMethodInstance rootLevelMethod = mockIMethodInstance(
                "rootLevelMethod", testClass, emptySet(), setOf("DT11", "DT12", "DT13"), emptySet()
        );
        Set<IMethodInstance> dependantMethods = setOf(
                // DT - dependant test, first number - tree level
                mockIMethodInstance("DT11", testClass, emptySet(), setOf("DT21", "DT22"), emptySet()),
                mockIMethodInstance("DT12", testClass, emptySet(), setOf("DT23"), emptySet()),
                mockIMethodInstance("DT13", testClass, emptySet(), setOf("DT24"), emptySet()),

                mockIMethodInstance("DT21", testClass, emptySet(), setOf("DT31"), emptySet()),
                mockIMethodInstance("DT22", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("DT23", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("DT24", testClass, emptySet(), setOf("DT32"), emptySet()),

                mockIMethodInstance("DT31", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("DT32", testClass, emptySet(), setOf("DT41"), emptySet()),

                mockIMethodInstance("DT41", testClass, emptySet(), emptySet(), emptySet())
        );

        Set<IMethodInstance> methodsSuperSet = Stream.of(setOf(rootLevelMethod), dependantMethods)
                                                     .flatMap(Collection::stream)
                                                     .collect(Collectors.toSet());
        Set<IMethodInstance> methodsToResolve = setOf(rootLevelMethod);

        return Stream.of(
                Arguments.of(methodsSuperSet, methodsToResolve, dependantMethods)
        );
    }

    private static Stream<Arguments> provideMethodsTreeWithOnlyDependantGroups() {
        ITestClass testClass = mockTestClass();

        IMethodInstance rootLevelMethod = mockIMethodInstance(
                "rootLevelMethod", testClass, emptySet(), emptySet(), setOf("DG1", "DG2", "DG3")
        );
        Set<IMethodInstance> dependantMethods = setOf(
                // DT - dependant test, DG - dependant group, DT first number - tree level
                mockIMethodInstance("DT11", testClass, setOf("DG1"), emptySet(), setOf("DG2")),
                mockIMethodInstance("DT12", testClass, setOf("DG2"), emptySet(), setOf("DG3")),
                mockIMethodInstance("DT13", testClass, setOf("DG3"), emptySet(), setOf("DG4")),

                mockIMethodInstance("DT21", testClass, setOf("DG1"), emptySet(), setOf("DG2", "DG4")),
                mockIMethodInstance("DT22", testClass, setOf("DG2"), emptySet(), setOf("DG3")),
                mockIMethodInstance("DT23", testClass, setOf("DG2"), emptySet(), emptySet()),
                mockIMethodInstance("DT24", testClass, setOf("DG3"), emptySet(), emptySet()),

                mockIMethodInstance("DT31", testClass, setOf("DG1"), emptySet(), setOf("DG3", "DG4")),
                mockIMethodInstance("DT32", testClass, setOf("DG4"), emptySet(), emptySet()),

                mockIMethodInstance("DT41", testClass, setOf("DG4"), emptySet(), emptySet())
        );

        Set<IMethodInstance> methodsSuperSet = Stream.of(setOf(rootLevelMethod), dependantMethods)
                                                     .flatMap(Collection::stream)
                                                     .collect(Collectors.toSet());
        Set<IMethodInstance> methodsToResolve = setOf(rootLevelMethod);

        return Stream.of(
                Arguments.of(methodsSuperSet, methodsToResolve, dependantMethods)
        );
    }

    private static Stream<Arguments> provideMethodsTreeWithBothDependantMethodsAndGroups() {
        ITestClass testClass = mockTestClass();

        IMethodInstance rootLevelMethod = mockIMethodInstance(
                "rootLevelMethod", testClass, emptySet(), setOf("DT11", "DT12", "DT13"), setOf("DG1")
        );
        Set<IMethodInstance> dependantMethods = setOf(
                // DT - dependant test, DG - dependant group, DT first number - tree level
                mockIMethodInstance("DT11", testClass, setOf("DG1"), setOf("DT21", "DT22"), setOf("DG2")),
                mockIMethodInstance("DT12", testClass, setOf("DG2"), setOf("DT23"), setOf("DG3")),
                mockIMethodInstance("DT13", testClass, setOf("DG3"), setOf("DT24"), setOf("DG4")),

                mockIMethodInstance("DT21", testClass, setOf("DG1"), setOf("DT31"), setOf("DG2", "DG4")),
                mockIMethodInstance("DT22", testClass, setOf("DG2"), emptySet(), setOf("DG3")),
                mockIMethodInstance("DT23", testClass, setOf("DG2"), emptySet(), emptySet()),
                mockIMethodInstance("DT24", testClass, setOf("DG3"), setOf("DT32"), emptySet()),

                mockIMethodInstance("DT31", testClass, setOf("DG1"), emptySet(), setOf("DG3", "DG4")),
                mockIMethodInstance("DT32", testClass, setOf("DG4"), setOf("DT41"), emptySet()),

                mockIMethodInstance("DT41", testClass, setOf("DG4"), emptySet(), emptySet())
        );

        Set<IMethodInstance> methodsSuperSet = Stream.of(setOf(rootLevelMethod), dependantMethods)
                                                     .flatMap(Collection::stream)
                                                     .collect(Collectors.toSet());
        Set<IMethodInstance> methodsToResolve = setOf(rootLevelMethod);

        return Stream.of(
                Arguments.of(methodsSuperSet, methodsToResolve, dependantMethods)
        );
    }

    private static Stream<Arguments> provideMethodsTreeWithoutDependantMethodsAndGroups() {
        ITestClass testClass = mockTestClass();

        IMethodInstance rootLevelMethod = mockIMethodInstance(
                "rootLevelMethod", testClass, emptySet(), emptySet(), emptySet()
        );
        Set<IMethodInstance> otherMethods = setOf(
                mockIMethodInstance("T1", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T2", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T3", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T4", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T5", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T6", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T7", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T8", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T9", testClass, emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T10", testClass, emptySet(), emptySet(), emptySet())
        );

        Set<IMethodInstance> methodsSuperSet = Stream.of(setOf(rootLevelMethod), otherMethods)
                                                     .flatMap(Collection::stream)
                                                     .collect(Collectors.toSet());
        Set<IMethodInstance> methodsToResolve = setOf(rootLevelMethod);

        return Stream.of(
                Arguments.of(methodsSuperSet, methodsToResolve, emptySet())
        );
    }

    private static Stream<Arguments> provideMethodsTreesWithDifferentFactoryInstances() {
        Set<ITestClass> factoryInstances = new HashSet<>();

        // original tree
        ITestClass testClass = mockTestClass();
        factoryInstances.add(testClass);

        IMethodInstance rootLevelMethod = mockIMethodInstance(
                "rootLevelMethod", testClass, emptySet(), setOf("DT11", "DT12"), emptySet()
        );
        Set<IMethodInstance> dependantMethods = setOf(
                // DT - dependant test, DG - dependant group, DT first number - tree level
                mockIMethodInstance("DT11", testClass, setOf("DG1"), setOf("DT21"), setOf("DG2")),
                mockIMethodInstance("DT12", testClass, setOf("DG1"), setOf("DT21"), setOf("DG2")),

                mockIMethodInstance("DT21", testClass, setOf("DG1"), emptySet(), setOf("DG2"))
        );

        Set<IMethodInstance> methodsSuperSet = Stream.of(setOf(rootLevelMethod), dependantMethods)
                                                     .flatMap(Collection::stream)
                                                     .collect(Collectors.toSet());

        // factory instance tree
        ITestClass factoryInstance = mockTestClass();
        factoryInstances.add(factoryInstance);

        rootLevelMethod = mockIMethodInstance(
                "rootLevelMethod", factoryInstance, emptySet(), setOf("DT11", "DT12"), emptySet()
        );
        dependantMethods = setOf(
                // DT - dependant test, DG - dependant group, DT first number - tree level
                mockIMethodInstance("DT11", factoryInstance, setOf("DG1"), setOf("DT21"), setOf("DG2")),
                mockIMethodInstance("DT12", factoryInstance, setOf("DG1"), setOf("DT21"), setOf("DG2")),

                mockIMethodInstance("DT21", factoryInstance, setOf("DG1"), emptySet(), setOf("DG2"))
        );

        methodsSuperSet = Stream.of(methodsSuperSet, setOf(rootLevelMethod), dependantMethods)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toSet());

        Set<IMethodInstance> methodsToResolve = setOf(rootLevelMethod);
        return Stream.of(
                Arguments.of(factoryInstances, methodsSuperSet, methodsToResolve, dependantMethods)
        );
    }

    private static ITestClass mockTestClass() {
        ITestClass testClass = mock(ITestClass.class);

        String className = "org.Sample";
        when(testClass.getName()).thenReturn(className);
        when(testClass.getInstances(true)).thenReturn(new Object[] { testClass });

        return testClass;
    }

    private static IMethodInstance mockIMethodInstance(String methodName,
                                                       ITestClass testClass,
                                                       Collection<String> groups,
                                                       Collection<String> dependantMethods,
                                                       Collection<String> dependantGroups) {
        ITestNGMethod method = mock(ITestNGMethod.class);

        // getInstance of test method can return in some cases testClass as factory instance
        when(method.getInstance()).thenReturn(testClass);
        when(method.getTestClass()).thenReturn(testClass);
        when(method.getMethodName()).thenReturn(methodName);
        when(method.getQualifiedName()).thenReturn(methodName);

        String[] groupsArray = groups.toArray(new String[0]);
        when(method.getGroups()).thenReturn(groupsArray);

        String[] dependantMethodsArray = dependantMethods.toArray(new String[0]);
        when(method.getMethodsDependedUpon()).thenReturn(dependantMethodsArray);

        String[] dependantGroupsArray = dependantGroups.toArray(new String[0]);
        when(method.getGroupsDependedUpon()).thenReturn(dependantGroupsArray);

        IMethodInstance instance = mock(IMethodInstance.class);
        when(instance.getMethod()).thenReturn(method);

        return instance;
    }

}
