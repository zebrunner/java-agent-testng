package com.zebrunner.agent.testng.core.config;

import org.testng.xml.XmlSuite;

import lombok.RequiredArgsConstructor;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ReportingConfiguration;

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
        return new ReportingConfiguration()
                .setProjectKey(rootXmlSuite.getParameter(PROJECT_KEY_PARAMETER))
                .setRun(
                        new ReportingConfiguration.RunConfiguration()
                                .setDisplayName(rootXmlSuite.getParameter(RUN_DISPLAY_NAME_PARAMETER))
                )
                .setNotification(
                        new ReportingConfiguration.Notification()
                                .setSlackChannels(rootXmlSuite.getParameter(NOTIFICATION_SLACK_CHANNELS_PARAMETER))
                                .setMsTeamsChannels(rootXmlSuite.getParameter(NOTIFICATION_MS_TEAMS_PARAMETER))
                                .setEmails(rootXmlSuite.getParameter(NOTIFICATION_EMAILS_PARAMETER))
                )
                .setMilestone(
                        new ReportingConfiguration.Milestone()
                                .setId(parseLong(rootXmlSuite.getParameter(MILESTONE_ID_PARAMETER)))
                                .setName(rootXmlSuite.getParameter(MILESTONE_NAME_PARAMETER))
                );
    }

}
