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

package org.apache.linkis.manager.engineplugin.shell.executor;

import org.apache.linkis.engineconn.computation.executor.execute.ConcurrentComputationExecutor;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.governance.common.utils.GovernanceUtils;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.scheduler.executer.ExecuteResponse;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellEngineConnConcurrentExecutor extends ConcurrentComputationExecutor {

  private static final Logger logger =
      LoggerFactory.getLogger(ShellEngineConnConcurrentExecutor.class);

  private ShellEngineConnExecutor ShellEngineConnExecutor;

  private int maxRunningNumber;

  public ShellEngineConnConcurrentExecutor(int id, int maxRunningNumber) {
    super(id);
    this.ShellEngineConnExecutor = new ShellEngineConnExecutor(id);
    this.maxRunningNumber = maxRunningNumber;
  }

  @Override
  public ExecuteResponse executeLine(EngineExecutionContext engineExecutorContext, String code) {
    return ShellEngineConnExecutor.executeLine(engineExecutorContext, code);
  }

  @Override
  public ExecuteResponse executeCompletely(
      EngineExecutionContext engineExecutorContext, String code, String completedLine) {
    return ShellEngineConnExecutor.executeCompletely(engineExecutorContext, code, completedLine);
  }

  @Override
  public float progress(String taskID) {
    return ShellEngineConnExecutor.progress(taskID);
  }

  @Override
  public JobProgressInfo[] getProgressInfo(String taskID) {
    return ShellEngineConnExecutor.getProgressInfo(taskID);
  }

  @Override
  public boolean supportCallBackLogs() {
    return ShellEngineConnExecutor.supportCallBackLogs();
  }

  @Override
  public String getId() {
    return ShellEngineConnExecutor.getId();
  }

  @Override
  public void close() {
    try {
      killAll();
      ShellEngineConnExecutor.logAsyncService.shutdown();
    } catch (Exception e) {
      logger.error("Shell ec failed to close ");
    }
    super.close();
  }

  @Override
  public void killAll() {
    Iterator<ShellECTaskInfo> iterator =
        ShellEngineConnExecutor.shellECTaskInfoCache.values().iterator();
    while (iterator.hasNext()) {
      ShellECTaskInfo shellECTaskInfo = iterator.next();
      killTask(shellECTaskInfo.getTaskId());
    }
  }

  @Override
  public void killTask(String taskID) {
    ShellECTaskInfo shellECTaskInfo = ShellEngineConnExecutor.shellECTaskInfoCache.remove(taskID);
    if (shellECTaskInfo == null) {
      return;
    }

    /*
     Kill sub-processes
    */
    int pid = ShellEngineConnExecutor.getPid(shellECTaskInfo.getProcess());
    GovernanceUtils.killProcess(String.valueOf(pid), "kill task " + taskID + " process", false);

    /*
     Kill yarn-applications
    */
    List<String> yarnAppIds = shellECTaskInfo.getYarnAppIdExtractor().getExtractedYarnAppIds();
    GovernanceUtils.killYarnJobApp(yarnAppIds);
    logger.info(
        "Finished kill yarn app ids in the engine of ({}). The YARN app ids are {}.",
        getId(),
        yarnAppIds);
    super.killTask(taskID);
  }

  @Override
  public int getConcurrentLimit() {
    return maxRunningNumber;
  }

  @Override
  public List<Label<?>> getExecutorLabels() {
    return ShellEngineConnExecutor.getExecutorLabels();
  }

  @Override
  public void setExecutorLabels(List<Label<?>> labels) {
    ShellEngineConnExecutor.setExecutorLabels(labels);
  }

  @Override
  public NodeResource requestExpectedResource(NodeResource expectedResource) {
    return ShellEngineConnExecutor.requestExpectedResource(expectedResource);
  }

  @Override
  public NodeResource getCurrentNodeResource() {
    return ShellEngineConnExecutor.getCurrentNodeResource();
  }
}
