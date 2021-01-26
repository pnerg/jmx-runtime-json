/**
 * Copyright 2021 Peter Nerg
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dmonix.jmx;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.WriterConfig;

import java.io.File;
import java.lang.management.*;

/**
 * Generates Json formatted runtime information extracted from JMX.
 *
 * <p>The class offers a builder pattern functionality allowing the user to generate a json with the
 * desired information.
 *
 * <p>E.g.
 *
 * <pre>
 *     JMXJsonBuilder.apply().withRuntimeInfo().prettyPrint();
 * </pre>
 *
 * or for convenience one can let the class produce everything it supports
 *
 * <pre>
 *     JMXJsonBuilder.allInfo().prettyPrint();
 * </pre>
 */
public class JMXJsonBuilder {
  private final JsonObject builderJson = Json.object();

  private JMXJsonBuilder() {}

  /**
   * Creates an empty instance of the builder.
   *
   * @return The created empty instance
   */
  public static JMXJsonBuilder apply() {
    return new JMXJsonBuilder();
  }

  /**
   * Creates an instance and adds everything supported by this class.
   *
   * <p>In practice the same as invoking {@link #allInfo(int) allInfo(0)}
   *
   * @return The created and populated instance
   * @see #allInfo(int)
   */
  public static JMXJsonBuilder allInfo() {
    return allInfo(0);
  }

  /**
   * Creates an instance and adds everything supported by this class.
   *
   * <p>The provided argument decides max depth/size of the stack-trace array on each thread.
   * Providing 0 will yield a empty stack-trace array.
   *
   * @param stackTraceDepth The max depth/size of the stack-trace array on each thread
   * @return The created and populated instance
   * @see #withClassLoadingInfo()
   * @see #withMemoryInfo()
   * @see #withRuntimeInfo()
   * @see #withThreadInfo(int)
   * @since 1.1
   */
  public static JMXJsonBuilder allInfo(int stackTraceDepth) {
    return apply()
        .withMemoryInfo(true)
        .withRuntimeInfo()
        .withThreadInfo(stackTraceDepth)
        .withClassLoadingInfo();
  }

  /**
   * Adds runtime information extracted from the 'RuntimeMXBean'
   *
   * <pre>
   *   "runtime": {
   *     "vm-name": "OpenJDK 64-Bit Server VM",
   *     "vm-vendor": "AdoptOpenJDK",
   *     "vm-version": "11.0.9+11",
   *     "uptime": 12573,
   *     "start-time": 1611129944546,
   *     "input-arguments": [
   *       "-Dfile.encoding=UTF-8",
   *       "-Xms1024m",
   *       "-Xmx1024m",
   *       "-Xss4M",
   *       "-XX:ReservedCodeCacheSize=128m"
   *     ],
   *     "classpath": [
   *       "/opt/sbt/bin/sbt-launch.jar"
   *     ]
   *   }
   * </pre>
   *
   * @return itself
   */
  public JMXJsonBuilder withRuntimeInfo() {
    RuntimeMXBean mbean = ManagementFactory.getRuntimeMXBean();

    JsonObject jo = Json.object();
    jo.add("vm-name", mbean.getVmName());
    jo.add("vm-vendor", mbean.getVmVendor());
    jo.add("vm-version", mbean.getVmVersion());
    jo.add("uptime", mbean.getUptime());
    jo.add("start-time", mbean.getStartTime());
    jo.add(
        "input-arguments", Json.array(mbean.getInputArguments().stream().toArray(String[]::new)));
    jo.add("classpath", Json.array(mbean.getClassPath().split(File.pathSeparator)));

    builderJson.add("runtime", jo);
    return this;
  }

  /**
   * Adds runtime information extracted from the 'ThreadMXBean' without stack trace information.
   *
   * <p>In practice the same as invoking {@link #withThreadInfo(int) withThreadInfo(0)}
   *
   * @return itself
   * @see #withThreadInfo(int)
   */
  public JMXJsonBuilder withThreadInfo() {
    return withThreadInfo(5);
  }

