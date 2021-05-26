package com.zebrunner.agent.testng.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionUtils {

    public static String getStacktrace(Throwable throwable) {
        if (throwable != null) {
            StringWriter errorMessageStringWriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(errorMessageStringWriter));
            return errorMessageStringWriter.toString();
        } else {
            return "";
        }
    }

}
