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
package org.dmonix.jmx

import com.eclipsesource.json.{Json, JsonArray, JsonObject, JsonValue}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

import java.lang.management.ManagementFactory

/**
 * Tests for ''JMXJsonBuilderSpec''
 */
class JMXJsonBuilderSpec extends Specification {

  //enables measurement of blocked/waited time of threads
  ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(true)
  private[this] type AssertJsonFunction = JsonObject => MatchResult[_]

  private[this] implicit class PimpedString(s: String) {
    def parseJson: JsonObject = Json.parse(s).asObject()
  }

  private[this] implicit class PimpedJsonObject(json: JsonObject) {
    def getLong(name: String) = getNonNull(name).asLong()

    def getString(name: String): String = getNonNull(name).asString()

    def getObject(name: String): JsonObject = getNonNull(name).asObject()

    def getInt(name: String): Int = getNonNull(name).asInt()

    def getArray(name: String): JsonArray = getNonNull(name).asArray()

    def getNonNull(name: String): JsonValue = {
      Option(json.get(name)) match {
        case Some(v) => v
        case None => throw new NullPointerException(s"No such attribute [$name]")
      }
    }

    def mustHaveAttribute(name: String): MatchResult[JsonValue] = {
      json.get(name) aka s"missing attribute '$name'" must not(beNull)
    }
  }

  private[this] def builderAsserts(builder: JMXJsonBuilder, assertJsonFunc: AssertJsonFunction) = {
    "must produce 'asJson' with the expected contents" >> {
      assertJsonFunc(builder.asJson())
    }
    "must produce 'compactPrint()' with the expected contents" >> {
      assertJsonFunc(builder.compactPrint().parseJson)
    }
    "must produce 'toString()' with the expected contents" >> {
      assertJsonFunc(builder.toString().parseJson)
    }
    "must produce 'toString(false)' with the expected contents" >> {
      assertJsonFunc(builder.toString(false).parseJson)
    }
    "must produce 'toString(true)' with the expected contents" >> {
      assertJsonFunc(builder.toString(true).parseJson)
    }
    "must produce 'prettyPrint' with the expected contents" >> {
      assertJsonFunc(builder.prettyPrint().parseJson)
    }
  }

  "withRuntime" >> {
    val builder = JMXJsonBuilder.apply().withRuntimeInfo()
    builderAsserts(builder, assertRuntimeContents)
  }

  "withThreadInfo" >> {
    "without thread stack info" >> {
      val builder = JMXJsonBuilder.apply().withThreadInfo()
      builderAsserts(builder, assertThreadContents(_, false))
    }
    "with thread stack info" >> {
      val builder = JMXJsonBuilder.apply().withThreadInfo(3)
      builderAsserts(builder, assertThreadContents(_, true))
    }
  }

  "withMemoryInfo" >> {
    "without pool information" >> {
      val builder = JMXJsonBuilder.apply().withMemoryInfo()
      builderAsserts(builder, assertMemoryContents(_, false))
    }
    "with pool information" >> {
      val builder = JMXJsonBuilder.apply().withMemoryInfo(true)
      builderAsserts(builder, assertMemoryContents(_, true))
    }
  }

  "withClassLoadingInfo" >> {
    val builder = JMXJsonBuilder.apply().withClassLoadingInfo()
    builderAsserts(builder, assertClassLoadingContents)
  }

  "withOperatingSystemInfo" >> {
    val builder = JMXJsonBuilder.apply().withOperatingSystemInfo()
    builderAsserts(builder, assertOperatingSystemInfo)
  }

  "withGarbageCollectionInfo" >> {
    val builder = JMXJsonBuilder.apply().withGarbageCollectionInfo()
    builderAsserts(builder, assertGarbageCollectionContents)
  }

  "allInfo" >> {
    def assertAllInfo(json: JsonObject, expectingStackInfo: Boolean = false): MatchResult[_] = {
      assertMemoryContents(json, true)
      assertRuntimeContents(json)
      assertThreadContents(json, expectingStackInfo)
      assertClassLoadingContents(json)
      assertOperatingSystemInfo(json)
      assertGarbageCollectionContents(json)
    }

    "without thread stack info" >> {
      val builder = JMXJsonBuilder.allInfo(0)
      builderAsserts(builder, assertAllInfo(_, false))
    }

    "default size thread stack info" >> {
      val builder = JMXJsonBuilder.allInfo()
      builderAsserts(builder, assertAllInfo(_, true))
    }

    "with thread stack info" >> {
      val builder = JMXJsonBuilder.allInfo(3)
      builderAsserts(builder, assertAllInfo(_, true))
    }
  }

