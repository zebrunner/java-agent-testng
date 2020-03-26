# Zebrunner TestNG agent

Official Zebrunner TestNG agent providing reporting and smart reruns functionality. In order to enable Zebrunner Listener for TestNG no special configuration is required - service discovery mechanism will automatically register listener once it will be availlable on your test application classpath.

# Checking out and building

To check out the project and build from source, do the following:

    git clone git://github.com/zebrunner/java-agent-testng.git
    cd java-agent-testng
    ./gradlew build

# Including into your project

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

# Agent configuration - reporting

In order to start using agent for reporting you'll nead to provide minimal configuration such as Zebrunner API host and API access token. Configuration lookup is implenented in the following mannger: first agent seeks for environment variables required, then it looks for system arguments (arguments passed to java programm) and finally looks for property file residing in root of resources folder called `agent.properties` and uses whatever found first.
1. Environment configuration - `ZBR_HOSTNAME` and `ZBR_ACCESS_TOKEN` env variables should be initialized
2. System configuration - `zbr.hostname` and `zbr.accessToken` params should be passed
3. Property file - file called `agent.properties` should reside in resources root folder and contain `zbr.hostname` and `zbr.access-token` properties with corresponding values.

# Agent configuration - smart reruns

In order to use smart reruns for your test runs additional property should be specified in `agent.properties` called `rerun_id`. This property basically encodes a rerun conditions - test run id and selectors describing what tests to rerun (e.g. by specifying test ids or statuses). Below are examples of such conditions:
```
# general condition structure
<test-run-id>:<optional:test-selectors>

# condition that will tell agent to rerun all tests
677d3bb5-52b5-414b-8790-b538815572f0

# condition that will tell agent to rerun tests with specified ids only
677d3bb5-52b5-414b-8790-b538815572f0:[1, 2, 3, 4]

# condition that will tell agent to rerun tests having either passed or failed status
677d3bb5-52b5-414b-8790-b538815572f0:[passed, failed]
```

# License

Zebrunner Reporting service is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
