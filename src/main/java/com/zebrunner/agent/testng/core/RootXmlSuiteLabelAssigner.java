package com.zebrunner.agent.testng.core;

import com.zebrunner.agent.core.registrar.Label;
import org.testng.xml.XmlSuite;

import java.util.HashMap;
import java.util.Map;

public class RootXmlSuiteLabelAssigner {

    private static final RootXmlSuiteLabelAssigner INSTANCE = new RootXmlSuiteLabelAssigner();

    public static RootXmlSuiteLabelAssigner getInstance() {
        return INSTANCE;
    }

    private static final Map<String, String> PARAMETER_NAME_TO_LABEL_NAME = new HashMap<String, String>() {
        {
            put("reporting.tcm.xray.test-execution-key", "com.zebrunner.app/tcm.xray.test-execution-key");
            put("reporting.tcm.testrail.assignee", "com.zebrunner.app/tcm.testrail.assignee");
            put("reporting.tcm.testrail.milestone", "com.zebrunner.app/tcm.testrail.milestone");
            put("reporting.tcm.testrail.project-id", "com.zebrunner.app/tcm.testrail.project-id");
            put("reporting.tcm.testrail.suite-id", "com.zebrunner.app/tcm.testrail.suite-id");
            put("reporting.tcm.qtest.project-id", "com.zebrunner.app/tcm.qtest.project-id");
            put("reporting.tcm.qtest.cycle-name", "com.zebrunner.app/tcm.qtest.cycle-name");
        }
    };

    public void assignTestRunLabels(XmlSuite suite) {
        for (String parameterName : PARAMETER_NAME_TO_LABEL_NAME.keySet()) {
            String parameter = suite.getParameter(parameterName);

            if (parameter != null && !parameter.trim().isEmpty()) {
                Label.attachToTestRun(PARAMETER_NAME_TO_LABEL_NAME.get(parameterName), parameter);
            }
        }
    }

}
