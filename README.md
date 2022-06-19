# Zebrunner TestNG agent
The official Zebrunner TestNG agent provides reporting and smart reruns functionality. No special configuration is required to enable the Zebrunner Listener for TestNG - service discovery mechanism will automatically register the listener as soon as it becomes available on the classpath of your test project.

> **Carina support**
> 
> Since Carina is a TestNG-based automation framework, all steps described in this doc for TestNG are valid for Carina as well.
> Official Carina documentation can be found [here](https://zebrunner.github.io/carina/).

## Inclusion into your project 

Including the agent into your project is easy - just add the dependency to the build descriptor.

<!-- tabs:start -->

#### **Gradle**
```groovy
dependencies {
  testImplementation 'com.zebrunner:agent-testng:1.6.4'
}
```

#### **Maven**
```xml
<dependency>
  <groupId>com.zebrunner</groupId>
  <artifactId>agent-testng</artifactId>
  <version>RELEASE</version>
  <scope>test</scope>
</dependency>
```

<!-- tabs:end -->

The following table shows compatibility matrix between Zebrunner Agent and the specific versions of TestNG.

| Agent version | Compatible TestNG versions |
|---------------|----------------------------|
| `1.7.*`       | `7.5`                      |
| `1.6.*`       | `7.3.0`, `7.4.0`           |

Zebrunner TestNG Agent is tightly coupled to specific versions of TestNG framework. We cannot guarantee that a particular version of the agent will work correctly with a version of TestNG other than the one listed in the table. The latest version of the agent comes bundled with TestNG 7.5.

## Tracking of test results
Once the agent is available on the classpath of your test project, it is **not** automatically enabled. The valid configuration must be provided.

It is currently possible to provide the configuration via:
1. Environment variables 
2. Program arguments 
3. YAML file
4. Properties file

The configuration lookup will be performed in the order listed above, meaning that environment configuration will always take precedence over YAML and so on.
It is also possible to override configuration parameters by passing them through a configuration provider with higher precedence.

<!-- groups:start -->

### Environment variables
The following configuration parameters are recognized by the agent:
- `REPORTING_ENABLED` - enables or disables reporting. The default value is `false`. If disabled, the agent will use no op component implementations that will simply log output for tracing purposes with the `trace` level;
- `REPORTING_SERVER_HOSTNAME` - mandatory if reporting is enabled. It is Zebrunner server hostname. It can be obtained in Zebrunner on the 'Account & profile' page under the 'Service URL' section;
- `REPORTING_SERVER_ACCESS_TOKEN` - mandatory if reporting is enabled. Access token must be used to perform API calls. It can be obtained in Zebrunner on the 'Account & profile' page under the 'Token' section;
- `REPORTING_PROJECT_KEY` - optional value. It is the project that the test run belongs to. The default value is `DEF`. You can manage projects in Zebrunner in the appropriate section;
- `REPORTING_RUN_DISPLAY_NAME` - optional value. It is the display name of the test run. The default value is `Default Suite`;
- `REPORTING_RUN_BUILD` - optional value. It is the build number that is associated with the test run. It can depict either the test build number or the application build number;
- `REPORTING_RUN_ENVIRONMENT` - optional value. It is the environment where the tests will run;
- `REPORTING_RUN_RETRY_KNOWN_ISSUES` - optional value. If set to `false` and test failed with an issue previously occurred for the test method, then the agent will ignore results of the `IRetryAnalyzer` assigned to test and stop retries. The default value is `true`;
- `REPORTING_NOTIFICATION_NOTIFY_ON_EACH_FAILURE` - optional value. Specifies whether Zebrunner should send notification to Slack/Teams on each test failure. The notifications will be sent even if the suite is still running. The default value is `false`;
- `REPORTING_NOTIFICATION_SLACK_CHANNELS` - optional value. The list of comma-separated Slack channels to send notifications to. Notification will be sent only if Slack integration is properly configured in Zebrunner with valid credentials for the project the tests are reported to. Zebrunner can send two type of notifications: on each test failure (if appropriate property is enabled) and on suite finish;
- `REPORTING_NOTIFICATION_MS_TEAMS_CHANNELS` - optional value. The list of comma-separated Microsoft Teams channels to send notifications to. Notification will be sent only if Teams integration is configured in Zebrunner project with valid webhooks for the channels. Zebrunner can send two type of notifications: on each test failure (if appropriate property is enabled) and on suite finish;
- `REPORTING_NOTIFICATION_EMAILS` - optional value. The list of comma-separated emails to send notifications to. This type of notification does not require further configuration on Zebrunner side. Unlike other notification mechanisms, Zebrunner can send emails only on suite finish;
- `REPORTING_MILESTONE_ID` - optional value. Id of the Zebrunner milestone to link the suite execution to. The id is not displayed on Zebrunner UI, so the field is basically used for internal purposes. If the milestone does not exist, appropriate warning message will be displayed in logs, but the test suite will continue executing;
- `REPORTING_MILESTONE_NAME` - optional value. Name of the Zebrunner milestone to link the suite execution to. If the milestone does not exist, appropriate warning message will be displayed in logs, but the test suite will continue executing.

### Program arguments
The following configuration parameters are recognized by the agent:
- `reporting.enabled` - enables or disables reporting. The default value is `false`. If disabled, the agent will use no op component implementations that will simply log output for tracing purposes with the `trace` level;
- `reporting.server.hostname` - mandatory if reporting is enabled. It is Zebrunner server hostname. It can be obtained in Zebrunner on the 'Account & profile' page under the 'Service URL' section;
- `reporting.server.accessToken` - mandatory if reporting is enabled. Access token must be used to perform API calls. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Token' section;
- `reporting.projectKey` - optional value. The project that the test run belongs to. The default value is `DEF`. You can manage projects in Zebrunner in the appropriate section;
- `reporting.run.displayName` - optional value. The display name of the test run. The default value is `Default Suite`;
- `reporting.run.build` - optional value. The build number that is associated with the test run. It can depict either the test build number or the application build number;
- `reporting.run.environment` - optional value. The environment in which the tests will run.
- `reporting.run.retry-known-issues` - optional value. If set to `false` and test failed with an issue previously occurred for the test method, then the agent will ignore results of the `IRetryAnalyzer` assigned to test and stop retries. The default value is `true`;
- `reporting.notification.notify-on-each-failure` - optional value. Specifies whether Zebrunner should send notification to Slack/Teams on each test failure. The notifications will be sent even if the suite is still running. The default value is `false`;
- `reporting.notification.slack-channels` - optional value. The list of comma-separated Slack channels to send notifications to. Notification will be sent only if Slack integration is properly configured in Zebrunner with valid credentials for the project the tests are reported to. Zebrunner can send two type of notifications: on each test failure (if appropriate property is enabled) and on suite finish;
- `reporting.notification.ms-teams-channels` - optional value. The list of comma-separated Microsoft Teams channels to send notifications to. Notification will be sent only if Teams integration is configured in Zebrunner project with valid webhooks for the channels. Zebrunner can send two type of notifications: on each test failure (if appropriate property is enabled) and on suite finish;
- `reporting.notification.emails` - optional value. The list of comma-separated emails to send notifications to. This type of notification does not require further configuration on Zebrunner side. Unlike other notification mechanisms, Zebrunner can send emails only on suite finish;
- `reporting.milestone.id` - optional value. Id of the Zebrunner milestone to link the suite execution to. The id is not displayed on Zebrunner UI, so the field is basically used for internal purposes. If the milestone does not exist, appropriate warning message will be displayed in logs, but the test suite will continue executing;
- `reporting.milestone.name` - optional value. Name of the Zebrunner milestone to link the suite execution to. If the milestone does not exist, appropriate warning message will be displayed in logs, but the test suite will continue executing.

### YAML file
Agent recognizes `agent.yaml` or `agent.yml` file in the resources root folder. It is currently not possible to configure an alternative file location.

Below is a sample configuration file:
```yaml
reporting:
  enabled: true
  project-key: WEB
  server:
    hostname: bestcompany.zebrunner.com
    access-token: <token>
  run:
    display-name: Nightly Regression Suite
    build: 2.5.3.96-SNAPSHOT
    environment: TEST-1
    retry-known-issues: false
  notification:
    notify-on-each-failure: true
    slack-channels: automation, dev-team
    ms-teams-channels: automation, qa-team
    emails: boss@example.com
  milestone:
    name: Release 2.5.3
```
- `reporting.enabled` - enables or disables reporting. The default value is `false`. If disabled, the agent will use no op component implementations that will simply log output for tracing purposes with the `trace` level;
- `reporting.server.hostname` - mandatory if reporting is enabled. Zebrunner server hostname. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Service URL' section;
- `reporting.server.access-token` - mandatory if reporting is enabled. Access token must be used to perform API calls. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Token' section;
- `reporting.project-key` - optional value. The project that the test run belongs to. The default value is `DEF`. You can manage projects in Zebrunner in the appropriate section;
- `reporting.run.display-name` - optional value. The display name of the test run. The default value is `Default Suite`;
- `reporting.run.build` - optional value. The build number that is associated with the test run. It can depict either the test build number or the application build number;
- `reporting.run.environment` - optional value. The environment in which the tests will run.
- `reporting.run.retry-known-issues` - optional value. If set to `false` and test failed with an issue previously occurred for the test method, then the agent will ignore results of the `IRetryAnalyzer` assigned to test and stop retries. The default value is `true`;
- `reporting.notification.notify-on-each-failure` - optional value. Specifies whether Zebrunner should send notification to Slack/Teams on each test failure. The notifications will be sent even if the suite is still running. The default value is `false`;
- `reporting.notification.slack-channels` - optional value. The list of comma-separated Slack channels to send notifications to. Notification will be sent only if Slack integration is properly configured in Zebrunner with valid credentials for the project the tests are reported to. Zebrunner can send two type of notifications: on each test failure (if appropriate property is enabled) and on suite finish;
- `reporting.notification.ms-teams-channels` - optional value. The list of comma-separated Microsoft Teams channels to send notifications to. Notification will be sent only if Teams integration is configured in Zebrunner project with valid webhooks for the channels. Zebrunner can send two type of notifications: on each test failure (if appropriate property is enabled) and on suite finish;
- `reporting.notification.emails` - optional value. The list of comma-separated emails to send notifications to. This type of notification does not require further configuration on Zebrunner side. Unlike other notification mechanisms, Zebrunner can send emails only on suite finish;
- `reporting.milestone.id` - optional value. Id of the Zebrunner milestone to link the suite execution to. The id is not displayed on Zebrunner UI, so the field is basically used for internal purposes. If the milestone does not exist, appropriate warning message will be displayed in logs, but the test suite will continue executing;
- `reporting.milestone.name` - optional value. Name of the Zebrunner milestone to link the suite execution to. If the milestone does not exist, appropriate warning message will be displayed in logs, but the test suite will continue executing.

### Properties file
The agent recognizes only `agent.properties` file in the resources root folder. It is currently not possible to configure an alternative file location.

Below is a sample configuration file:
```properties
reporting.enabled=true
reporting.project-key=WEB
reporting.server.hostname=bestcompany.zebrunner.com
reporting.server.access-token=<token>
reporting.run.display-name=Nightly Regression Suite
reporting.run.build=2.5.3.96-SNAPSHOT
reporting.run.environment=TEST-1
reporting.run.retry-known-issues=false
notification.notify-on-each-failure=true
notification.slack-channels=automation, dev-team
notification.ms-teams-channels=automation, qa-team
notification.emails=boss@example.com
milestone.name=Release 2.5.3
```
- `reporting.enabled` - enables or disables reporting. The default value is `false`. If disabled, the agent will use no op component implementations that will simply log output for tracing purposes with the `trace` level;
- `reporting.server.hostname` - mandatory if reporting is enabled. Zebrunner server hostname. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Service URL' section;
- `reporting.server.access-token` - mandatory if reporting is enabled. Access token must be used to perform API calls. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Token' section;
- `reporting.project-key` - optional value. The project that the test run belongs to. The default value is `DEF`. You can manage projects in Zebrunner in the appropriate section;
- `reporting.run.display-name` - optional value. The display name of the test run. The default value is `Default Suite`;
- `reporting.run.build` - optional value. The build number that is associated with the test run. It can depict either the test build number or the application build number;
- `reporting.run.environment` - optional value. The environment in which the tests will run.
- `reporting.run.retry-known-issues` - optional value. If set to `false` and test failed with an issue previously occurred for the test method, then the agent will ignore results of the `IRetryAnalyzer` assigned to test and stop retries. The default value is `true`;
- `reporting.notification.notify-on-each-failure` - optional value. Specifies whether Zebrunner should send notification to Slack/Teams on each test failure. The notifications will be sent even if the suite is still running. The default value is `false`;
- `reporting.notification.slack-channels` - optional value. The list of comma-separated Slack channels to send notifications to. Notification will be sent only if Slack integration is properly configured in Zebrunner with valid credentials for the project the tests are reported to. Zebrunner can send two type of notifications: on each test failure (if appropriate property is enabled) and on suite finish;
- `reporting.notification.ms-teams-channels` - optional value. The list of comma-separated Microsoft Teams channels to send notifications to. Notification will be sent only if Teams integration is configured in Zebrunner project with valid webhooks for the channels. Zebrunner can send two type of notifications: on each test failure (if appropriate property is enabled) and on suite finish;
- `reporting.notification.emails` - optional value. The list of comma-separated emails to send notifications to. This type of notification does not require further configuration on Zebrunner side. Unlike other notification mechanisms, Zebrunner can send emails only on suite finish;
- `reporting.milestone.id` - optional value. Id of the Zebrunner milestone to link the suite execution to. The id is not displayed on Zebrunner UI, so the field is basically used for internal purposes. If the milestone does not exist, appropriate warning message will be displayed in logs, but the test suite will continue executing;
- `reporting.milestone.name` - optional value. Name of the Zebrunner milestone to link the suite execution to. If the milestone does not exist, appropriate warning message will be displayed in logs, but the test suite will continue executing.

<!-- groups:end -->

Once the configuration is set up, the agent is ready to track your test run events, with no additional configuration required. However, to get the most out of tracking capabilities, it is also possible to configure the agent to collect additional test data such as logs, screenshots (if applicable) and more generic artifacts. Additionally, custom metadata can be attached to both test run itself and tests, improving your reporting experience in Zebrunner.

### Collecting test logs
It is also possible to enable the log collection for your tests. Currently, three logging frameworks are supported out of the box: **logback**, **log4j**, **log4j2**. We recommend using slf4j (Simple Logging Facade for Java) which provides abstraction over logging libraries.
All you have to do to enable logging is to register the reporting appender in your test framework configuration file.

<!-- groups:start -->

#### Logback
Add logback (and, optionally, slf4j) dependencies to your build descriptor.

<!-- tabs:start -->

#### **Gradle**
```groovy
dependencies {
  implementation 'org.slf4j:slf4j-api:1.7.30'
  implementation 'ch.qos.logback:logback-core:1.2.3'
  implementation 'ch.qos.logback:logback-classic:1.2.3'
}
```
#### **Maven**
```xml
<dependencies>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.30</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-core</artifactId>
        <version>1.2.3</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.2.3</version>
    </dependency>
</dependencies>
```

<!-- tabs:end -->

Add logging appender to `logback.xml` file. Feel free to customize the logging pattern according to your needs:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="ZebrunnerAppender" class="com.zebrunner.agent.core.logging.logback.ReportingAppender">
       <encoder>
          <pattern>%d{HH:mm:ss.SSS} [%t] %-5level - %msg%n</pattern>
       </encoder>
    </appender>
    <root level="info">
        <appender-ref ref="ZebrunnerAppender" />
    </root>
</configuration>
```

#### Log4j
Add log4j (and, optionally, slf4j) dependency to your build descriptor.

<!-- tabs:start -->

#### **Gradle**
```groovy
dependencies {
  implementation 'org.slf4j:slf4j-api:1.7.30'
  implementation 'log4j:log4j:1.2.17'
}
```
#### **Maven**
```xml
<dependencies>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.30</version>
    </dependency>
    <dependency>
        <groupId>log4j</groupId>
        <artifactId>log4j</artifactId>
        <version>1.2.17</version>
    </dependency>
</dependencies>
```

<!-- tabs:end -->

Add logging appender in `log4j.properties` file. Feel free to customize the logging pattern according to your needs:
```properties
log4j.rootLogger = INFO, zebrunner
log4j.appender.zebrunner=com.zebrunner.agent.core.logging.log4j.ReportingAppender
log4j.appender.zebrunner.layout=org.apache.log4j.PatternLayout
log4j.appender.zebrunner.layout.conversionPattern=pattern">[%d{HH:mm:ss}] %-5p (%F:%L) - %m%n
```

#### Log4j2
Add log4j2 (and, optionally, slf4j) dependency to your build descriptor:

<!-- tabs:start -->

#### **Gradle**
```groovy
dependencies {
  implementation 'org.slf4j:slf4j-api:1.7.30'
  implementation 'org.apache.logging.log4j:log4j-api:2.17.2'
  implementation 'org.apache.logging.log4j:log4j-core:2.17.2'
  implementation 'org.apache.logging.log4j:log4j-slf4j-impl:2.17.2'
}
```
#### **Maven**
```xml
<dependencies>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.36</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-api</artifactId>
        <version>2.17.2</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.17.2</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j-impl</artifactId>
        <version>2.17.2</version>
    </dependency>
</dependencies>
```

<!-- tabs:end -->

Add logging appender to `log4j2.xml` file. Feel free to customize the logging pattern according to your needs:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration packages="com.zebrunner.agent.core.logging.log4j2">
   <properties>
      <property name="pattern">[%d{HH:mm:ss}] %-5p (%F:%L) - %m%n</property>
   </properties>
   <appenders>
      <ReportingAppender name="ReportingAppender">
         <PatternLayout pattern="${pattern}" />
      </ReportingAppender>
   </appenders>
   <loggers>
      <root level="info">
         <appender-ref ref="ReportingAppender"/>
      </root>
   </loggers>
</configuration>
```

<!-- groups:end -->

#### Logger usage
No additional steps are required to collect test logs and track them in Zebrunner.
```java
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwesomeTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void awesomeTest() {
        LOGGER.info("Test info");
    }

}
```

### Collecting captured screenshots
In case you are using TestNG as a UI testing framework, it may be useful to have an ability to track captured screenshots in scope of Zebrunner reporting.
The agent comes with a Java API allowing you to send your screenshots to Zebrunner, so they will be attached to the test.

Below is a sample code of test sending a screenshot to Zebrunner:
```java
import com.zebrunner.agent.core.registrar.Screenshot;
import org.testng.annotations.Test;

public class AwesomeTests {

    @Test
    public void myAwesomeTest() {
        byte[] screenshotBytes = // capture screenshot
        Screenshot.upload(screenshotBytes, capturedAtMillis);
        // meaningful assertions
    }

}
```

A screenshot should be passed as a byte array along with a unix timestamp in milliseconds corresponding to the moment when the screenshot was captured. 
If `null` is supplied instead of a timestamp, it will be generated automatically. However, it is recommended to use an accurate timestamp in order to get accurate tracking.

The uploaded screenshot will appear among test logs. The actual position depends on the provided (or generated) timestamp.

### Collecting additional artifacts
In case your tests or entire test run produce some artifacts, it may be useful to track them in Zebrunner. The agent comes with a few convenient methods for uploading artifacts in Zebrunner and linking them to the currently running test or the test run.

Artifacts can be uploaded using the `Artifact` class. This class has a bunch of static methods to either attach an arbitrary artifact reference or upload artifacts represented by any Java type associated with the files. 

The `#attachToTestRun(name, file)` and `#attachToTest(name, file)` methods can be used to upload and attach an artifact file to test run and test respectively.

The `#attachReferenceToTestRun(name, reference)` and `#attachReferenceToTest(name, reference)` methods can be used to attach an arbitrary artifact reference to test run and test respectively.

Together with an artifact or artifact reference, you must provide the display name. For the file, this name must contain the file extension that reflects the actual content of the file. If the file extension does not match the file content, this file will not be saved in Zebrunner. Artifact reference can have an arbitrary name.

Here is a sample test that uploads 3 artifacts for test, 1 artifact for test run and attaches 1 artifact reference to test and 1 to test run:
```java
import java.io.InputStream;
import java.io.File;
import java.nio.file.Path;

import com.zebrunner.agent.core.registrar.Artifact;
import org.testng.annotations.Test;

public class AwesomeTests {

    @Test
    public void awesomeTest() {
        // some code here
        InputStream inputStream;
        byte[] byteArray;
        File file;
        Path path;
        
        Artifact.attachToTestRun("file.docx", inputStream);
        
        Artifact.attachToTest("image.png", byteArray);
        Artifact.attachToTest("application.apk", file);
        Artifact.attachToTest("test-log.txt", path);
        
        Artifact.attachReferenceToTestRun("Zebrunner in Github", "https://github.com/zebrunner");
        
        Artifact.attachReferenceToTest("zebrunner.com", "https://zebrunner.com/");
        // meaningful assertions
    }

}
```
Artifact upload process is performed in the background, so it will not affect test execution.
The uploaded artifacts will appear under the test or test run name in the run results in Zebrunner.

### Tracking test maintainer
You may want to add transparency to the process of automation maintenance by having an engineer responsible for evolution of specific tests or test classes.
Zebrunner comes with a concept of a maintainer - a person that can be assigned to maintain tests. In order to keep track of those, the agent comes with the `@Maintainer` annotation.

This annotation can be placed on both test class and method. It is also possible to override a class-level maintainer on a method-level. If a base test class is marked with this annotation, all child classes will inherit the annotation unless they have an explicitly specified one.

See a sample test class below:
```java
import com.zebrunner.agent.core.reporting.Maintainer;
import org.testng.annotations.Test;

@Maintainer("kenobi")
public class AwesomeTests {

    @Test
    @Maintainer("skywalker")
    public void awesomeTest() {
        // meaningful assertions
    }

    @Test
    public void anotherAwesomeTest() {
        // meaningful assertions
    }


}
```

In the example above, `kenobi` will be reported as a maintainer of `anotherAwesomeTest` (class-level value taken into account), while `skywalker` will be reported as a maintainer of test `awesomeTest`.

The maintainer username should be a valid Zebrunner username, otherwise it will be set to `anonymous`.

### Attaching labels
In some cases, it may be useful to attach some meta information related to a test.

The agent comes with a concept of labels. Label is a key-value pair associated with a test or test run. The key is represented by a `String`, the label value accepts a vararg of `Strings`.

There is a repeatable `@TestLabel` annotation that can be used to attach labels to a test. The annotation can be used on both class and method levels and will attach labels to test. It is also possible to override a class-level label on a method-level.

There is also a Java API to attach labels during test or execution. The `Label` class has static methods that can be used to attach labels.

Here is a sample:
```java
import com.zebrunner.agent.core.annotation.JiraReference;
import com.zebrunner.agent.core.annotation.Priority;
import com.zebrunner.agent.core.annotation.TestLabel;
import com.zebrunner.agent.core.registrar.Label;
import org.testng.annotations.Test;

public class AwesomeTests {

    @Test
    @TestLabel(name = "feature", value = "labels")
    @TestLabel(name = "app", value = {"reporting-service:v1.0", "reporting-service:v1.1"})
    public void awesomeTest() {
        // some code here  
        Label.attachToTest("Chrome", "85.0");
        Label.attachToTestRun("Author", "Deve Loper");
        // meaningful assertions
    }

}
```
The test from the sample above attaches 4 test-level labels (2 `app` labels, 1 `feature` label, 1 `Chrome` label) and 1 run-level label (`Author`).

The values of attached labels will be displayed in Zebrunner under the name of a corresponding test or run.

### Reverting test registration
In some cases it might be handy not to register test execution in Zebrunner. This may be caused by very special circumstances of test environment or execution conditions.

Zebrunner agent comes with a convenient method `#revertRegistration()` from `CurrentTest` class for managing test registration at runtime. The following code snippet shows a case where test is not reported on Monday.
```java
import com.zebrunner.agent.core.registrar.CurrentTest;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class AwesomeTests {

    @Test
    public void awesomeTest() {
        // some code here  
        if (LocalDate.now().getDayOfWeek() == DayOfWeek.MONDAY) {
            CurrentTest.revertRegistration();
        }
        // meaningful assertions
    }

}
```

It is worth mentioning that the method invocation does not affect the test execution, but simply unregisters the test in Zebrunner. To interrupt the test execution, you need to do additional actions, for example, throw a `SkipException`.

### Setting Test Run build at runtime
All the configuration mechanisms listed above provide possibility to declaratively set test run build. But there might be cases when actual build becomes available only at runtime.

For such cases Zebrunner agent has a special method that can be used at any moment of the suite execution:
```java
import com.zebrunner.agent.core.registrar.CurrentTestRun;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class AwesomeTests {

    @BeforeSuite
    public void setUp() {
        String build = resolveBuild();
        CurrentTestRun.setBuild(build);
    }

    private String resolveBuild() {
        // some code here
    }

}
```

In the above example, the `#setUp()` method on `@BeforeSuite` phase resolves and sets the test run build.

### Setting Test Run locale
If you want to get full reporting experience and collect as much information in Zebrunner as its possible, you may want to report the test run locale.

For this, Zebrunner agent has a special method that can be used at any moment of the suite execution:
```java
import com.zebrunner.agent.core.registrar.CurrentTestRun;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class AwesomeTests {

    @BeforeSuite
    public void setUp() {
        String locale = resolveLocale();
        CurrentTestRun.setLocale(locale);
    }

    private String resolveLocale() {
        // some code here
    }

}
```

In the above example, the `#setUp()` method on `@BeforeSuite` phase resolves and sets the test run locale.

### Overriding Test Run platform
A test run in Zebrunner may have platform associated with the run. If there is at least one initiated `RemoteDriverSession` within the test run, then its platform will be displayed as a platform of the whole test run. Even if subsequent `RemoteDriverSession`s are initiated on another platform, the very first one will be displayed as the run platform.

In some cases you may want to override the platform of the first `RemoteDriverSession`. Another problem is that it is not possible to specify `API` as a platform.

Zebrunner provides two special methods to solve both of these problems: `CurrentTestRun.setPlatform(name)` and `CurrentTestRun.setPlatform(name, version)`.

In the example below, the `#setUp()` method sets the API as a platform associated with the current test run.
```java
import com.zebrunner.agent.core.registrar.CurrentTestRun;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class AwesomeTests {

    @BeforeSuite
    public void setUp() {
        CurrentTestRun.setPlatform("API");
    }

}
```

### Upload test results to external test case management systems
Zebrunner provides an ability to upload test results to external TCMs on test run finish. For some TCMs it is possible to upload results in real-time during the test run execution. 

Currently, Zebrunner supports TestRail, Xray, Zephyr Squad and Zephyr Scale test case management systems.

<!-- tabs:start -->

#### Testrail

For successful upload of test run results in TestRail, two steps must be performed:
1) Integration with TestRail is configured and enabled for Zebrunner project;
2) Configuration is performed on the tests side.

