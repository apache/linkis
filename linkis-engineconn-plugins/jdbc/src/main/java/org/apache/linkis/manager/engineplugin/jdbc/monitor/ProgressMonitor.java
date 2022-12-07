/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.engineplugin.jdbc.monitor;

import org.apache.linkis.protocol.engine.JobProgressInfo;

import org.apache.commons.lang3.StringUtils;

import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.alibaba.druid.pool.DruidPooledStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProgressMonitor<T> implements Consumer<T> {
  private static final Logger LOG = LoggerFactory.getLogger(ProgressMonitor.class);
  private static final Map<String, String> MONITORS = new ConcurrentHashMap<>();

  static {
    register(
        "io.trino.jdbc.TrinoStatement",
        "org.apache.linkis.manager.engineplugin.jdbc.monitor.impl.TrinoProgressMonitor");
  }

  public static void register(String statementClassName, String monitorClassName) {
    MONITORS.put(statementClassName, monitorClassName);
  }

  public static ProgressMonitor<?> attachMonitor(Statement statement) {
    ProgressMonitor<?> progressMonitor = null;
    /* unwrap the druid statement */
    if (statement instanceof DruidPooledStatement) {
      statement = ((DruidPooledStatement) statement).getStatement();
    }
    try {
      String monitorName = MONITORS.get(statement.getClass().getName());
      if (StringUtils.isNotBlank(monitorName)) {
        progressMonitor = (ProgressMonitor<?>) Class.forName(monitorName).newInstance();
      }
      if (progressMonitor != null) {
        progressMonitor.attach(statement);
      }
    } catch (Exception e) {
      LOG.warn(
          "Failed to create monitor for statement: {}, exception: {} - {}",
          statement,
          e.getClass().getName(),
          e.getMessage());
    }
    return progressMonitor;
  }

  public abstract void attach(Statement statement);

  public abstract void callback(Runnable callback);

  public abstract float getSqlProgress();

  public abstract int getSucceedTasks();

  public abstract int getTotalTasks();

  public abstract int getRunningTasks();

  public abstract int getFailedTasks();

  public abstract JobProgressInfo jobProgressInfo(String id);
}
