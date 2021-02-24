# Zebrunner TestNG agent
The official Zebrunner TestNG agent provides reporting and smart reruns functionality. No special configuration is required to enable the Zebrunner Listener for TestNG - service discovery mechanism will automatically register the listener as soon as it becomes available on the classpath of your test project.

## Inclusion into your project 
The agent comes bundled with TestNG 7.3.0, so you should comment out or exclude your TestNG dependency from the project. If you are using a version of TestNG below 7.3.0, we cannot guarantee the correct functionality of the agent.

Including the agent into your project is easy - just add the dependency to the build descriptor.

<!-- tabs:start -->

#### **Gradle**
```groovy
dependencies {
  testImplementation 'com.zebrunner:agent-testng:1.2.1'
}
```

#### **Maven**
```xml
<dependency>
  <groupId>com.zebrunner</groupId>
  <artifactId>agent-testng</artifactId>
  <version>1.2.1</version>
  <scope>test</scope>
</dependency>
```

<!-- tabs:end -->

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
- `REPORTING_PROJECT_KEY` - optional value. It is the project that the test run belongs to. The default value is `UNKNOWN`. You can manage projects in Zebrunner in the appropriate section;
- `REPORTING_RUN_DISPLAY_NAME` - optional value. It is the display name of the test run. The default value is `Default Suite`;
- `REPORTING_RUN_BUILD` - optional value. It is the build number that is associated with the test run. It can depict either the test build number or the application build number;
- `REPORTING_RUN_ENVIRONMENT` - optional value. It is the environment where the tests will run.

### Program arguments
The following configuration parameters are recognized by the agent:
- `reporting.enabled` - enables or disables reporting. The default value is `false`. If disabled, the agent will use no op component implementations that will simply log output for tracing purposes with the `trace` level;
- `reporting.server.hostname` - mandatory if reporting is enabled. It is Zebrunner server hostname. It can be obtained in Zebrunner on the 'Account & profile' page under the 'Service URL' section;
- `reporting.server.accessToken` - mandatory if reporting is enabled. Access token must be used to perform API calls. Can be obtained in Zebrunner on the 'Account & profile' page under the 'Token' section;
- `reporting.projectKey` - optional value. The project that the test run belongs to. The default value is `UNKNOWN`. You can manage projects in Zebrunner in the appropriate section;
- `reporting.run.displayName` - optional value. The display name of the test run. The default value is `Default Suite`;
- `reporting.run.build` - optional value. The build number that is associated with the test run. It can depict either the test build number or the application build number;
- `reporting.run.environment` - optional value. The environment in which the tests will run.

### YAML file
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
- `reporting.run.build` - optional value. The build number that is associated with the test run. It can depict either the test build number or the application build number;
- `reporting.run.environment` - optional value. The environment in which the tests will run.

### Properties file
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
- `reporting.run.build` - optional value. The build number that is associated with the test run. It can depict either the test build number or the application build number;
- `reporting.run.environment` - optional value. The environment in which the tests will run.

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
<configuration packages="com.zebrunner.agent.core.logging.log4j2">
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

### Attaching test labels
In some cases, it may be useful to attach meta information related to a test - its Jira id, its priority, or any other useful data.

The agent comes with a concept of a label. Label is a key-value pair associated with a test. The key is represented by a `String`, the label value accepts a vararg of `Strings`. 

There is a bunch of annotations that can be used to attach a label to a test. All the annotations can be used on both class and method levels. It is also possible to override a class-level label on a method-level. There is one generic annotation, and a few bespoke ones that don't require a label name:
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
The test from the sample above attaches 5 labels: 1 priority, 1 jira-reference, 2 'app' labels, 1 'Chrome' label.

The values of attached labels will be displayed in Zebrunner under the name of a corresponding test. The values of the `@JiraReference` annotation will be displayed in blue pills to the right of the test name.

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
```

The `${com.zebrunner:agent-core:jar}` property is generated by the `maven-dependency-plugin` during the initialization phase. Maven automatically sets the generated value when `maven-surefire-plugin` launches tests.

#### Maven antrun plugin
The `maven-surefire-plugin` provides the ability to add VM arguments in a convenient way. You only need to provide the absolute path to the jar file with the Zebrunner agent.

The `maven-antrun-plugin` can be used to obtain the absolute path to a project dependency. If your project is already using the `maven-antrun-plugin`, this is the best way to go.

```xml
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
                    <basename file="${maven.dependency.com.zebrunner.agent-core.jar.path}" property="com.zebrunner:agent-core:jar"/>
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

Test agent itself does not capture those artifacts since it has no control over underlying Selenium Hub or MCloud implementation, however, it is possible to attach appropriate artifact references by providing specially designed set of driver session capabilities (**enabling capabilities**) - see the table below for more details. Only the `true` value for those is considered as trigger to save the link.

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
