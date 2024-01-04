package com.zebrunner.agent.testng.core.config;

import com.zebrunner.agent.core.config.ConfigurationProvider;
import com.zebrunner.agent.core.config.ConfigurationUtils;
import com.zebrunner.agent.core.config.ReportingConfiguration;
import com.zebrunner.agent.core.registrar.domain.SummarySendingPolicy;
import lombok.RequiredArgsConstructor;
import org.testng.xml.XmlSuite;

@RequiredArgsConstructor
public class RootXmlSuiteConfigurationProvider implements ConfigurationProvider {

    private static final String PROJECT_KEY_PARAMETER = "reporting.project-key";
    private static final String RUN_DISPLAY_NAME_PARAMETER = "reporting.run.display-name";
    private static final String NOTIFICATION_SLACK_CHANNELS_PARAMETER = "reporting.notification.slack-channels";
    private static final String NOTIFICATION_MS_TEAMS_PARAMETER = "reporting.notification.ms-teams-channels";
    private static final String NOTIFICATION_EMAILS_PARAMETER = "reporting.notification.emails";
    private static final String NOTIFICATION_SUMMARY_SENDING_POLICY_PARAMETER = "reporting.notification.summary-sending-policy";
    private static final String MILESTONE_ID_PARAMETER = "reporting.milestone.id";
    private static final String MILESTONE_NAME_PARAMETER = "reporting.milestone.name";

    private final XmlSuite rootXmlSuite;

    @Override
    public ReportingConfiguration getConfiguration() {
        return ReportingConfiguration.builder()
                                     .projectKey(rootXmlSuite.getParameter(PROJECT_KEY_PARAMETER))
                                     .run(this.getRunConfiguration())
                                     .notification(this.getNotificationConfiguration())
                                     .milestone(this.getMilestoneConfiguration())
                                     .tcm(new ReportingConfiguration.TcmConfiguration())
                                     .build();
    }

    private ReportingConfiguration.RunConfiguration getRunConfiguration() {
        ReportingConfiguration.RunConfiguration config = new ReportingConfiguration.RunConfiguration();

        config.setDisplayName(rootXmlSuite.getParameter(RUN_DISPLAY_NAME_PARAMETER));

        return config;
    }

    private ReportingConfiguration.NotificationConfiguration getNotificationConfiguration() {
        ReportingConfiguration.NotificationConfiguration config = new ReportingConfiguration.NotificationConfiguration();

        config.setSlackChannels(rootXmlSuite.getParameter(NOTIFICATION_SLACK_CHANNELS_PARAMETER));
        config.setMsTeamsChannels(rootXmlSuite.getParameter(NOTIFICATION_MS_TEAMS_PARAMETER));
        config.setEmails(rootXmlSuite.getParameter(NOTIFICATION_EMAILS_PARAMETER));

        String sendingPolicyParameter = rootXmlSuite.getParameter(NOTIFICATION_SUMMARY_SENDING_POLICY_PARAMETER);
        config.setSummarySendingPolicy(ConfigurationUtils.parseEnum(sendingPolicyParameter, SummarySendingPolicy.class));

        return config;
    }

    private ReportingConfiguration.MilestoneConfiguration getMilestoneConfiguration() {
        ReportingConfiguration.MilestoneConfiguration config = new ReportingConfiguration.MilestoneConfiguration();

        config.setId(ConfigurationUtils.parseLong(rootXmlSuite.getParameter(MILESTONE_ID_PARAMETER)));
        config.setName(rootXmlSuite.getParameter(MILESTONE_NAME_PARAMETER));

        return config;
    }

}
