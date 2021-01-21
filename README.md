![Build & Test](https://github.com/pnerg/jmx-runtime-json/workflows/Build%20&%20Test/badge.svg) [![codecov](https://codecov.io/gh/pnerg/jmx-runtime-json/branch/master/graph/badge.svg?token=O8I3FS7RSI)](https://codecov.io/gh/pnerg/jmx-runtime-json)

JMX Runtime Information As Json
-------

This little library helps to generate a json out of a selected set of the runtime information available in the JVM.   
All of the data is accessible over various managed beans (mbeans) available in the JVM.

But accessing this data over JMX when running the application in a Docker container is difficult the least to say.  
Most applications however expose a web interface and possibly even health and or meta-information over HTTP.

The use case for this library is to convert JMX internal information into Json format that can be transported as e.g. a
HTTP response for debugging purposes.

# Usage

The easiest way to produce a Json with all the data supported by the library is to:

```
String json = JMXJsonBuilder.allInfo().prettyPrint();
```

This will create a Json like the one below (threads cut for brevity).

```
{
  "memory": {
    "heap": {
      "init": 1073741824,
      "committed": 1073741824,
      "max": 1073741824,
      "used": 286342112
    },
    "non-heap": {
      "init": 2555904,
      "committed": 178573312,
      "max": -1,
      "used": 164771184
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
    "threads": [
      {
        "name": "main",
        "id": 1,
        "blocked-count": 222,
        "blocked-time": -1,
        "waited-count": 564,
        "waited-time": -1,
        "state": "WAITING"
      },
      {
        "name": "Reference Handler",
        "id": 2,
        "blocked-count": 8,
        "blocked-time": -1,
        "waited-count": 0,
        "waited-time": -1,
        "state": "RUNNABLE"
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

## Generate subset

The Json is modularised into

* class-loading - `withClassLoadingInfo`
* memory - `withMemoryInfo`
* runtime - `withRuntimeInfo`
* thread - `withThreadInfo`

One can choose to generate only portions of the Json.  
E.g. if I'd only want memory and runtime parts

```
String json = JMXJsonBuilder.apply().withMemoryInfo().withRuntimeInfo().prettyPrint();
```

Would produce something like:

```
{
  "memory": {
    "heap": {
      "init": 1073741824,
      "committed": 1073741824,
      "max": 1073741824,
      "used": 286342112
    },
    "non-heap": {
      "init": 2555904,
      "committed": 178573312,
      "max": -1,
      "used": 164771184
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
  }
}
```

# Dependencies

This library uses [minimal-json](https://github.com/ralfstx/minimal-json) for managing Json format.  
It is a small no-dependencies library fit for the purpose.

# Download

Both libraries are cross-compiled for Scala 2.11, 2.12 and 2.13.  
Simply add the following dependency:

sbt

```
"org.dmonix" %% "jmx-runtime-json" % [version]
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

