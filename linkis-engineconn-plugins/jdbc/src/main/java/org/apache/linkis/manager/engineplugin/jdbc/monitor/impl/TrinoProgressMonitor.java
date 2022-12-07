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

package org.apache.linkis.manager.engineplugin.jdbc.monitor.impl;

import org.apache.linkis.manager.engineplugin.jdbc.monitor.ProgressMonitor;
import org.apache.linkis.protocol.engine.JobProgressInfo;

import java.sql.Statement;

import io.trino.jdbc.QueryStats;
import io.trino.jdbc.TrinoStatement;

public class TrinoProgressMonitor extends ProgressMonitor<QueryStats> {
  private volatile Runnable callback;
  private volatile double sqlProgress = 0.0;
  private volatile int completedSplits = 0;
  private volatile int totalSplits = 0;
  private volatile int runningSplits = 0;

  @Override
  public void accept(QueryStats stats) {
    sqlProgress = stats.getProgressPercentage().orElse(0.0) / 100;
    completedSplits = stats.getCompletedSplits();
    totalSplits = stats.getTotalSplits();
    runningSplits = stats.getRunningSplits();

    if (callback != null) {
      callback.run();
    }
  }

  @Override
  public void attach(Statement statement) {
    if (statement instanceof TrinoStatement) {
      ((TrinoStatement) statement).setProgressMonitor(this);
    }
  }

  @Override
  public void callback(Runnable callback) {
    this.callback = callback;
  }

  @Override
  public float getSqlProgress() {
    return Double.valueOf(sqlProgress).floatValue();
  }

  @Override
  public int getSucceedTasks() {
    return completedSplits;
  }

  @Override
  public int getTotalTasks() {
    return totalSplits;
  }

  @Override
  public int getRunningTasks() {
    return runningSplits;
  }

  @Override
  public int getFailedTasks() {
    return 0;
  }

  @Override
  public JobProgressInfo jobProgressInfo(String id) {
    return new JobProgressInfo(id, totalSplits, runningSplits, 0, completedSplits);
  }
}
