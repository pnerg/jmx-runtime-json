![Build & Test](https://github.com/pnerg/jmx-runtime-json/workflows/Build%20&%20Test/badge.svg)
[![codecov](https://codecov.io/gh/pnerg/jmx-runtime-json/branch/master/graph/badge.svg?token=O8I3FS7RSI)](https://codecov.io/gh/pnerg/jmx-runtime-json)
[![Javadoc](http://javadoc-badge.appspot.com/org.dmonix/jmx-runtime-json.svg?label=javadoc)](http://javadoc-badge.appspot.com/org.dmonix/jmx-runtime-json)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dmonix/jmx-runtime-json/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.dmonix/jmx-runtime-json)

JMX Runtime Information As Json
-------

This little library helps to generate a json out of a selected set of the runtime information available in the JVM.   
All of the data is accessible over various managed beans (mbeans) available in the JVM.

But accessing this data over JMX when running the application in a Docker container is difficult the least to say.  
Most applications however expose a web interface and possibly even health and or meta-information over HTTP.

The use case for this library is to convert JMX internal information into Json format that can be transported as e.g. a
HTTP response for debugging purposes or just to be logged.

# Usage

The Json is modularised into allowing for a builder pattern approach where one can choose to use one or more of the '
modules'.

* class-loading - `withClassLoadingInfo`
* memory - `withMemoryInfo`
* operating-system - `withOperatingSystemInfo`
* runtime - `withRuntimeInfo`
* thread - `withThreadInfo`

Builder pattern example:

```
JMXJsonBuilder.apply().withMemoryInfo().withRuntimeInfo();
```

The easiest way to produce a Json with all the data supported by the library is to use one of the `allInfo()` methods.  
One can choose to get an optional stack trace for every thread by supplying the depth desired to print (three in this
example).  
Not providing a depth defaults to zero, i.e. no stack trace at all.

```
String json = JMXJsonBuilder.allInfo(3).prettyPrint();
```

This will create a Json like the one below (threads cut for brevity).

```
{
  "operating-system": {
    "name": "Mac OS X",
    "architecture": "x86_64",
    "version": "10.16",
    "available-processors": 16,
    "system-load-average": 2.81396484375
  },
  "memory": {
    "heap": {
      "init": 1073741824,
      "committed": 1073741824,
      "max": 1073741824,
      "used": 286342112,
      "pools": [
         {
          "name": "G1 Eden Space",
          "usage": {
            "init": 54525952,
            "committed": 46137344,
            "max": -1,
            "used": 4194304
          },
          "peak-usage": {
            "init": 54525952,
            "committed": 671088640,
            "max": -1,
            "used": 50331648
          }
        },
        ...
      ]   
    },
    "non-heap": {
      "init": 2555904,
      "committed": 178573312,
      "max": -1,
      "used": 164771184,
      "pools": [
       {
          "name": "CodeHeap 'non-nmethods'",
          "usage": {
            "init": 2555904,
            "committed": 2949120,
            "max": 7598080,
            "used": 2866048
          },
          "peak-usage": {
            "init": 2555904,
            "committed": 2949120,
            "max": 7598080,
            "used": 2885248
          },
         ...
      ]          
    }
  },
  "runtime": {
    "vm-name": "OpenJDK 64-Bit Server VM",
    "vm-vendor": "AdoptOpenJDK",
    "vm-version": "11.0.9+11",
    "uptime": 7448,
    "start-time": 1611145735991,
    "input-arguments": [
      "-Dfile.encoding=UTF-8",
      "-Xms1024m",
      "-Xmx1024m",
      "-Xss256k",
      "-XX:ReservedCodeCacheSize=128m"
    ],
    "classpath": [
      "/opt/sbt/bin/sbt-launch.jar"
    ]
  },
  "thread": {
    "current-thread-count": 46,
    "daemon-thread-count": 24,
    "peak-thread-count": 46,
    "thread-cpu-time-enabled": true,
    "thread-contention-monitoring-enabled": true,
    "threads": [
      {
        "name": "main",
        "id": 1,
        "blocked-count": 222,
        "blocked-time": 0,
        "waited-count": 564,
        "waited-time": 0,
        "state": "WAITING",
        "stack-trace": [
          "jdk.internal.misc.Unsafe.park(Native method)",
          "java.util.concurrent.locks.LockSupport.park(LockSupport.java:194)",
          "java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2081)"
        ]
      },
      {
        "name": "Reference Handler",
        "id": 2,
        "blocked-count": 8,
        "blocked-time": 0,
        "waited-count": 0,
        "waited-time": 0,
        "state": "RUNNABLE",
        "stack-trace": [
          "java.lang.ref.Reference.waitForReferencePendingList(Native method)",
          "java.lang.ref.Reference.processPendingReferences(Reference.java:241)",
          "java.lang.ref.Reference$ReferenceHandler.run(Reference.java:213)"
        ]
      },
     ...
    ]
  },
  "class-loading": {
    "loaded-classes": 16123,
    "total-loaded-classes": 16128,
    "unloaded-classes": 5
  }
}
```

## withClassLoadingInfo

Provides information on the total of all class loaders in the JVM.

```
JMXJsonBuilder.apply().withClassLoadingInfo();
```

## withMemoryInfo

Provides insights into the allocated memory of the JVM, heap as well as off/non-heap (e.g thread stacks).     
Optionally one can get statistics on all memory pools.

```
JMXJsonBuilder.apply().withMemoryInfo(); //defaults to withMemoryInfo(false)
JMXJsonBuilder.apply().withMemoryInfo(true);
```

## withOperatingSystemInfo

Adds information on the operating system the JVM runs on.

```
JMXJsonBuilder.apply().withOperatingSystemInfo();
```

## withRuntimeInfo

Provides runtime information such as:

* when the JVM was started
* total uptime
* JVM vendor/version
* all input arguments
* classpath

```
JMXJsonBuilder.apply().withRuntimeInfo();
```

## withThreadInfo

As threads may be in abundance this is the most verbose part.   
Apart from providing general counters for threads it also provides insights into every thread in the system.

* name and id
* state
* blocked/waited counters
* blocked/waited time - These may be -1 depending on if the JVM is configured for measuring the information or not
* stack trace - The stack trace is optional and one can choose how many rows/lines from the stack trace to print for
  each thread (0 is default)

```
JMXJsonBuilder.apply().withThreadInfo(); //same as withThreadInfo(3)
JMXJsonBuilder.apply().withThreadInfo(0); //disables stack trace
```

**Note** The blocked/waited time measurements may not be enabled on the JVM.  
To enable invoke `ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(true)`.   
This may have an impact on the performance so do it with caution.

# Dockerized example

For a full example on the printout from a dockerized Java application refer to the [docker](docker/) submodule.  
It contains small application that easily be built into a Docker and executed locally.

# Dependencies

This library uses [minimal-json](https://github.com/ralfstx/minimal-json) for managing Json format.  
It is a small no-dependencies library fit for the purpose.

# Download

The libraries are plain Java (the Scala part is only unit tests), compiled for compatibility with Java 8+.   
Simply add the following dependency:

sbt

```
"org.dmonix" % "jmx-runtime-json" % [version]
```

maven

```
<dependency>
  <groupId>org.dmonix</groupId>
  <artifactId>jmx-runtime-json</artifactId>
  <version>[version]</version>
</dependency>
```

# License

The library is licensed under [Apache 2](LICENSE) 