##### Configuration

Zebrunner agent has a special `TestRail` class with a bunch of methods to control results upload:
- `#setSuiteId(String)` - mandatory. The method sets TestRail suite id for current test run. This method must be invoked before all tests. Thus, it should be invoked from `@BeforeSuite` method. If your suite is composed of multiple suites, you should invoke this method only for the first sub-suite;
- `#setCaseId(String)` or `@TestRailCaseId(array of Strings)` - mandatory. Using these mechanisms you can set TestRail's case associated with specific automated test. It is highly recommended using the `@TestRailCaseId` annotation instead of static method invocation. Use the static method only for special cases;
- `#disableSync()` - optional. Disables result upload. Same as `#setSuiteId(String)`, this method must be invoked before all tests;
- `#includeAllTestCasesInNewRun()` - optional. Includes all cases from suite into newly created run in TestRail. Same as `#setSuiteId(String)`, this method must be invoked before all tests;
- `#enableRealTimeSync()` - optional. Enables real-time results upload. In this mode, result of test execution will be uploaded immediately after test finish. This method also automatically invokes `#includeAllTestCasesInNewRun()`. Same as `#setSuiteId(String)`, this method must be invoked before all tests;
- `#setRunId(String)` - optional. Adds result into existing TestRail run. If not provided, test run is treated as new. Same as `#setSuiteId(String)`, this method must be invoked before all tests;
- `#setRunName(String)` - optional. Sets custom name for new TestRail run. By default, Zebrunner test run name is used. Same as `#setSuiteId(String)`, this method must be invoked before all tests;
- `#setMilestone(String)` - optional. Adds result in TestRail milestone with the given name. Same as `#setSuiteId(String)`, this method must be invoked before all tests;
- `#setAssignee(String)` - optional. Sets TestRail run assignee. Same as `#setSuiteId(String)`, this method must be invoked before all tests.

