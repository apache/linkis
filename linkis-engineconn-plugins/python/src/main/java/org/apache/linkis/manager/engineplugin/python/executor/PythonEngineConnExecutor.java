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

package org.apache.linkis.manager.engineplugin.python.executor;

import org.apache.linkis.engineconn.computation.executor.execute.ComputationExecutor;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.engineconn.core.EngineConnObject;
import org.apache.linkis.engineconn.launch.EngineConnServer;
import org.apache.linkis.governance.common.paser.PythonCodeParser;
import org.apache.linkis.manager.common.entity.resource.CommonNodeResource;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils;
import org.apache.linkis.manager.engineplugin.python.conf.PythonEngineConfiguration;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.executer.ExecuteResponse;
import org.apache.linkis.scheduler.executer.SuccessExecuteResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonEngineConnExecutor extends ComputationExecutor {

  private static final Logger logger = LoggerFactory.getLogger(PythonEngineConnExecutor.class);

  private EngineExecutionContext engineExecutionContext;
  private List<Label<?>> executorLabels = new ArrayList<>();

  private int id;
  private PythonSession pythonSession;
  private int outputPrintLimit;

  public PythonEngineConnExecutor(int id, PythonSession pythonSession, int outputPrintLimit) {
    super(outputPrintLimit);
    this.id = id;
    this.pythonSession = pythonSession;
    this.outputPrintLimit = outputPrintLimit;
  }

  @Override
  public void init() {
    logger.info("Ready to change engine state!");
    setCodeParser(new PythonCodeParser());
    super.init();
  }

  private String getPyVersion() {
    if (null != EngineConnServer.getEngineCreationContext().getOptions()) {
      return EngineConnServer.getEngineCreationContext()
          .getOptions()
          .getOrDefault("python.version", "python");
    } else {
      return PythonEngineConfiguration.PYTHON_VERSION.getValue();
    }
  }

  @Override
  public ExecuteResponse executeLine(EngineExecutionContext engineExecutionContext, String code) {
    String pythonVersion =
        engineExecutionContext
            .getProperties()
            .getOrDefault("python.version", getPyVersion())
            .toString()
            .toLowerCase();
    logger.info("EngineExecutionContext user python.version = > {}" + pythonVersion);
    System.getProperties().put("python.version", pythonVersion);
    logger.info(
        "System getProperties python.version = > {}"
            + System.getProperties().getProperty("python.version"));
    pythonSession.lazyInitGateway();

    if (engineExecutionContext != this.engineExecutionContext) {
      this.engineExecutionContext = engineExecutionContext;
      pythonSession.setEngineExecutionContext(engineExecutionContext);
      logger.info("Python executor reset new engineExecutorContext!");
    }
    engineExecutionContext.appendStdout(getId() + " >> " + code.trim());
    pythonSession.execute(code);
    return new SuccessExecuteResponse();
  }

  @Override
  public ExecuteResponse executeCompletely(
      EngineExecutionContext engineExecutionContext, String code, String completedLine) {
    String newcode = completedLine + code;
    logger.debug("newcode is {}", newcode);
    return executeLine(engineExecutionContext, newcode);
  }

  @Override
  public float progress(String taskID) {
    if (null != this.engineExecutionContext) {
      return this.engineExecutionContext.getCurrentParagraph()
          / this.engineExecutionContext.getTotalParagraph();
    } else {
      return 0.0f;
    }
  }

  @Override
  public JobProgressInfo[] getProgressInfo(String taskID) {
    List<JobProgressInfo> jobProgressInfo = new ArrayList<>();
    if (null == this.engineExecutionContext) {
      return jobProgressInfo.toArray(new JobProgressInfo[0]);
    }
    String jobId =
        engineExecutionContext.getJobId().isDefined()
            ? engineExecutionContext.getJobId().get()
            : "";
    if (0.0f == progress(taskID)) {
      jobProgressInfo.add(new JobProgressInfo(jobId, 1, 1, 0, 0));
    } else {
      jobProgressInfo.add(new JobProgressInfo(jobId, 1, 0, 0, 1));
    }
    return jobProgressInfo.toArray(new JobProgressInfo[0]);
  }

  @Override
  public boolean supportCallBackLogs() {
    // todo
    return true;
  }

  @Override
  public NodeResource requestExpectedResource(NodeResource expectedResource) {
    return null;
  }

  @Override
  public NodeResource getCurrentNodeResource() {
    CommonNodeResource resource = new CommonNodeResource();
    resource.setUsedResource(
        NodeResourceUtils.applyAsLoadInstanceResource(
            EngineConnObject.getEngineCreationContext().getOptions()));
    return resource;
  }

  @Override
  public List<Label<?>> getExecutorLabels() {
    return executorLabels;
  }

  @Override
  public void setExecutorLabels(List<Label<?>> labels) {
    if (labels != null) {
      executorLabels.clear();
      executorLabels.addAll(labels);
    }
  }

  @Override
  public String getId() {
    return Sender.getThisServiceInstance().getInstance() + "_" + id;
  }

  @Override
  public void killTask(String taskID) {
    logger.info("Start to kill python task " + taskID);
    super.killTask(taskID);
    logger.info("To close python cli task " + taskID);
    close();
  }

  @Override
  public void close() {
    pythonSession.close();
    logger.info("To delete python executor");
    logger.info("Finished to kill python");
  }
}
