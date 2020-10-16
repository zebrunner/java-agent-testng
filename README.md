# Zebrunner TestNG (7.3.0+) agent
Official Zebrunner TestNG agent providing reporting and smart reruns functionality. In order to enable Zebrunner Listener for TestNG no special configuration is required - service discovery mechanism will automatically register listener once it will be available on your test application classpath.

## Including into your project
Agent comes bundled with TestNG 7.3.0, so you may want to comment our your dependency or exclude it from the agent.

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
Once agent is available on classpath of your test project it is **not** automatically enabled and expects a valid configuration to be available.
It is currently possible to provide the configuration via:

1. Environment variables 
2. Program arguments 
3. YAML file
4. Properties file

Configuration lookup will be performed in order listed above, meaning environment configuration will always take precedence over YAML and so on.
It is also possible to override configuration parameters by supplying them via configuration provider having higher precedence.

Once configuration is in place agent is ready to track you test run events, no additional configuration required.

### Environment configuration
The following configuration parameters are recognized by the agent:

- `REPORTING_ENABLED` - default value: `false`. Enables or disables reporting. Once disabled - agent will use no op component implementations that will simply log output for tracing purpose;
- `REPORTING_SERVER_HOSTNAME` - mandatory if reporting is enabled. Zebrunner server hostname. Can be obtained by visiting Zebrunner user profile page;
- `REPORTING_SERVER_ACCESS_TOKEN` - mandatory if reporting is enabled. Access token to be used to perform API calls. Can be obtained by visiting Zebrunner user profile page;

### Program arguments configuration
The following configuration parameters are recognized by the agent:

- `reporting.enabled` - default value: `false`. Enables or disables reporting. Once disabled - agent will use no op component implementations that will simply log output for tracing purpose;
- `reporting.server.hostname` - mandatory if reporting is enabled. Zebrunner server hostname. Can be obtained by visiting Zebrunner user profile page;
- `reporting.server.accessToken` - mandatory if reporting is enabled. Access token to be used to perform API calls. Can be obtained by visiting Zebrunner user profile page;

### YAML configuration
Agent recognizes `agent.yaml` or `agent.yml` file residing in resources root folder. It is currently not possible to configure alternative file location.
Below is sample configuration file:

```yaml
reporting:
  enabled: true
  server:
    hostname: localhost:8080
    access-token: <token>
```

- `reporting.enabled` - default value: `false`. Enables or disables reporting. Once disabled - agent will use no op component implementations that will simply log output for tracing purpose;
- `reporting.server.hostname` - mandatory if reporting is enabled. Zebrunner server hostname;
- `reporting.server.access-token` - mandatory if reporting is enabled. Access token to be used to perform API calls. Can be obtained by visiting Zebrunner user profile page;


### Properties configuration
Agent recognizes only `agent.properties` file residing in resources root folder. It is currently not possible to configure alternative file location.
Below is sample configuration file:

```properties
reporting.enabled=true
reporting.server.hostname=localhost:8080
reporting.server.access-token=<token>
```

- `reporting.enabled` - default value: `false`. Enables or disables reporting. Once disabled - agent will use no op component implementations that will simply log output for tracing purpose;
- `reporting.server.hostname` - mandatory if reporting is enabled. Zebrunner server hostname;
- `reporting.server.access-token` - mandatory if reporting is enabled. Access token to be used to perform API calls. Can be obtained by visiting Zebrunner user profile page;

## Advanced reporting
It is possible to configure additional reporting capabilities improving your tracking experience. 

### Collecting test logs
It is also possible to enable log collection for your tests. Currently, three logging frameworks are supported out of the box: logback, log4j, log4j2. We recommend to use slf4j (Simple Logging Facade for Java) which provides abstraction over logging libraries.
In order to enable logging all you have to do is register reporting appender in your test framework configuration file.


#### Logback
Add logback (and, optionally, slf4j) dependencies in your build descriptor.

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
Add logging appender in **logback.xml** file. Feel free to adjust the logging pattern according to your needs:
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
Add log4j (and, optionally, slf4j) dependency in your build descriptor.

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

Add logging appender in **log4j.properties** file. Feel free to adjust the logging pattern according to your needs:
```properties
log4j.rootLogger = INFO, zebrunner
log4j.appender.zebrunner=com.zebrunner.agent.core.appender.log4j.ReportingAppender
log4j.appender.zebrunner.layout=org.apache.log4j.PatternLayout
log4j.appender.zebrunner.layout.conversionPattern=pattern">[%d{HH:mm:ss}] %-5p (%F:%L) - %m%n
```

#### Log4j2
Add log4j2 (and, optionally, slf4j) dependency in your build descriptor:

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

Add logging appender in **log4j2.xml** file. Feel free to adjust the logging pattern according to your needs:
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
In case you are using TestNG as a UI testing framework it might come handy to have an ability to track captured screenshots in scope of Zebrunner reporting.
Agent comes with a Java API allowing you to send your screenshots to Zebrunner, so they will be attached to test run. 
Below is a sample code of test sending screenshot to Zebrunner:
```java
import com.zebrunner.agent.core.registrar.Screenshot;
import org.testng.annotations.Test;

public class AwesomeTests {

    @Test
    public void myAwesomeTest() {
        // capture screenshot 
        Screenshot.upload(screenshotBytes, capturedAtMillis);
        // meaningful assertions
    }

}
```

Screenshot should be passed as byte array along with unix timestamp in milliseconds corresponding to the moment when screenshot was captured. 
If `null` is supplied instead of timestamp - it will be generated automatically, however it is strongly recommended to include an accurate timestamp in order to get accurate tracking. 

### Tracking test maintainer
You might want to add transparency to the process of automation maintenance by having an engineer responsible for evolution of specific tests or test classes.
Zebrunner comes with a concept of maintainer - a person that can be assigned to maintain tests. In order to keep track of those, agent comes with `@Maintainer` annotation.

This annotation can be placed on both test class and method. It is also possible to override class-level maintainer on a method-level.
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

In the example above `kenobi` will be reported as a maintainer of `anotherAwesomeTest` (class-level value taken into account), while `skywalker` will be reported as a maintainer of test `awesomeTest`.
Maintainer username should be valid Zebrunner username, otherwise it will be set to `anonymous`.

## Contribution
To check out the project and build from the source, do the following:
```
git clone git://github.com/zebrunner/java-agent-testng.git
cd java-agent-testng
./gradlew build
```

## License
Zebrunner Reporting service is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
