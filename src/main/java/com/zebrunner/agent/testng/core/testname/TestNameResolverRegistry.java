package com.zebrunner.agent.testng.core.testname;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestNameResolverRegistry {

    private static TestNameResolver testNameResolver;

    static {
        set(new DefaultTestNameResolver());
    }

    public static void set(TestNameResolver testNameResolver) {
        if (testNameResolver != null) {
            TestNameResolverRegistry.testNameResolver = testNameResolver;
        }
    }

    public static TestNameResolver get() {
        return testNameResolver;
    }

}
