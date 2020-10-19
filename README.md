# Zebrunner TestNG agent
The official Zebrunner TestNG agent provides reporting and smart reruns functionality. No special configuration is required to enable the Zebrunner Listener for TestNG - service discovery mechanism will automatically register the listener as soon as it becomes available on the classpath of your test project.

## Inclusion into your project 
The agent comes bundled with TestNG 7.3.0, so you should comment out or exclude your TestNG dependency from the project. If you are using a version of TestNG below 7.3.0, we cannot guarantee the correct functionality of the agent.

Including the agent into your project is easy - just add the dependency to the build descriptor.

<!-- tabs:start -->

#### **Gradle**
```groovy
dependencies {
  testImplementation 'com.zebrunner:agent-testng:1.0.0'
}
```

#### **Maven**
```xml
<dependency>
  <groupId>com.zebrunner</groupId>
  <artifactId>agent-testng</artifactId>
  <version>1.0.0</version>
  <scope>test</scope>
</dependency>
```

<!-- tabs:end -->

## Agent configuration
Once the agent is available on the classpath of your test project, it is **not** automatically enabled. The valid configuration must be provided.

It is currently possible to provide the configuration via:
1. Environment variables 
2. Program arguments 
3. YAML file
4. Properties file

The configuration lookup will be performed in the order listed above, meaning the environment configuration will always take precedence over YAML and so on.
It is also possible to override configuration parameters by passing them through a configuration provider with higher precedence.

Once the configuration is set up, the agent is ready to track your test run events, with no additional configuration required.

### Environment configuration
The following configuration parameters are recognized by the agent:

- `REPORTING_ENABLED` - enables or disables reporting. The default value is `false`. If disabled, the agent will use no op component implementations that will simply log output for tracing purposes with the `trace` level;
- `REPORTING_SERVER_HOSTNAME` - mandatory if reporting is enabled. Zebrunner server hostname. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Service URL' section;
- `REPORTING_SERVER_ACCESS_TOKEN` - mandatory if reporting is enabled. Access token must be used to perform API calls. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Token' section;
- `REPORTING_PROJECT_KEY` - optional value. The project that the test run belongs to. The default value is `UNKNOWN`. You can manage projects in Zebrunner in the appropriate section;
- `REPORTING_RUN_DISPLAY_NAME` - optional value. The display name of the test run. The default value is `Default Suite`;
- `REPORTING_RUN_BUILD` - optional value. The build number that is associated with the test run. It can depict either the test build number, or the application build number;
- `REPORTING_RUN_ENVIRONMENT` - optional value. The environment in which the tests will run.

### Program arguments configuration
The following configuration parameters are recognized by the agent:

- `reporting.enabled` - enables or disables reporting. The default value is `false`. If disabled, the agent will use no op component implementations that will simply log output for tracing purposes with the `trace` level;
- `reporting.server.hostname` - mandatory if reporting is enabled. Zebrunner server hostname. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Service URL' section;
- `reporting.server.accessToken` - mandatory if reporting is enabled. Access token must be used to perform API calls. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Token' section;
- `reporting.projectKey` - optional value. The project that the test run belongs to. The default value is `UNKNOWN`. You can manage projects in Zebrunner in the appropriate section;
- `reporting.run.displayName` - optional value. The display name of the test run. The default value is `Default Suite`;
- `reporting.run.build` - optional value. The build number that is associated with the test run. It can depict either the test build number, or the application build number;
- `reporting.run.environment` - optional value. The environment in which the tests will run.

### YAML configuration
Agent recognizes `agent.yaml` or `agent.yml` file in the resources root folder. It is currently not possible to configure an alternative file location.

Below is a sample configuration file:

```yaml
reporting:
  enabled: true
  project-key: UNKNOWN
  server:
    hostname: localhost:8080
    access-token: <token>
  run:
    display-name: Nightly Regression Suite
    build: 1.12.1.96-SNAPSHOT
    environment: TEST-1
```

- `reporting.enabled` - enables or disables reporting. The default value is `false`. If disabled, the agent will use no op component implementations that will simply log output for tracing purposes with the `trace` level;
- `reporting.server.hostname` - mandatory if reporting is enabled. Zebrunner server hostname. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Service URL' section;
- `reporting.server.access-token` - mandatory if reporting is enabled. Access token must be used to perform API calls. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Token' section;
- `reporting.project-key` - optional value. The project that the test run belongs to. The default value is `UNKNOWN`. You can manage projects in Zebrunner in the appropriate section;
- `reporting.run.display-name` - optional value. The display name of the test run. The default value is `Default Suite`;
- `reporting.run.build` - optional value. The build number that is associated with the test run. It can depict either the test build number, or the application build number;
- `reporting.run.environment` - optional value. The environment in which the tests will run.

### Properties configuration
The agent recognizes only `agent.properties` file in the resources root folder. It is currently not possible to configure an alternative file location.

Below is a sample configuration file:

```properties
reporting.enabled=true
reporting.project-key=UNKNOWN
reporting.server.hostname=localhost:8080
reporting.server.access-token=<token>
reporting.run.display-name=Nightly Regression Suite
reporting.run.build=1.12.1.96-SNAPSHOT
reporting.run.environment=TEST-1
```

