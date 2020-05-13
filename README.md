# Zebrunner TestNG agent

Official Zebrunner TestNG agent providing reporting and smart reruns functionality. In order to enable Zebrunner Listener for TestNG no special configuration is required - service discovery mechanism will automatically register listener once it will be availlable on your test application classpath.

# Checking out and building

To check out the project and build from source, do the following:

    git clone git://github.com/zebrunner/java-agent-testng.git
    cd java-agent-testng
    ./gradlew build

# Including into your project

Agent comes bundled with TestNG 7.1.0, so you may want to comment our your dependency or exclude it from agent.

Gradle:
```gradle
dependencies {
  testImplementation 'com.zebrunner:agent-testng:1.0.0'
}
```

Maven:
```xml
<dependency>
  <groupId>com.zebrunner</groupId>
  <artifactId>agent-testng</artifactId>
  <version>1.0.0</version>
</dependency>
```

# Agent configuration

Once agent is available on classpath of your test project it is automatically enabled and expects valid configuration to be available.
It is currently possible to provide configuration via: 

1. Environment variables 
2. Program arguments 
3. YAML file
4. Properties file

Configuration lookup will be performed in order listed above, meaning that environment configuration will always take precedence over YAML and so on.
It is also possible to override configuration parameters by supplying them via configuration provider having higher precedence.

Once configuration is in place agent is ready to track you test run events, no additional configuration required.

## Environment configuration

The following configuration parameters are recognized by agent:

- `REPORTING_ENABLED` - optional, default value: `true`. Enables or disables reporting. Once disabled - agent will use no op component implementations that will simply log output for tracing purpose
- `REPORTING_SERVER_HOSTNAME` - mandatory. Zebrunner server hostname
- `REPORTING_SERVER_ACCESS_TOKEN` - mandatory. Access token to be used to perform API calls. Can be obtained by visiting Zebrunner user profile page

## Program arguments configuration

The following configuration parameters are recognized by agent:

- `reporting.enabled` - optional, default value: `true`. Enables or disables reporting. Once disabled - agent will use no op component implementations that will simply log output for tracing purpose
- `reporting.server.hostname` - mandatory. Zebrunner server hostname
- `reporting.server.accessToken` - mandatory. Access token to be used to perform API calls. Can be obtained by visiting Zebrunner user profile page

## YAML configuration

Agent will recognize `agent.yaml` or `agent.yml` file residing in resources root folder. It is currently not possible to configure alternative file location.
Below is sample configuration file:

```yaml
reporting:
  enabled: true
  server:
    hostname: localhost:8080/api
    access-token: <token>

```

- `reporting.enabled` - optional, default value: `true`. Enables or disables reporting. Once disabled - agent will use no op component implementations that will simply log output for tracing purpose
- `reporting.server.hostname` - mandatory. Zebrunner server hostname
- `reporting.server.access-token` - mandatory. Access token to be used to perform API calls. Can be obtained by visiting Zebrunner user profile page

## Properties configuration

Agent will recognize `agent.properties` file residing in resources root folder. It is currently not possible to configure alternative file location.
Below is sample configuration file:

```properties
zafira_enabled=true
zbr.hostname=localhost:8080/api
zbr.access-token=<token>
```

- `reporting.enabled` - optional, default value: `true`. Enables or disables reporting. Once disabled - agent will use no op component implementations that will simply log output for tracing purpose
- `reporting.server.hostname` - mandatory. Zebrunner server hostname
- `reporting.server.access-token` - mandatory. Access token to be used to perform API calls. Can be obtained by visiting Zebrunner user profile page

# Advanced reporting

It is possible to configure additional reporting capabilities improving your tracking experience. 

## Collecting test logs

It is also possible to enable log collection for your tests. Currently three logging frameworks are supported out of the box: logback, log4j, log4j2.
In order to enable logging all you have to do is register reporting appender in your test framework configuration file.
Below is the list of appenders supplied out of the box:

1. logback: `com.zebrunner.agent.core.appender.logback.ReportingAppender`
2. log4j: `com.zebrunner.agent.core.appender.log4j.ReportingAppender`
3. log4j2: `com.zebrunner.agent.core.appender.log4j2.ReportingAppender`

## Capturing screenshots

In case you are using TestNG as a UI testing framework it might come handy to have an ability to track captured screenshots in scope of Zebrunner reporting.
Agent comes with a Java API allowing you to send your screenshots to Zebrunner so they will be attached to test run. 
Below is a sample code of test sending screenshot to Zebrunner:

```java
@Test
public void myAwesomeTest() {
    // capture screenshot 
    Screenshot.upload(screenshotBytes, capturedAtMillis);
    // meaningful assertions
}
```

Screenshot should be passed as byte array along with unix timestamp in milliseconds corresponding to the moment when screenshot was captured. 
If `null` is supplied instead of timestamp - it will be generated automatically, however it is strongly recommended to include accurate timestamp in order to get accurate tracking. 

# License

Zebrunner Reporting service is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
