Carbon-Logging
--------------

Carbon Logging provides a framework for performing declarative logging in Java. Its design provides for a variety of mechanisms for integrating standardised logging. As well
as supporting standardised debug style logging, Carbon Logging provides distinctive capabilities for performing performance logging and exception logging. Past experience 
has convinced us that not only are these the most critical kinds of logging to capture for performance tuning and production support but also search capabilities, implemented
adhocly often have significant impact on codebases.

Carbon Logging addresses a key concern that always plagues production support situations. It consistently logs method parameters, return values and stack traces. Parameters are
effectively serialized as part of the logging output even when objects to be logged do not implement toString().

Carbon Logging provides a variety of options for how you can serialize log output. As well as readable text output, JSON strategies are provided for integration with event-sinks,
and operational monitoring tools.

Carbon Logging provides a variety of mechanisms for integration of logging into your application. Aspects can be applied to introduce logging toyour own code via compile time 
or runtime weaving. For instrumenting code owned by others you can use dynamic proxies, either class or interface based using CGLib and JDK Dynamic proxies respectively. These can
configured programmatically or by discovery. The discovery approach is particularly well suited to use of libraries such as Retrofit where you can apply logging annotation to 
the client interfaces you define and wrap a dynamic proxy around your call to Retrofit.

To give an idea of the style of usage of Carbon-Logging, here's some example code:

```java
    @Warn(toStringStrategy=JacksonToStringStrategy.class, includeStartAndEndMarkers=true)
    @LogExceptions
    public String serviceMethod(int a, String b, Object c, Object d) {
```

Logging can be applied via AspectJ in which case no further configuration is required. For configuration via Proxies, factories are provided:

```java
    ExampleServiceWithLogging proxy = LoggingCglibFactory.getProxy(Level.ERROR, service);
```

Carbon Logging is licensed under the business-friendly [Apache 2.0 licence](https://raw.githubusercontent.com/GroupCDG/carbon-logging/develop/LICENSE).


### Documentation
Documentation is available at:

* The [home page](https://github.com/GroupCDG/carbon-logging/)

### Releases
[Release 0.9.0](https://github.com/GroupCDG/carbon-logging/releases) is the current latest release.
This release is considered stable and is a candidate for promotion to 1.0.0
It depends on JSE 8.0 or later.

Available in the [Maven Central repository](http://search.maven.org/#artifactdetails|com.groupcdg.carbon|carbon-logging|0.9.0|jar)


### Related projects
Related projects at GitHub:
- https://github.com/GroupCDG/tidesdk-maven-plugin
- https://github.com/GroupCDG/cordova-maven-plugin

### Support
Please use GitHub issues and Pull Requests for any support requirements.

### History
Issue tracking and active development is via GitHub.