- `reporting.enabled` - enables or disables reporting. The default value is `false`. If disabled, the agent will use no op component implementations that will simply log output for tracing purposes with the `trace` level;
- `reporting.server.hostname` - mandatory if reporting is enabled. Zebrunner server hostname. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Service URL' section;
- `reporting.server.access-token` - mandatory if reporting is enabled. Access token must be used to perform API calls. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Token' section;
- `reporting.project-key` - optional value. The project that the test run belongs to. The default value is `UNKNOWN`. You can manage projects in Zebrunner in the appropriate section;
- `reporting.run.display-name` - optional value. The display name of the test run. The default value is `Default Suite`;
- `reporting.run.build` - optional value. The build number that is associated with the test run. It can depict either the test build number, or the application build number;
- `reporting.run.environment` - optional value. The environment in which the tests will run.

## Advanced reporting
It is possible to configure additional reporting capabilities by improving your tracking experience.

### Collecting test logs
It is also possible to enable the log collection for your tests. Currently, three logging frameworks are supported out of the box: logback, log4j, log4j2. We recommend using slf4j (Simple Logging Facade for Java) which provides abstraction over logging libraries.
All you have to do to enable logging is to register the reporting appender in your test framework configuration file.

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
    <appender name="ZebrunnerAppender" class="com.zebrunner.agent.core.appender.logback.ReportingAppender">
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
log4j.appender.zebrunner=com.zebrunner.agent.core.appender.log4j.ReportingAppender
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
  implementation 'org.apache.logging.log4j:log4j-api:2.13.3'
  implementation 'org.apache.logging.log4j:log4j-core:2.13.3'
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
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-api</artifactId>
        <version>2.13.3</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.13.3</version>
    </dependency>
</dependencies>
```

<!-- tabs:end -->

Add logging appender to `log4j2.xml` file. Feel free to customize the logging pattern according to your needs:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration packages="com.zebrunner.agent.core.appender.log4j2">
   <properties>
      <property name="pattern">[%d{HH:mm:ss}] %-5p (%F:%L) - %m%n</property>
   </properties>
   <appenders>
      <ZebrunnerAppender name="ZebrunnerAppender">
         <PatternLayout pattern="${pattern}" />
      </ZebrunnerAppender>
   </appenders>
   <loggers>
      <root level="info">
         <appender-ref ref="ZebrunnerAppender"/>
      </root>
   </loggers>
</configuration>
```

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

### Capturing screenshots
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

### Saving artifacts
In case your tests produce some artifacts, it may be useful to track them in Zebrunner. The agent comes with a few convenient methods for uploading artifacts in Zebrunner and linking them to the currently running test.

Artifacts can be uploaded using the `Artifact` class. This class has 4 static methods to upload artifacts represented by any Java type associated with the files. Together with an artifact, you must provide the artifact name. This name must contain the file extension that reflects the actual content of the file. If the file extension is incorrect, this file will not be saved in Zebrunner.

Here is a sample test:
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
        Artifact.upload(inputStream, "file.docx");
        Artifact.upload(byteArray, "image.png");
        Artifact.upload(file, "application.apk");
        Artifact.upload(path, "test-log.txt");
        // meaningful assertions
    }

}
```
Artifact upload process is performed in the background, so it will not affect test execution.
The uploaded artifacts will appear under the test name in the run results in Zebrunner.

It is also allowed to attach links to external artifacts. Any kind of external resources can be used. In order to attach an external artifact, you should use a static method of the `ArtifactReference` class.

Here is an example:
```java
import com.zebrunner.agent.core.registrar.ArtifactReference;
import org.testng.annotations.Test;

public class AwesomeTests {

    @Test
    public void awesomeTest() {
        // some code here
        ArtifactReference.attach("Zebrunner", "https://zebrunner.com/");
        // meaningful assertions
    }

}
```
The example above adds a link to zebrunner.com to the list of test artifacts.

### Attaching labels
In some cases, it may be useful to attach some meta information related to a test - its Jira id, its priority, or any other useful data.

The agent comes with a concept of a label. Label is a key-value pair associated with a test. The key is represented by a `String`, the label value accepts a vararg of `Strings`. 

There is a bunch of annotations that can be used to attach a label to a test. All the annotations can be used on both class and method levels. It is also possible to override a class-level label on a method-level. There is one generic annotation and a few bespoke ones that don't require a label name:
- `@Priority`
- `@JiraReference`
- `@TestLabel` - the generic one.

There is also a Java API to attach labels during test execution. The `Label` class has a static method that can be used to attach a label. 

Here is a sample:
```java
import com.zebrunner.agent.core.annotation.JiraReference;
import com.zebrunner.agent.core.annotation.Priority;
import com.zebrunner.agent.core.annotation.TestLabel;
import com.zebrunner.agent.core.registrar.Label;
import org.testng.annotations.Test;

public class AwesomeTests {

    @Test
    @Priority(Priority.P1)
    @JiraReference("ZBR-1231")
    @TestLabel(name = "app", value = {"reporting-service:v1.0", "reporting-service:v1.1"})
    public void awesomeTest() {
        // some code here  
        Label.attach("Chrome", "85.0");
        // meaningful assertions
    }

}
```
The test from the sample above attaches 5 labels: 1 priority, 1 jira-reference, 2 app, 1 Chrome label.

The values of attached labels will be displayed in Zebrunner under the name of a corresponding test. The values of the `@JiraReference` annotation will be displayed in blue pills to the right of the test name.

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

## Contribution
To check out the project and build from the source, do the following:
```
git clone git://github.com/zebrunner/java-agent-testng.git
cd java-agent-testng
./gradlew build
```

## License
Zebrunner Reporting service is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
