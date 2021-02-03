Test Docker
========

The test docker is meant only to illustrate the output of this library when running in a Docker container.  
The first step is to create the docker image:

```
sbt docker/docker:publishLocal
```

This will generate a docker image with a small app that only prints the `allInfo()` json and then exits.   
Try running the image with

```
 docker run --rm -m 64m --cpus 2 jmx-runtime-json-test-app:[version]
```

Note the memory and cpu restrictions as these are enforced by the container.

```json
{
  "operating-system": {
    "name": "Linux",
    "architecture": "amd64",
    "version": "4.19.121-linuxkit",
    "available-processors": 2,
    "system-load-average": 0.32
  },
  "memory": {
    "heap": {
      "init": 8388608,
      "committed": 8126464,
      "max": 32440320,
      "used": 1895440,
      "pools": [
        {
          "name": "Tenured Gen",
          "usage": {
            "init": 5636096,
            "committed": 5636096,
            "max": 22413312,
            "used": 649608
          },
          "peak-usage": {
            "init": 5636096,
            "committed": 5636096,
            "max": 22413312,
            "used": 649608
          }
        },
        {
          "name": "Eden Space",
          "usage": {
            "init": 2228224,
            "committed": 2228224,
            "max": 8912896,
            "used": 544760
          },
          "peak-usage": {
            "init": 2228224,
            "committed": 2228224,
            "max": 8912896,
            "used": 2228224
          }
        },
        {
          "name": "Survivor Space",
          "usage": {
            "init": 262144,
            "committed": 262144,
            "max": 1114112,
            "used": 262144
          },
          "peak-usage": {
            "init": 262144,
            "committed": 262144,
            "max": 1114112,
            "used": 262144
          }
        }
      ]
    },
    "non-heap": {
      "init": 7667712,
      "committed": 15794176,
      "max": 511705088,
      "used": 8937144,
      "pools": [
        {
          "name": "CodeHeap 'non-nmethods'",
          "usage": {
            "init": 2555904,
            "committed": 2555904,
            "max": 5828608,
            "used": 1074048
          },
          "peak-usage": {
            "init": 2555904,
            "committed": 2555904,
            "max": 5828608,
            "used": 1085568
          }
        },
        {
          "name": "Metaspace",
          "usage": {
            "init": 0,
            "committed": 7864320,
            "max": 134217728,
            "used": 7440960
          },
          "peak-usage": {
            "init": 0,
            "committed": 7864320,
            "max": 134217728,
            "used": 7440960
          }
        },
        {
          "name": "CodeHeap 'profiled nmethods'",
          "usage": {
            "init": 2555904,
            "committed": 2555904,
            "max": 122912768,
            "used": 474240
          },
          "peak-usage": {
            "init": 2555904,
            "committed": 2555904,
            "max": 122912768,
            "used": 474240
          }
        },
        {
          "name": "Compressed Class Space",
          "usage": {
            "init": 0,
            "committed": 786432,
            "max": 125829120,
            "used": 700424
          },
          "peak-usage": {
            "init": 0,
            "committed": 786432,
            "max": 125829120,
            "used": 700424
          }
        },
        {
          "name": "CodeHeap 'non-profiled nmethods'",
          "usage": {
            "init": 2555904,
            "committed": 2555904,
            "max": 122916864,
            "used": 88704
          },
          "peak-usage": {
            "init": 2555904,
            "committed": 2555904,
            "max": 122916864,
            "used": 88704
          }
        }
      ]
    }
  },
  "garbage-collectors": [
    {
      "name": "Copy",
      "collection-count": 1,
      "collection-time": 1,
      "memory-pool-names": [
        "Eden Space",
        "Survivor Space"
      ]
    },
    {
      "name": "MarkSweepCompact",
      "collection-count": 0,
      "collection-time": 0,
      "memory-pool-names": [
        "Eden Space",
        "Survivor Space",
        "Tenured Gen"
      ]
    }
  ],
  "runtime": {
    "vm-name": "OpenJDK 64-Bit Server VM",
    "vm-vendor": "AdoptOpenJDK",
    "vm-version": "11.0.10+9",
    "uptime": 210,
    "start-time": 1612361334853,
    "input-arguments": [
      "-Xss256k",
      "-XX:MaxMetaspaceSize=128m",
      "-XX:+CrashOnOutOfMemoryError",
      "-XX:+UseContainerSupport",
      "-XX:MaxRAMPercentage=75.0"
    ],
    "classpath": [
      "/opt/docker/lib/org.dmonix.jmx-runtime-json-test-app-1.2.0.jar",
      "/opt/docker/lib/org.dmonix.jmx-runtime-json-1.2.0.jar",
      "/opt/docker/lib/com.eclipsesource.minimal-json.minimal-json-0.9.5.jar"
    ]
  },
  "thread": {
    "current-thread-count": 5,
    "daemon-thread-count": 4,
    "peak-thread-count": 5,
    "thread-cpu-time-enabled": true,
    "thread-contention-monitoring-enabled": false,
    "threads": [
      {
        "name": "main",
        "id": 1,
        "blocked-count": 0,
        "blocked-time": -1,
        "waited-count": 0,
        "waited-time": -1,
        "state": "RUNNABLE",
        "stack-trace": [
          "sun.management.ThreadImpl.getThreadInfo1(Native method)",
          "sun.management.ThreadImpl.getThreadInfo(ThreadImpl.java:197)",
          "org.dmonix.jmx.JMXJsonBuilder.withThreadInfo(JMXJsonBuilder.java:197)"
        ]
      },
      {
        "name": "Reference Handler",
        "id": 2,
        "blocked-count": 1,
        "blocked-time": -1,
        "waited-count": 0,
        "waited-time": -1,
        "state": "RUNNABLE",
        "stack-trace": [
          "java.lang.ref.Reference.waitForReferencePendingList(Native method)",
          "java.lang.ref.Reference.processPendingReferences(Reference.java:241)",
          "java.lang.ref.Reference$ReferenceHandler.run(Reference.java:213)"
        ]
      },
      {
        "name": "Finalizer",
        "id": 3,
        "blocked-count": 1,
        "blocked-time": -1,
        "waited-count": 2,
        "waited-time": -1,
        "state": "WAITING",
        "stack-trace": [
          "java.lang.Object.wait(Native method)",
          "java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)",
          "java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:176)"
        ]
      },
      {
        "name": "Signal Dispatcher",
        "id": 4,
        "blocked-count": 0,
        "blocked-time": -1,
        "waited-count": 0,
        "waited-time": -1,
        "state": "RUNNABLE"
      },
      {
        "name": "Common-Cleaner",
        "id": 9,
        "blocked-count": 0,
        "blocked-time": -1,
        "waited-count": 1,
        "waited-time": -1,
        "state": "TIMED_WAITING",
        "stack-trace": [
          "java.lang.Object.wait(Native method)",
          "java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)",
          "jdk.internal.ref.CleanerImpl.run(CleanerImpl.java:148)"
        ]
      }
    ]
  },
  "class-loading": {
    "loaded-classes": 1063,
    "total-loaded-classes": 1063,
    "unloaded-classes": 0
  }
}
```