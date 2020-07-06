package com.zebrunner.agent.testng.core;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores context of specific test method invocation (e.g. invocation N of test method with invocation count X where N is less or equal than X)
 */
@Getter
@Setter
@NoArgsConstructor
public class TestInvocationContext {

    private final static Gson gson = new Gson();

    private String className;
    private String methodName;
    private String displayName;
    private List<String> parameterClassNames;
    private int dataProviderLineIndex;
    private int instanceIndex;
    private int invocationIndex;

    @Builder
    public TestInvocationContext(String className, String methodName, String displayName,
                                 List<String> parameterClassNames, int dataProviderLineIndex, int instanceIndex,
                                 int invocationIndex) {
        this.className = className;
        this.methodName = methodName;
        this.displayName = displayName;
        this.parameterClassNames = parameterClassNames;
        this.dataProviderLineIndex = dataProviderLineIndex;
        this.instanceIndex = instanceIndex;
        this.invocationIndex = invocationIndex;
    }

    public String buildUniqueDisplayName() {
        StringBuilder builderPattern = new StringBuilder("%s(%s)");

        List<Object> buildParameters = new ArrayList<>();

        String displayName = this.displayName == null || this.displayName.isEmpty() ? this.methodName : this.displayName;
        buildParameters.add(displayName);
        buildParameters.add(String.join(", ", parameterClassNames));

        if (dataProviderLineIndex != -1) {
            builderPattern.append("[%d]");
            buildParameters.add(dataProviderLineIndex);
        }

        if (instanceIndex != -1) {
            builderPattern.append(" ")
                          .append("(%d)");
            buildParameters.add(instanceIndex);
        }
        return String.format(builderPattern.toString(), buildParameters.toArray());
    }

    @Override
    public int hashCode() {
        return (className + methodName + parameterClassNames + instanceIndex).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TestInvocationContext)) {
            return false;
        }

        TestInvocationContext test = (TestInvocationContext) o;
        return hashCode() == test.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builderPattern = new StringBuilder("%s.%s(%s)");

        List<Object> buildParameters = new ArrayList<>();
        buildParameters.add(className);
        buildParameters.add(methodName);
        buildParameters.add(String.join(", ", parameterClassNames));

        if (dataProviderLineIndex != -1) {
            builderPattern.append("[%d]");
            buildParameters.add(dataProviderLineIndex);
        }

        if (instanceIndex != -1) {
            builderPattern.append(" ")
                          .append("(%d)");
            buildParameters.add(instanceIndex);
        }

        if (invocationIndex > 1) {
            builderPattern.append(" ")
                          .append("InvCount(%d)");
            buildParameters.add(invocationIndex);
        }

        return String.format(builderPattern.toString(), buildParameters.toArray());
    }

    public String asJsonString() {
        return gson.toJson(this);
    }

    public static TestInvocationContext fromJsonString(String jsonContext) {
        return gson.fromJson(jsonContext, TestInvocationContext.class);
    }
}
