package com.zebrunner.agent.testng.core.method;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    //------------------------------------------------------------------------------------------------------------------
    // DATA PROVIDERS
    //------------------------------------------------------------------------------------------------------------------

    private static Stream<Arguments> provideMethodsTreeWithOnlyDependantMethods() {
        IMethodInstance rootLevelMethod = mockIMethodInstance(
                "rootLevelMethod", emptySet(), Set.of("DT11", "DT12", "DT13"), emptySet()
        );
        Set<IMethodInstance> dependantMethods = Set.of(
                // DT - dependant test, first number - tree level
                mockIMethodInstance("DT11", emptySet(), Set.of("DT21", "DT22"), emptySet()),
                mockIMethodInstance("DT12", emptySet(), Set.of("DT23"), emptySet()),
                mockIMethodInstance("DT13", emptySet(), Set.of("DT24"), emptySet()),

                mockIMethodInstance("DT21", emptySet(), Set.of("DT31"), emptySet()),
                mockIMethodInstance("DT22", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("DT23", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("DT24", emptySet(), Set.of("DT32"), emptySet()),

                mockIMethodInstance("DT31", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("DT32", emptySet(), Set.of("DT41"), emptySet()),

                mockIMethodInstance("DT41", emptySet(), emptySet(), emptySet())
        );

        Set<IMethodInstance> methodsSuperSet = Stream.of(Set.of(rootLevelMethod), dependantMethods)
                                                     .flatMap(Collection::stream)
                                                     .collect(Collectors.toSet());
        Set<IMethodInstance> methodsToResolve = Set.of(rootLevelMethod);

        return Stream.of(
                Arguments.of(methodsSuperSet, methodsToResolve, dependantMethods)
        );
    }

    private static Stream<Arguments> provideMethodsTreeWithOnlyDependantGroups() {
        IMethodInstance rootLevelMethod = mockIMethodInstance(
                "rootLevelMethod", emptySet(), emptySet(), Set.of("DG1", "DG2", "DG3")
        );
        Set<IMethodInstance> dependantMethods = Set.of(
                // DT - dependant test, DG - dependant group, DT first number - tree level
                mockIMethodInstance("DT11", Set.of("DG1"), emptySet(), Set.of("DG2")),
                mockIMethodInstance("DT12", Set.of("DG2"), emptySet(), Set.of("DG3")),
                mockIMethodInstance("DT13", Set.of("DG3"), emptySet(), Set.of("DG4")),

                mockIMethodInstance("DT21", Set.of("DG1"), emptySet(), Set.of("DG2", "DG4")),
                mockIMethodInstance("DT22", Set.of("DG2"), emptySet(), Set.of("DG3")),
                mockIMethodInstance("DT23", Set.of("DG2"), emptySet(), emptySet()),
                mockIMethodInstance("DT24", Set.of("DG3"), emptySet(), emptySet()),

                mockIMethodInstance("DT31", Set.of("DG1"), emptySet(), Set.of("DG3", "DG4")),
                mockIMethodInstance("DT32", Set.of("DG4"), emptySet(), emptySet()),

                mockIMethodInstance("DT41", Set.of("DG4"), emptySet(), emptySet())
        );

        Set<IMethodInstance> methodsSuperSet = Stream.of(Set.of(rootLevelMethod), dependantMethods)
                                                     .flatMap(Collection::stream)
                                                     .collect(Collectors.toSet());
        Set<IMethodInstance> methodsToResolve = Set.of(rootLevelMethod);

        return Stream.of(
                Arguments.of(methodsSuperSet, methodsToResolve, dependantMethods)
        );
    }

    private static Stream<Arguments> provideMethodsTreeWithBothDependantMethodsAndGroups() {
        IMethodInstance rootLevelMethod = mockIMethodInstance(
                "rootLevelMethod", emptySet(), Set.of("DT11", "DT12", "DT13"), Set.of("DG1")
        );
        Set<IMethodInstance> dependantMethods = Set.of(
                // DT - dependant test, DG - dependant group, DT first number - tree level
                mockIMethodInstance("DT11", Set.of("DG1"), Set.of("DT21", "DT22"), Set.of("DG2")),
                mockIMethodInstance("DT12", Set.of("DG2"), Set.of("DT23"), Set.of("DG3")),
                mockIMethodInstance("DT13", Set.of("DG3"), Set.of("DT24"), Set.of("DG4")),

                mockIMethodInstance("DT21", Set.of("DG1"), Set.of("DT31"), Set.of("DG2", "DG4")),
                mockIMethodInstance("DT22", Set.of("DG2"), emptySet(), Set.of("DG3")),
                mockIMethodInstance("DT23", Set.of("DG2"), emptySet(), emptySet()),
                mockIMethodInstance("DT24", Set.of("DG3"), Set.of("DT32"), emptySet()),

                mockIMethodInstance("DT31", Set.of("DG1"), emptySet(), Set.of("DG3", "DG4")),
                mockIMethodInstance("DT32", Set.of("DG4"), Set.of("DT41"), emptySet()),

                mockIMethodInstance("DT41", Set.of("DG4"), emptySet(), emptySet())
        );

        Set<IMethodInstance> methodsSuperSet = Stream.of(Set.of(rootLevelMethod), dependantMethods)
                                                     .flatMap(Collection::stream)
                                                     .collect(Collectors.toSet());
        Set<IMethodInstance> methodsToResolve = Set.of(rootLevelMethod);

        return Stream.of(
                Arguments.of(methodsSuperSet, methodsToResolve, dependantMethods)
        );
    }

    private static Stream<Arguments> provideMethodsTreeWithoutDependantMethodsAndGroups() {
        IMethodInstance rootLevelMethod = mockIMethodInstance(
                "rootLevelMethod", emptySet(), emptySet(), emptySet()
        );
        Set<IMethodInstance> otherMethods = Set.of(
                mockIMethodInstance("T1", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T2", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T3", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T4", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T5", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T6", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T7", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T8", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T9", emptySet(), emptySet(), emptySet()),
                mockIMethodInstance("T10", emptySet(), emptySet(), emptySet())
        );

        Set<IMethodInstance> methodsSuperSet = Stream.of(Set.of(rootLevelMethod), otherMethods)
                                                     .flatMap(Collection::stream)
                                                     .collect(Collectors.toSet());
        Set<IMethodInstance> methodsToResolve = Set.of(rootLevelMethod);

        return Stream.of(
                Arguments.of(methodsSuperSet, methodsToResolve, emptySet())
        );
    }

    private static IMethodInstance mockIMethodInstance(String methodName,
                                                       Collection<String> groups,
                                                       Collection<String> dependantMethods,
                                                       Collection<String> dependantGroups) {
        ITestNGMethod method = mock(ITestNGMethod.class);

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
