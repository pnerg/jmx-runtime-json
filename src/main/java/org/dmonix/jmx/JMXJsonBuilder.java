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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class JMXJsonBuilder {
  private final JsonObject builderJson = Json.object();

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

  public JMXJsonBuilder withThreadInfo() {
    ThreadMXBean mbean = ManagementFactory.getThreadMXBean();

    // create detailed info for each thread
    JsonArray threadArrayJson = Json.array();
    for (ThreadInfo ti : mbean.getThreadInfo(mbean.getAllThreadIds())) {
      JsonObject to = Json.object();
      to.add("name", ti.getThreadName());
      to.add("id", ti.getThreadId());
      to.add("blocked-count", ti.getBlockedCount());
      to.add("blocked-time", ti.getBlockedTime());
      to.add("waited-count", ti.getWaitedCount());
      to.add("waited-time", ti.getWaitedTime());
      to.add("state", ti.getThreadState().name());
      threadArrayJson.add(to);
    }

    JsonObject jo = Json.object();
    jo.add("current-thread-count", mbean.getThreadCount());
    jo.add("daemon-thread-count", mbean.getDaemonThreadCount());
    jo.add("peak-thread-count", mbean.getPeakThreadCount());
    jo.add("threads", threadArrayJson);

    builderJson.add("thread", jo);
    return this;
  }

  public JsonObject asJson() {
    return builderJson;
  }

  public String prettyPrint() {
    return builderJson.toString(WriterConfig.PRETTY_PRINT);
  }

  public String toString() {
    return builderJson.toString();
  }
}
