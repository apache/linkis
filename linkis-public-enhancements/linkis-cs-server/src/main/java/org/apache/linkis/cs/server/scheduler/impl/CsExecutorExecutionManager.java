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

package org.apache.linkis.cs.server.scheduler.impl;

import org.apache.linkis.scheduler.executer.Executor;
import org.apache.linkis.scheduler.executer.ExecutorManager;
import org.apache.linkis.scheduler.listener.ExecutorListener;
import org.apache.linkis.scheduler.queue.SchedulerEvent;

import scala.Option;
import scala.Some;
import scala.concurrent.duration.Duration;

public class CsExecutorExecutionManager extends ExecutorManager {

  @Override
  public void setExecutorListener(ExecutorListener executorListener) {}

  @Override
  public Executor createExecutor(SchedulerEvent event) {
    return new CsExecutor();
  }

  @Override
  public Option<Executor> askExecutor(SchedulerEvent event) {
    return new Some<>(createExecutor(event));
  }

  @Override
  public Option<Executor> askExecutor(SchedulerEvent event, Duration wait) {
    return askExecutor(event);
  }

  @Override
  public Option<Executor> getById(long id) {
    return new Some<>(null);
  }

  @Override
  public Executor[] getByGroup(String groupName) {
    return new Executor[0];
  }

  @Override
  public void delete(Executor executor) {}

  @Override
  public void shutdown() {}
}
