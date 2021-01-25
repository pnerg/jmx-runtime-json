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

import com.eclipsesource.json.{Json, JsonObject, JsonValue}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

/**
 * Tests for ''JMXJsonBuilderSpec''
 */
class JMXJsonBuilderSpec extends Specification {

  implicit class PimpedString(s: String) {
    def parseJson: JsonObject = Json.parse(s).asObject()
  }

  implicit class PimpedJsonObject(json: JsonObject) {
    def getLong(name: String) = {
      getNonNull(name).asLong()
    }

    def getString(name: String) = {
      getNonNull(name).asString()
    }

    def getObject(name: String) = {
      getNonNull(name).asObject()
    }

    def getNonNull(name: String): JsonValue = {
      Option(json.get(name)) match {
        case Some(v) => v
        case None => throw new NullPointerException(s"No such attribute [$name]")
      }
    }

    def mustHaveAttribute(name: String) = {
      json.get(name) must not(beNull)
    }
  }

  "withRuntime" >> {
    val builder = JMXJsonBuilder.apply().withRuntimeInfo()
    "must produce 'asJson' with the expected contents" >> {
      assertRuntimeContents(builder.asJson())
    }
    "must produce 'toString' with the expected contents" >> {
      assertRuntimeContents(builder.toString.parseJson)
    }
    "must produce 'prettyPrint' with the expected contents" >> {
      assertRuntimeContents(builder.prettyPrint.parseJson)
    }
  }

  "withThreadInfo" >> {
    "without thread stack info" >> {
      val builder = JMXJsonBuilder.apply().withThreadInfo()
      "must produce a 'asJson' with the expected contents" >> {
        assertThreadContents(builder.asJson())
      }
      "must produce 'toString' with the expected contents" >> {
        assertThreadContents(builder.toString.parseJson)
      }
      "must produce 'prettyPrint' with the expected contents" >> {
        assertThreadContents(builder.prettyPrint.parseJson)
      }
    }
    "with thread stack info" >> {
      val builder = JMXJsonBuilder.apply().withThreadInfo(3)
      "must produce a 'asJson' with the expected contents" >> {
        assertThreadContents(builder.asJson())
      }
      "must produce 'toString' with the expected contents" >> {
        assertThreadContents(builder.toString.parseJson)
      }
      "must produce 'prettyPrint' with the expected contents" >> {
        assertThreadContents(builder.prettyPrint.parseJson)
      }
    }
  }

  "withMemoryInfo" >> {
    val builder = JMXJsonBuilder.apply().withMemoryInfo()
    "must produce a 'asJson' with the expected contents" >> {
      assertMemoryContents(builder.asJson())
    }
    "must produce 'toString' with the expected contents" >> {
      assertMemoryContents(builder.toString.parseJson)
    }
    "must produce 'prettyPrint' with the expected contents" >> {
      assertMemoryContents(builder.prettyPrint.parseJson)
    }
  }

  "withClassLoadingInfo" >> {
    val builder = JMXJsonBuilder.apply().withClassLoadingInfo()
    "must produce a 'asJson' with the expected contents" >> {
      assertClassLoadingContents(builder.asJson())
    }
    "must produce 'toString' with the expected contents" >> {
      assertClassLoadingContents(builder.toString.parseJson)
    }
    "must produce 'prettyPrint' with the expected contents" >> {
      assertClassLoadingContents(builder.prettyPrint.parseJson)
    }
  }

  "allInfo with no stack-trace" >> {
    val builder = JMXJsonBuilder.allInfo()

    def assertAllInfo(json: JsonObject): MatchResult[_] = {
      assertMemoryContents(json)
      assertRuntimeContents(json)
      assertThreadContents(json)
      assertClassLoadingContents(json)
    }

    "must produce a 'asJson' with the expected contents" >> {
      assertAllInfo(builder.asJson())
    }
    "must produce 'toString' with the expected contents" >> {
      assertAllInfo(builder.toString.parseJson)
    }
    "must produce 'prettyPrint' with the expected contents" >> {
      assertAllInfo(builder.prettyPrint.parseJson)
    }
  }

  "allInfo with stack-trace" >> {
    val builder = JMXJsonBuilder.allInfo(3)

    def assertAllInfo(json: JsonObject): MatchResult[_] = {
      assertMemoryContents(json)
      assertRuntimeContents(json)
      assertThreadContents(json)
      assertClassLoadingContents(json)
    }

    "must produce a 'asJson' with the expected contents" >> {
      assertAllInfo(builder.asJson())
    }
    "must produce 'toString' with the expected contents" >> {
      assertAllInfo(builder.toString.parseJson)
    }
    "must produce 'prettyPrint' with the expected contents" >> {
      println(builder.prettyPrint)
      assertAllInfo(builder.prettyPrint.parseJson)
    }
  }


  private def assertRuntimeContents(json: JsonObject): MatchResult[_] = {
    val obj = json.getObject("runtime")
    obj mustHaveAttribute "vm-vendor"
    obj mustHaveAttribute "vm-name"
    obj mustHaveAttribute "vm-version"
    obj mustHaveAttribute "uptime"
    obj mustHaveAttribute "start-time"
    obj mustHaveAttribute "input-arguments"
    obj mustHaveAttribute "classpath"
  }

  private def assertThreadContents(json: JsonObject): MatchResult[_] = {
    val obj = json.getObject("thread")
    obj mustHaveAttribute "current-thread-count"
    obj mustHaveAttribute "daemon-thread-count"
    obj mustHaveAttribute "peak-thread-count"
    obj mustHaveAttribute "threads"

    //should be at least a few threads so we check the first one
    obj.get("threads").asArray().isEmpty must beFalse
    val thread = obj.get("threads").asArray().get(0).asObject()
    thread mustHaveAttribute "name"
    thread mustHaveAttribute "id"
    thread mustHaveAttribute "blocked-count"
    thread mustHaveAttribute "blocked-time"
    thread mustHaveAttribute "waited-count"
    thread mustHaveAttribute "waited-time"
    thread mustHaveAttribute "state"
    thread mustHaveAttribute "stack-trace"
  }

  private def assertMemoryContents(json: JsonObject): MatchResult[_] = {
    val obj = json.getObject("memory")
    obj mustHaveAttribute "heap"
    obj mustHaveAttribute "non-heap"

    val heap = obj.getObject("heap")
    heap mustHaveAttribute "init"
    heap mustHaveAttribute "committed"
    heap mustHaveAttribute "max"
    heap mustHaveAttribute "used"

    val nonheap = obj.getObject("non-heap")
    nonheap mustHaveAttribute "init"
    nonheap mustHaveAttribute "committed"
    nonheap mustHaveAttribute "max"
    nonheap mustHaveAttribute "used"
  }

  private def assertClassLoadingContents(json: JsonObject): MatchResult[_] = {
    val obj = json.getObject("class-loading")
    obj mustHaveAttribute "loaded-classes"
    obj mustHaveAttribute "total-loaded-classes"
    obj mustHaveAttribute "unloaded-classes"
  }

}
