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

class JMXJsonBuilderSpec extends Specification {

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
    val builder = new JMXJsonBuilder().withRuntimeInfo()
    "must produce 'asJson' with the expected contents" >> {
      assertRuntimeContents(builder.asJson())
    }
    "must produce 'toString' with the expected contents" >> {
      assertRuntimeContents(builder.toString)
    }
    "must produce 'prettyPrint' with the expected contents" >> {
      println(builder.prettyPrint())
      assertRuntimeContents(builder.prettyPrint())
    }
  }

  "withThreadInfo" >> {
    val builder = new JMXJsonBuilder().withThreadInfo()
    "must produce a 'asJson' with the expected contents" >> {
      assertThreadContents(builder.asJson())
    }
    "must produce 'toString' with the expected contents" >> {
      assertThreadContents(builder.toString)
    }
    "must produce 'prettyPrint' with the expected contents" >> {
      println(builder.prettyPrint())
      assertThreadContents(builder.prettyPrint)
    }
  }

  private def assertRuntimeContents(string: String): MatchResult[JsonValue] = assertRuntimeContents(Json.parse(string).asObject())

  private def assertRuntimeContents(json: JsonObject): MatchResult[JsonValue] = {
    val runtime = json.getObject("runtime")
    runtime mustHaveAttribute "vm-vendor"
    runtime mustHaveAttribute "vm-name"
    runtime mustHaveAttribute "vm-version"
    runtime mustHaveAttribute "uptime"
    runtime mustHaveAttribute "start-time"
    runtime mustHaveAttribute "input-arguments"
    runtime mustHaveAttribute "classpath"
  }

  private def assertThreadContents(s: String): MatchResult[JsonValue] = assertThreadContents(Json.parse(s).asObject())

  private def assertThreadContents(json: JsonObject): MatchResult[JsonValue] = {
    val runtime = json.getObject("thread")
    runtime mustHaveAttribute "current-thread-count"
    runtime mustHaveAttribute "daemon-thread-count"
    runtime mustHaveAttribute "peak-thread-count"
    runtime mustHaveAttribute "threads"
  }

}