By default, a new run containing only cases assigned to the tests will be created in TestRail on test run finish.

##### Example

In the example below, a new run with name "Best run ever" will be created in TestRail on test run finish. Suite id is `321` and assignee is "Deve Loper". Results of the `awesomeTest1` will be uploaded as result of cases with id `10000`, `10001`, `10002`. Results of the `awesomeTest2` will be uploaded as result of case with id `20000`. 

```java
import com.zebrunner.agent.core.annotation.TestRailCaseId;
import com.zebrunner.agent.core.registrar.TestRail;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class AwesomeTests {

    @BeforeSuite
    public void setUp() {
        TestRail.setSuiteId("321");
        TestRail.setRunName("Best run ever");
        TestRail.setAssignee("Deve Loper");
    }

    @Test
    @TestRailCaseId("10000")
    @TestRailCaseId({"10001", "10002"})
    public void awesomeTest1() {
        // some code here
    }

    @Test
    public void awesomeTest2() {
        // some code here
        TestRail.setCaseId("20000");
        // meaningful assertions
    }

}
```

#### Xray

For successful upload of test run results in Xray two steps must be performed:
1) Xray integration is configured and enabled in Zebrunner project
2) Xray configuration is performed on the tests side

##### Configuration

Zebrunner agent has a special `Xray` class with a bunch of methods to control results upload:
- `#setExecutionKey(String)` - mandatory. The method sets Xray execution key. This method must be invoked before all tests. Thus, it should be invoked from `@BeforeSuite` method. If your suite is composed of multiple suites, you should invoke this method only for the first sub-suite;
- `#setTestKey(String)` or `@XrayTestKey(array of Strings)` - mandatory. Using these mechanisms you can set test keys associated with specific automated test. It is highly recommended using the `@XrayTestKey` annotation instead of static method invocation. Use the static method only for special cases;
- `#disableSync()` - optional. Disables result upload. Same as `#setExecutionKey(String)`, this method must be invoked before all tests;
- `#enableRealTimeSync()` - optional. Enables real-time results upload. In this mode, result of test execution will be uploaded immediately after test finish. Same as `#setExecutionKey(String)`, this method must be invoked before all tests.

