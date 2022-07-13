package com.zebrunner.agent.testng;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CollectionUtils {

    @SafeVarargs
    public static <T> Set<T> setOf(T... elements) {
        return Stream.of(elements).collect(Collectors.toSet());
    }

}