  /**
   * Adds runtime information extracted from the 'ThreadMXBean' with optional stack trace depth.
   *
   * <p>The blocked/waited time is only measured if the JVM is configured to measure thread
   * contention time.
   *
   * <p>The provided argument decides max depth/size of the stack-trace array on each thread. <br>
   * Providing 0 will yield a empty stack-trace array. <br>
   * E.g. invoking {@link #withThreadInfo(int) withThreadInfo(4)}
   *
   * <pre>
   *  "thread": {
   *     "current-thread-count": 46,
   *     "daemon-thread-count": 25,
   *     "peak-thread-count": 49,
   *      "thread-cpu-time-enabled": true,
   *     "thread-contention-monitoring-enabled": true,
   *     "threads": [
   *       {
   *         "name": "main",
   *         "id": 1,
   *         "blocked-count": 223,
   *         "blocked-time": -1,
   *         "waited-count": 665,
   *         "waited-time": -1,
   *         "state": "WAITING",
   *         "stack-trace": [
   *           "jdk.internal.misc.Unsafe(Native method)",
   *           "java.util.concurrent.locks.LockSupport(LockSupport.java:194)",
   *           "java.util.concurrent.ForkJoinPool(ForkJoinPool.java:1628)",
   *           "java.util.concurrent.ForkJoinWorkerThread(ForkJoinWorkerThread.java:183)"
   *         ]
   *       },
   *       ...
   *    ]
   * }
   * </pre>
   *
   * @param stackTraceDepth The max depth/size of the stack-trace array on each thread
   * @return itself
   * @since 1.1
   * @see ThreadMXBean#setThreadCpuTimeEnabled(boolean)
   * @see ThreadMXBean#setThreadContentionMonitoringEnabled(boolean)
   */
  public JMXJsonBuilder withThreadInfo(int stackTraceDepth) {
    ThreadMXBean mbean = ManagementFactory.getThreadMXBean();

    // create detailed info for each thread
    JsonArray threadArrayJson = Json.array();
    for (ThreadInfo ti : mbean.getThreadInfo(mbean.getAllThreadIds(), stackTraceDepth)) {

      JsonArray stackArray = Json.array();
      for (StackTraceElement ste : ti.getStackTrace()) {
        stackArray.add(asString(ste));
      }

      JsonObject to = Json.object();
      to.add("name", ti.getThreadName());
      to.add("id", ti.getThreadId());
      to.add("blocked-count", ti.getBlockedCount());
      to.add("blocked-time", ti.getBlockedTime());
      to.add("waited-count", ti.getWaitedCount());
      to.add("waited-time", ti.getWaitedTime());
      to.add("state", ti.getThreadState().name());
      to.add("stack-trace", stackArray);

      threadArrayJson.add(to);
    }

    JsonObject jo = Json.object();
    jo.add("current-thread-count", mbean.getThreadCount());
    jo.add("daemon-thread-count", mbean.getDaemonThreadCount());
    jo.add("peak-thread-count", mbean.getPeakThreadCount());
    jo.add("thread-cpu-time-enabled", mbean.isThreadCpuTimeEnabled());
    jo.add("thread-contention-monitoring-enabled", mbean.isThreadContentionMonitoringEnabled());
    jo.add("threads", threadArrayJson);

    builderJson.add("thread", jo);
    return this;
  }

  /**
   * Adds runtime information extracted from the 'MemoryMXBean'.
   *
   * <p>In practice the same as invoking {@link #withMemoryInfo(boolean) withMemoryInfo(false)}
   *
   * <pre>
   *   "memory": {
   *     "heap": {
   *       "init": 1073741824,
   *       "committed": 1073741824,
   *       "max": 17179869184,
   *       "used": 4194304
   *     },
   *     "non-heap": {
   *       "init": 7667712,
   *       "committed": 32374784,
   *       "max": -1,
   *       "used": 28859904
   *     }
   *   }
   * </pre>
   *
   * @return itself
   * @see #withMemoryInfo(boolean)
   */
  public JMXJsonBuilder withMemoryInfo() {
    return withMemoryInfo(false);
  }