By default, results will be uploaded to Xray on test run finish.

##### Example

In the example below, results will be uploaded to execution with key `ZBR-42`. Results of the `awesomeTest1` will be uploaded as result of tests with key `ZBR-10000`, `ZBR-10001`, `ZBR-10002`. Results of the `awesomeTest2` will be uploaded as result of test with key `ZBR-20000`.

```java
import com.zebrunner.agent.core.annotation.TestRailCaseId;
import com.zebrunner.agent.core.registrar.Xray;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class AwesomeTests {

    @BeforeSuite
    public void setUp() {
        Xray.setExecutionKey("ZBR-42");
    }

    @Test
    @XrayTestKey("ZBR-10000")
    @XrayTestKey({"ZBR-10001", "ZBR-10002"})
    public void awesomeTest1() {
        // some code here
    }

    @Test
    public void awesomeTest2() {
        // some code here
        Xray.setTestKey("ZBR-20000");
        // meaningful assertions
    }

}
```

#### Zephyr Squad & Zephyr Scale

For successful upload of test run results in Zephyr two steps must be performed:
1) Zephyr integration is configured and enabled in Zebrunner project
2) Zephyr configuration is performed on the tests side

##### Configuration

Zebrunner agent has a special `Zephyr` class with a bunch of methods to control results upload:
- `#setTestCycleKey(String)` - mandatory. The method sets Zephyr test cycle key. This method must be invoked before all tests. Thus, it should be invoked from `@BeforeSuite` method. If your suite is composed of multiple suites, you should invoke this method only for the first sub-suite;
- `#setJiraProjectKey(String)` - mandatory. Sets Zephyr Jira project key. Same as `#setTestCycleKey(String)`, this method must be invoked before all tests;
- `#setTestCaseKey(String)` or `@ZephyrTestCaseKey(array of Strings)` - mandatory. Using these mechanisms you can set test case keys associated with specific automated test. It is highly recommended using the `@ZephyrTestCaseKey` annotation instead of static method invocation. Use the static method only for special cases;
- `#disableSync()` - optional. Disables result upload. Same as `#setTestCycleKey(String)`, this method must be invoked before all tests;
- `#enableRealTimeSync()` - optional. Enables real-time results upload. In this mode, result of test execution will be uploaded immediately after test finish. Same as `#setTestCycleKey(String)`, this method must be invoked before all tests.

