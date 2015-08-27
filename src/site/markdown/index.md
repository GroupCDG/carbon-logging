## <i></i> About

**Carbon Logging** provides a framework for performing declarative logging in Java. Its design provides for a variety of mechanisms for integrating standardised logging. As well
as supporting standardised debug style logging, Carbon Logging provides distinctive capabilities for performing performance logging and exception logging. 

Past experience has convinced us that not only are these the most critical kinds of logging to capture for performance tuning and production support but also such capabilities, 
implemented adhocly often have significant impact on codebases.

Carbon Logging is licensed under the business-friendly [Apache 2.0 licence](https://raw.githubusercontent.com/GroupCDG/carbon-logging/develop/LICENSE).

Carbon Logging is an Open Source project of CDG. CDG have released Carbon Logging as part of Carbon Digital Platforms (CDP). CDP provides a toolkit for Java developers and a framework 
for integrating services seamlessly, from composite services that embody an enterprises' capabilities through to lightweight microservices. CDP is being delivered by CDG as an open 
source model, with service, support, infrastructure provisioning, and customisation available to CDG's customers.


## <i></i> Features

Carbon Logging addresses a key concern that always plagues production support situations. It consistently logs method parameters, return values and stack traces. Parameters are
effectively serialized as part of the logging output even when objects to be logged do not implement toString().

Carbon Logging provides a variety of options for how you can serialize log output. As well as readable text output, JSON strategies are provided for integration with event-sinks,
and operational monitoring tools.

Carbon Logging provides a variety of mechanisms for integration of logging into your application:

* Aspects can be applied to introduce logging to your own code via compile time 
or runtime weaving. 
* For instrumenting code owned by others you can use dynamic proxies, either a) class, or b) interface based using CGLib and JDK Dynamic proxies respectively. 

Proxies can configured programmatically or by discovery. The discovery approach is particularly well suited to use of libraries such as Retrofit where you can apply logging annotation to 
the client interfaces you define and wrap a dynamic proxy around your call to Retrofit.


## <i></i> Documentation

Documentation is available:

* The [Javadoc](apidocs/index.html)
* The [Source XRef](xref/index.html)
* The [Changes Report](changes-report.html) for each release
* The [GitHub](https://github.com/GroupCDG/carbon-logging) source repository

A variety of reports are available based on the most recent release:

* [Dependencies Report](dependencies.html)
* [JDepend Analysis](jdepend-report.html)
* [Tag List Report](taglist.html)
---

## <i></i> Example Usage

To give an idea of the style of usage of Carbon-Logging, here's some example code:

<div class="source">
<pre>
 @Warn(toStringStrategy=JacksonToStringStrategy.class, includeStartAndEndMarkers=true)
 @LogExceptions
 public String serviceMethod(int a, String b, Object c, Object d) {</pre>
</div>

Logging can be applied via AspectJ in which case no further configuration is required. For configuration via Proxies, factories are provided:

<div class="source">
<pre>
 ExampleServiceWithLogging proxy = LoggingCglibFactory.getProxy(Level.ERROR, service);</pre>
</div>)

## <i></i> Related Projects

Related projects at GitHub:

* https://github.com/GroupCDG/tidesdk-maven-plugin
* https://github.com/GroupCDG/cordova-maven-plugin


---

## <i></i> Releases

[Release 0.9.0](https://github.com/GroupCDG/carbon-logging/releases) is the current latest release.
This release is considered stable and is a candidate for promotion to 1.0.0
It depends on JSE 8.0 or later.

Available in the [Maven Central repository](http://search.maven.org/#artifactdetails|com.groupcdg.carbon|carbon-logging|0.9.0|jar)

```xml
<dependency>
  <groupId>com.groupcdg.carbon</groupId>
  <artifactId>carbon-logging</artifactId>
  <version>0.9.0</version>
</dependency>
```

---

### Support

Issue tracking and active development is via GitHub.

Please use GitHub [Issues](https://github.com/GroupCDG/carbon-logging/issues) and [Pull Requests](https://github.com/GroupCDG/carbon-logging/pulls) for any support requirements.