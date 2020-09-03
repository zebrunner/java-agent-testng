package com.zebrunner.agent.testng.core;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestInvocationContext {

    private final static Gson GSON = new Gson();
    private static final int MAX_DISPLAY_NAME_LENGTH = 255;

    private final String thread = Thread.currentThread().getName();
    private String className;
    private String methodName;
    private String displayName;
    private List<String> parameters;
    private List<String> parameterClassNames;
    private int dataProviderIndex;
    private int instanceIndex;
    private int invocationIndex;

    public String buildDisplayName() {
        String displayName = this.displayName == null || this.displayName.isEmpty()
                ? this.methodName
                : this.displayName;

        StringBuilder displayNameBuilder = new StringBuilder(displayName);
        displayNameBuilder.append('(')
                          .append(String.join(", ", parameters))
                          .append(')');

        // if there is instance index or if the test method have been executed more than once within run
        if (instanceIndex != -1 || invocationIndex > 1) {
            displayNameBuilder.append(" [");

            // if there is instance index (will have non -1 value only if there are more then 1 instance)
            if (instanceIndex != -1) {
                // instance indexes start from 0, so the displayed value is adjusted to start from 1
                displayNameBuilder.append("Instance: ").append(instanceIndex + 1);
            }
            // if the test method have been executed more than once within run
            if (invocationIndex > 1) {
                // if there are also more then 1 instance, then append delimiter between these indexes
                if (instanceIndex != -1) {
                    displayNameBuilder.append(", ");
                }
                displayNameBuilder.append("Invocation: ").append(invocationIndex);
            }

            displayNameBuilder.append("]");
        }

        return displayNameBuilder.length() > 255
                ? displayNameBuilder.replace(126, displayNameBuilder.length() - 126, "...").toString()
                : displayNameBuilder.toString();
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
        StringBuilder builderPattern = new StringBuilder("[%s]: %s.%s(%s)");

        List<Object> buildParameters = new ArrayList<>();
        buildParameters.add(Thread.currentThread().getName());
        buildParameters.add(className);
        buildParameters.add(methodName);
        buildParameters.add(String.join(", ", parameterClassNames));

        if (dataProviderIndex != -1) {
            builderPattern.append("[%d]");
            buildParameters.add(dataProviderIndex);
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
        return GSON.toJson(this);
    }

    public static TestInvocationContext fromJsonString(String jsonContext) {
        return GSON.fromJson(jsonContext, TestInvocationContext.class);
    }

}