By default, results will be uploaded to Zephyr on test run finish.

##### Example

In the example below, results will be uploaded to test cycle with key `ZBR-R42` from project with key `ZBR`. Results of the `awesomeTest1` will be uploaded as result of tests with key `ZBR-T10000`, `ZBR-T10001`, `ZBR-T10002`. Results of the `awesomeTest2` will be uploaded as result of test with key `ZBR-T20000`.

```java
import com.zebrunner.agent.core.annotation.TestRailCaseId;
import com.zebrunner.agent.core.registrar.Zephyr;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class AwesomeTests {

    @BeforeSuite
    public void setUp() {
        Zephyr.setTestCycleKey("ZBR-R42");
        Zephyr.setJiraProjectKey("ZBR");
    }

    @Test
    @ZephyrTestCaseKey("ZBR-T10000")
    @ZephyrTestCaseKey({"ZBR-T10001", "ZBR-T10002"})
    public void awesomeTest1() {
        // some code here
    }

    @Test
    public void awesomeTest2() {
        // some code here
        Zephyr.setTestCaseKey("ZBR-T20000");
        // meaningful assertions
    }

}
```

<!-- tabs:end -->

## Tracking of web driver sessions
The Zebrunner test agent has a great ability to track tests along with remote driver sessions. After providing additional configuration, the agent captures all events of `RemoteDriverSession` instances (or instances of its subclasses) and reports them to Zebrunner.

