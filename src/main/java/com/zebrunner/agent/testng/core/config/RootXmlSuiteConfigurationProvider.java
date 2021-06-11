package com.zebrunner.agent.testng.core.config;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import lombok.RequiredArgsConstructor;
import org.testng.xml.XmlSuite;

import static com.zebrunner.agent.core.config.ConfigurationUtils.parseLong;

@RequiredArgsConstructor
public class RootXmlSuiteConfigurationProvider implements ConfigurationProvider {

    private final static String PROJECT_KEY_PARAMETER = "reporting.project-key";
    private final static String RUN_DISPLAY_NAME_PARAMETER = "reporting.run.display-name";
    private final static String NOTIFICATION_SLACK_CHANNELS_PARAMETER = "reporting.notification.slack-channels";
    private final static String NOTIFICATION_MS_TEAMS_PARAMETER = "reporting.notification.ms-teams-channels";
    private final static String NOTIFICATION_EMAILS_PARAMETER = "reporting.notification.emails";
    private final static String MILESTONE_ID_PARAMETER = "reporting.milestone.id";
    private final static String MILESTONE_NAME_PARAMETER = "reporting.milestone.name";

    private final XmlSuite rootXmlSuite;

    @Override
    public ReportingConfiguration getConfiguration() {
        return ReportingConfiguration.builder()
                                     .projectKey(rootXmlSuite.getParameter(PROJECT_KEY_PARAMETER))
                                     .run(new ReportingConfiguration.RunConfiguration(
                                             rootXmlSuite.getParameter(RUN_DISPLAY_NAME_PARAMETER),
                                             null, null, null, null, null
                                     ))
                                     .notification(new ReportingConfiguration.NotificationConfiguration(
                                             rootXmlSuite.getParameter(NOTIFICATION_SLACK_CHANNELS_PARAMETER),
                                             rootXmlSuite.getParameter(NOTIFICATION_MS_TEAMS_PARAMETER),
                                             rootXmlSuite.getParameter(NOTIFICATION_EMAILS_PARAMETER)
                                     ))
                                     .milestone(new ReportingConfiguration.MilestoneConfiguration(
                                             parseLong(rootXmlSuite.getParameter(MILESTONE_ID_PARAMETER)),
                                             rootXmlSuite.getParameter(MILESTONE_NAME_PARAMETER)
                                     ))
                                     .build();
    }

}