  /**
   * Adds runtime information extracted from the 'MemoryMXBean'.
   *
   * <p>The <i>pool</i> are optional.
   *
   * <pre>
   *   "memory": {
   *     "heap": {
   *       "init": 1073741824,
   *       "committed": 1073741824,
   *       "max": 17179869184,
   *       "used": 4194304,
   *       "pools": [
   *         {
   *           "name": "G1 Eden Space",
   *           "usage": {
   *             "init": 54525952,
   *             "committed": 46137344,
   *             "max": -1,
   *             "used": 4194304
   *           },
   *        ...
   *       ]
   *     },
   *     "non-heap": {
   *       "init": 7667712,
   *       "committed": 32374784,
   *       "max": -1,
   *       "used": 28859904
   *       "pools": [
   *         {
   *           "name": "CodeHeap 'non-nmethods'",
   *           "usage": {
   *             "init": 2555904,
   *             "committed": 2949120,
   *             "max": 7598080,
   *             "used": 2865920
   *           },
   *         ...
   *       ]
   *     }
   *   }
   * </pre>
   *
   * @param includeMemoryPools If details on each individual memory pool shall be provided
   * @return itself
   * @since 1.2
   */
  public JMXJsonBuilder withMemoryInfo(boolean includeMemoryPools) {
    MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

    JsonObject jo = Json.object();
    JsonObject heap = memoryAsJson(mbean.getHeapMemoryUsage());
    JsonObject nonHeap = memoryAsJson(mbean.getNonHeapMemoryUsage());

    if (includeMemoryPools) {
      JsonArray heapPools = Json.array();
      JsonArray nonHeapPools = Json.array();
      ManagementFactory.getMemoryPoolMXBeans().stream()
          .filter(MemoryPoolMXBean::isValid)
          .forEach(
              poolMBean -> {
                JsonObject poolObject = Json.object();
                poolObject.add("name", poolMBean.getName());
                poolObject.add("usage", memoryAsJson(poolMBean.getUsage()));
                poolObject.add("peak-usage", memoryAsJson(poolMBean.getPeakUsage()));

                // add the json to the correct array
                if (poolMBean.getType() == MemoryType.HEAP) heapPools.add(poolObject);
                else nonHeapPools.add(poolObject);
              });

      heap.add("pools", heapPools);
      nonHeap.add("pools", nonHeapPools);
    }

    jo.add("heap", heap);
    jo.add("non-heap", nonHeap);

    builderJson.add("memory", jo);
    return this;
  }

  /**
   * Adds runtime information extracted from the 'ClassLoadingMXBean'
   *
   * <pre>
   *   "class-loading": {
   *     "loaded-classes": 2527,
   *     "total-loaded-classes": 2527,
   *     "unloaded-classes": 0
   *   }
   * </pre>
   *
   * @return itself
   */
  public JMXJsonBuilder withClassLoadingInfo() {
    ClassLoadingMXBean mbean = ManagementFactory.getClassLoadingMXBean();

    JsonObject jo = Json.object();
    jo.add("loaded-classes", mbean.getLoadedClassCount());
    jo.add("total-loaded-classes", mbean.getTotalLoadedClassCount());
    jo.add("unloaded-classes", mbean.getUnloadedClassCount());

    builderJson.add("class-loading", jo);
    return this;
  }

  static String asString(StackTraceElement ste) {
    String fileName = ste.getFileName();
    int lineNumber = ste.getLineNumber();
    if (ste.isNativeMethod()) {
      return String.format("%s.%s(Native method)", ste.getClassName(), ste.getMethodName());
    } else if (fileName != null && lineNumber >= 0) { // we have both file name and line number
      return String.format(
          "%s.%s(%s:%d)", ste.getClassName(), ste.getMethodName(), fileName, lineNumber);
    } else if (fileName != null) { // we only have file name
      return String.format("%s.%s(%s)", ste.getClassName(), ste.getMethodName(), fileName);
    }
    return String.format("%s.%s(Unknown source)", ste.getClassName(), ste.getMethodName());
  }

  /**
   * Converts the provided memory usage info into a Json object
   *
   * @param mem
   * @return
   */
  private static JsonObject memoryAsJson(MemoryUsage mem) {
    JsonObject json = Json.object();
    json.add("init", mem.getInit());
    json.add("committed", mem.getCommitted());
    json.add("max", mem.getMax());
    json.add("used", mem.getUsed());
    return json;
  }

  /**
   * Returns the built json object
   *
   * @return The json object
   */
  public JsonObject asJson() {
    return builderJson;
  }

  /**
   * Returns the built json pretty printed
   *
   * @return The json string
   */
  public String prettyPrint() {
    return builderJson.toString(WriterConfig.PRETTY_PRINT);
  }

  /**
   * Returns the built json plain printed
   *
   * @return The json string
   */
  @Override
  public String toString() {
    return builderJson.toString();
  }
}