### Configuration
Firstly, add a `byte-buddy` dependency with version `1.10.18` or higher to your project. It is required to proxy method invocations of `RemoteDriverSession` in order to track session start and end events and their metadata. Your project classpath may already contain `byte-buddy` dependency (either declared explicitly or fetched transitively), but it is crucial to make sure that its version is `1.10.18` or higher in order to guarantee proper work with JDK11+.

<!-- tabs:start -->

#### **Gradle**
```groovy
dependencies {
    // other project dependencies
    
    testImplementation("net.bytebuddy:byte-buddy:1.10.18")

    // other project dependencies
}
```

#### **Maven**
```xml
<dependencies>
    <!-- other project dependencies -->

    <dependency>
        <groupId>net.bytebuddy</groupId>
        <artifactId>byte-buddy</artifactId>
        <version>1.10.18</version>
        <scope>test</scope>
    </dependency>

    <!-- other project dependencies -->
</dependencies>
```

<!-- tabs:end -->

Secondly, you need to add a VM argument referencing the core Zebrunner agent jar file. This can be done in several ways: using a build tool (Maven or Gradle) or directly from the IDE.

<!-- groups:start -->

#### Gradle
Gradle provides support for adding a VM argument out of the box. The only thing you need to do is to add the `jvmArgs` property to the `test` task. Value of this property must point to the local path to the Zebrunner agent.