  "StackTraceElement asString" >> {
    val declaringClass = "org.dmonix.FooClass"
    val methodName = "execute"
    val fileName = "JMXBuilderSpec.scala"
    val lineNumber = 69
    "with class, file and line number shall return full line" >> {
      JMXJsonBuilder.asString(new StackTraceElement(declaringClass, methodName, fileName, lineNumber)) === s"$declaringClass.$methodName($fileName:$lineNumber)"
    }
    "with class, file and no line number shall return string without line number" >> {
      JMXJsonBuilder.asString(new StackTraceElement(declaringClass, methodName, fileName, -1)) === s"$declaringClass.$methodName($fileName)"
    }
    "with class, no file and no line number shall return line with class name" >> {
      JMXJsonBuilder.asString(new StackTraceElement(declaringClass, methodName, null, -1)) === s"$declaringClass.$methodName(Unknown source)"
    }
    "with native method shall return line with class name" >> {
      //apparently line.nr -2 counts as native method...hmmm
      JMXJsonBuilder.asString(new StackTraceElement(declaringClass, methodName, fileName, -2)) === s"$declaringClass.$methodName(Native method)"
    }
  }

  private[this] def assertRuntimeContents(json: JsonObject): MatchResult[_] = {
    val obj = json.getObject("runtime")
    obj mustHaveAttribute "vm-vendor"
    obj mustHaveAttribute "vm-name"
    obj mustHaveAttribute "vm-version"
    obj mustHaveAttribute "uptime"
    obj mustHaveAttribute "start-time"
    obj mustHaveAttribute "input-arguments"
    obj mustHaveAttribute "classpath"
  }

  private[this] def assertThreadContents(json: JsonObject, expectingStackInfo: Boolean = false): MatchResult[_] = {
    val obj = json.getObject("thread")
    obj mustHaveAttribute "current-thread-count"
    obj mustHaveAttribute "daemon-thread-count"
    obj mustHaveAttribute "peak-thread-count"
    obj mustHaveAttribute "thread-cpu-time-enabled"
    obj mustHaveAttribute "thread-contention-monitoring-enabled"
    obj mustHaveAttribute "threads"

    //should be at least a few threads so we check the first one
    val threadsJson = obj.getArray("threads")
    threadsJson.size() === obj.getInt("current-thread-count")
    val thread = threadsJson.get(0).asObject()
    if (expectingStackInfo)
      thread mustHaveAttribute "stack-trace"
    thread mustHaveAttribute "name"
    thread mustHaveAttribute "id"
    thread mustHaveAttribute "blocked-count"
    thread mustHaveAttribute "blocked-time"
    thread mustHaveAttribute "waited-count"
    thread mustHaveAttribute "waited-time"
    thread mustHaveAttribute "state"
  }

  private[this] def assertMemoryContents(json: JsonObject, expectingPoolInfo: Boolean = false): MatchResult[_] = {
    val obj = json.getObject("memory")
    obj mustHaveAttribute "heap"
    obj mustHaveAttribute "non-heap"

    val heap = obj.getObject("heap")
    if (expectingPoolInfo)
      heap mustHaveAttribute "pools"
    heap mustHaveAttribute "init"
    heap mustHaveAttribute "committed"
    heap mustHaveAttribute "max"
    heap mustHaveAttribute "used"

    val nonheap = obj.getObject("non-heap")
    if (expectingPoolInfo)
      nonheap mustHaveAttribute "pools"
    nonheap mustHaveAttribute "init"
    nonheap mustHaveAttribute "committed"
    nonheap mustHaveAttribute "max"
    nonheap mustHaveAttribute "used"
  }

  private[this] def assertOperatingSystemInfo(json: JsonObject): MatchResult[_] = {
    val obj = json.getObject("operating-system")
    obj mustHaveAttribute "name"
    obj mustHaveAttribute "architecture"
    obj mustHaveAttribute "version"
    obj mustHaveAttribute "available-processors"
    obj mustHaveAttribute "system-load-average"
  }

  private[this] def assertClassLoadingContents(json: JsonObject): MatchResult[_] = {
    val obj = json.getObject("class-loading")
    obj mustHaveAttribute "loaded-classes"
    obj mustHaveAttribute "total-loaded-classes"
    obj mustHaveAttribute "unloaded-classes"
  }

  private[this] def assertGarbageCollectionContents(json: JsonObject): MatchResult[_] = {
    val array = json.getArray("garbage-collectors")

    //there should at least be one GC
    val obj = array.get(0).asObject()
    obj mustHaveAttribute "name"
    obj mustHaveAttribute "collection-count"
    obj mustHaveAttribute "collection-time"
    obj mustHaveAttribute "memory-pool-names"
  }

}