The following code snippet shows the content of the `build.gradle` file.

```groovy
def coreAgentArtifact = configurations.testRuntimeClasspath.resolvedConfiguration.resolvedArtifacts.find { it.name == 'agent-core' }
test.doFirst {
    jvmArgs "-javaagent:${coreAgentArtifact.file}"
}
```

#### Maven dependency plugin
The `maven-surefire-plugin` provides the ability to add VM arguments in a convenient way. You only need to provide the absolute path to the jar file with the Zebrunner agent.

The `maven-dependency-plugin` can be used to obtain the absolute path to a project's dependency. The `properties` goal of this plugin supplies a set of properties with paths to all project dependencies. If your project is already using the `maven-dependency-plugin`, this is the best way to go.

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.1.2</version>
        <executions>
            <execution>
                <goals>
                    <goal>properties</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.0.0-M4</version>
        <configuration>
            <argLine>-javaagent:${com.zebrunner:agent-core:jar}</argLine>
        </configuration>
    </plugin>
</plugins>
```

The `${com.zebrunner:agent-core:jar}` property is generated by the `maven-dependency-plugin` during the initialization phase. Maven automatically sets the generated value when `maven-surefire-plugin` launches tests.

#### Maven antrun plugin
The `maven-surefire-plugin` provides the ability to add VM arguments in a convenient way. You only need to provide the absolute path to the jar file with the Zebrunner agent.

The `maven-antrun-plugin` can be used to obtain the absolute path to a project dependency. If your project is already using the `maven-antrun-plugin`, this is the best way to go.

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-antrun-plugin</artifactId>
        <version>1.8</version>
        <executions>
            <execution>
                <phase>initialize</phase>
                <configuration>
                    <exportAntProperties>true</exportAntProperties>
                    <tasks>
                        <basename file="${maven.dependency.com.zebrunner.agent-core.jar.path}"
                                  property="com.zebrunner:agent-core:jar"/>
                    </tasks>
                </configuration>
                <goals>
                    <goal>run</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.0.0-M4</version>
        <configuration>
            <argLine>-javaagent:${com.zebrunner:agent-core:jar}</argLine>
        </configuration>
    </plugin>
</plugins>
```

The `${com.zebrunner:agent-core:jar}` property is generated by the `maven-antrun-plugin` during the initialization phase. Maven automatically sets the generated value when `maven-surefire-plugin` launches tests.

#### IDE
Most modern IDEs provide the ability to run tests locally along with specifying environment and/or VM arguments. This allows to run tests locally on dev machines and report the results to Zebrunner.

**We strongly recommend not to run tests locally using IDE support along with Zebrunner agent, but instead use the build tool support.**

To add a VM argument to run via the IDE, open the run configuration for tests. Then find the VM arguments setting and append the following line to the property value.

`-javaagent:<path-to-core-agent-jar>`

The value you append to VM arguments setting must contain a valid path to your local agent-core jar file. In most cases, this jar has been downloaded by your IDE and saved in the local repository of your build tool.

With Maven, this path should have the following pattern: `<path-to-user-folder>/.m2/repository/com/zebrunner/agent-core/<agent-core-version>/agent-core-<agent-core-version>.jar`.

With Gradle, this path should have the following pattern: `<path-to-user-folder>/.gradle/caches/modules-2/files-2.1/com.zebrunner/agent-core/<agent-core-version>/agent-core-<agent-core-version>.jar`.

#### IDE with Gradle
If a test project is imported into IDE as Gradle project, it should be enough to apply normal Gradle configuration for the Zebrunner agent.

**We strongly recommend not to run tests locally using IDE support along with Zebrunner agent, but instead use the build tool support.**

Just add the following config to the `build.gradle` file.

```groovy
def coreAgentArtifact = configurations.testRuntimeClasspath.resolvedConfiguration.resolvedArtifacts.find { it.name == 'agent-core' }
test.doFirst {
    jvmArgs "-javaagent:${coreAgentArtifact.file}"
}
```

#### IDE with Maven
If a test project is imported into IDE as Maven project, some routine configuration is required to run tests using IDE support.

**We strongly recommend not to run tests locally using IDE support along with Zebrunner agent, but instead use the build tool support.**

The proposed solution is based on an approach with dependency plugin, but with additional configuration.

```xml

<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.1.2</version>
        <executions>
            <execution>
                <id>copy</id>
                <phase>initialize</phase>
                <goals>
                    <goal>copy</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <artifactItems>
                <artifactItem>
                    <groupId>com.zebrunner</groupId>
                    <artifactId>agent-core</artifactId>
                    <version>RELEASE</version>
                    <outputDirectory>${project.build.directory}/agent</outputDirectory>
                    <destFileName>zebrunner-core-agent.jar</destFileName>
                </artifactItem>
            </artifactItems>
        </configuration>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.0.0-M4</version>
        <configuration>
            <argLine>-javaagent:${project.build.directory}/agent/zebrunner-core-agent.jar</argLine>
        </configuration>
    </plugin>
</plugins>
```

The `maven-dependency-plugin` from preceding code snippet copies the jar file with the core Zebrunner agent to the project build directory during initialization phase. The `maven-surefire-plugin` then uses the copied version of the Zebrunner agent as java instrumentation agent.

In some cases, it may not be enough to simply apply such a configuration, but you also need to manually start the initialization phase at first.

<!-- groups:end -->

Once the configuration is in place the agent will automatically report to Zebrunner web driver session events. However, it is also possible to collect additional test session artifacts in order to improve overall reporting experience.

### Session artifacts
Zebrunner supports 3 types of test session artifacts:
- Video recording
- Session log
- VNC streaming

Test agent itself does not capture those artifacts since it has no control over underlying Selenium Hub or MCloud implementation, however, it is possible to attach appropriate artifact references by providing specially designed set of driver session capabilities (**enabling capabilities**) - see the table below for more details. Only the `true` value is considered as trigger to save the link.

| Artifact        | Display name | Enabling capability | Default reference                                  | Reference overriding capability |
| --------------- | ------------ | ------------------- | -------------------------------------------------- | ------------------------------- |
| Video recording | Video        | enableVideo         | `artifacts/test-sessions/<session-id>/video.mp4`   | videoLink                       |
| Session log     | Log          | enableLog           | `artifacts/test-sessions/<session-id>/session.log` | logLink                         |
| VNC streaming   |              | enableVNC           | `<provider-integration-host>/ws/vnc/<session-id>`  | vncLink                         |

The **display name** is the name of the artifact that will be displayed on Zebrunner UI. This value is predefined and unfortunately can not be changed at the moment.

The **default reference** is a reference to a location, where artifact is **expected to reside** in S3-compatible storage once created by test environment - it is important that it stays in sync with test environment configuration. It is possible to override references if needed by providing **reference overriding capabilities**. Note, that special `<session-id>` placeholder is supported and can be used as part of the value of those capabilities allowing runtime session id (generated by Web Driver) to be included into actual reference value.

#### VNC streaming
VNC is an artifact of a special type. They don't have a name and are not displayed among other artifacts. They are displayed in the video section on Zebrunner UI during session execution and are dropped off on session close.

Default reference to the VNC streaming is based on `provider` capability. Value of this capability will be converted to preconfigured integration from **Test Environment Provider** group. The resolved integration must have a filled in URL property and be enabled in order to save the link to VNC streaming. The `<provider-integration-host>` placeholder of the default link will be replaced by the host of the obtained integration URL. Also, the `http` protocol in the VNC streaming url will be automatically replaced by `ws`, and `https` protocol will be replaced by `wss`. Currently, we only support Selenium, Zebrunner and MCloud integrations.

### Examples
The following code snippet shows the creation of `RemoteWebDriver` with enabled VNC streaming, video recording and session log artifacts, but overrides the link to VNC streaming. The `<session-id>` placeholder of `vncLink` capability will be replaced by the actual session id.
```java
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class WebDriverManager {

    public RemoteWebDriver initWebDriver() throws MalformedURLException {
        ChromeOptions capabilities = new ChromeOptions();
        capabilities.setCapability("enableVNC", "true");
        capabilities.setCapability("vncLink", "wss://example.com/vnc/<session-id>");
        capabilities.setCapability("enableVideo", "true");
        capabilities.setCapability("enableLog", "true");

        return new RemoteWebDriver(new URL("https://user:pass@example.com/wd/hub"), capabilities);
    }

}
```

The following code snippet shows the creation of `RemoteWebDriver` with enabled video and session log artifacts, overridden link to VNC streaming and session log, but disabled VNC streaming. As a result, VNC streaming will not be available, video recording will have default link with populated placeholders, and session log will have custom link with populated `<session-id>` placeholder.
```java
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class WebDriverManager {

    public RemoteWebDriver initWebDriver() throws MalformedURLException {
        ChromeOptions capabilities = new ChromeOptions();
        capabilities.setCapability("enableVNC", "false");
        capabilities.setCapability("vncLink", "wss://example.com/vnc/<session-id>");
        capabilities.setCapability("enableVideo", "true");
        capabilities.setCapability("enableLog", "true");
        capabilities.setCapability("logLink", "https://example.com/driver-sessions/logs/<session-id>.log");

        return new RemoteWebDriver(new URL("https://user:pass@example.com/wd/hub"), capabilities);
    }

}
```

## Contribution
To check out the project and build from the source, do the following:
```
git clone git://github.com/zebrunner/java-agent-testng.git
cd java-agent-testng
./gradlew build
```

## License
Zebrunner Reporting service is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
