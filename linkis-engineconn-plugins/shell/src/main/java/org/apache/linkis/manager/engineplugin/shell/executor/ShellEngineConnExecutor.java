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

import org.apache.linkis.engineconn.computation.executor.execute.ComputationExecutor;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.engineconn.core.EngineConnObject;
import org.apache.linkis.governance.common.utils.GovernanceUtils;
import org.apache.linkis.manager.common.entity.resource.CommonNodeResource;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils;
import org.apache.linkis.manager.engineplugin.shell.common.ShellEngineConnPluginConst;
import org.apache.linkis.manager.engineplugin.shell.exception.ShellCodeErrorException;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse;
import org.apache.linkis.scheduler.executer.ExecuteResponse;
import org.apache.linkis.scheduler.executer.SuccessExecuteResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellEngineConnExecutor extends ComputationExecutor {
  private static final Logger logger = LoggerFactory.getLogger(ShellEngineConnExecutor.class);

  private int id;
  private EngineExecutionContext engineExecutionContext;
  private List<Label<?>> executorLabels = new ArrayList<>();
  private Process process;
  private YarnAppIdExtractor extractor;

  public ShellEngineConnExecutor(int id) {
    super(id);
    this.id = id;
  }

  @Override
  public void init() {
    logger.info("Ready to change engine state!");
    super.init();
  }

  @Override
  public ExecuteResponse executeCompletely(
      EngineExecutionContext engineExecutionContext, String code, String completedLine) {
    final String newcode = completedLine + code;
    logger.debug("newcode is " + newcode);
    return executeLine(engineExecutionContext, newcode);
  }

  @Override
  public ExecuteResponse executeLine(EngineExecutionContext engineExecutionContext, String code) {
    if (engineExecutionContext != null) {
      this.engineExecutionContext = engineExecutionContext;
      logger.info("Shell executor reset new engineExecutionContext!");
    }

    BufferedReader bufferedReader = null;
    BufferedReader errorsReader = null;

    AtomicBoolean completed = new AtomicBoolean(false);
    ReaderThread errReaderThread = null;
    ReaderThread inputReaderThread = null;

    try {
      engineExecutionContext.appendStdout(getId() + " >> " + code.trim());

      String[] argsArr = null;
      if (engineExecutionContext.getTotalParagraph() == 1
          && engineExecutionContext.getProperties() != null
          && engineExecutionContext
              .getProperties()
              .containsKey(ShellEngineConnPluginConst.RUNTIME_ARGS_KEY)) {

        ArrayList<String> argsList =
            (ArrayList<String>)
                engineExecutionContext
                    .getProperties()
                    .get(ShellEngineConnPluginConst.RUNTIME_ARGS_KEY);

        try {
          argsArr = argsList.toArray(new String[argsList.size()]);
          logger.info(
              "Will execute shell task with user-specified arguments: '{}'",
              Arrays.toString(argsArr));
        } catch (Exception t) {
          logger.warn(
              "Cannot read user-input shell arguments. Will execute shell task without them.", t);
        }
      }

      String workingDirectory = null;
      if (engineExecutionContext.getTotalParagraph() == 1
          && engineExecutionContext.getProperties() != null
          && engineExecutionContext
              .getProperties()
              .containsKey(ShellEngineConnPluginConst.SHELL_RUNTIME_WORKING_DIRECTORY)) {

        String wdStr =
            (String)
                engineExecutionContext
                    .getProperties()
                    .get(ShellEngineConnPluginConst.SHELL_RUNTIME_WORKING_DIRECTORY);

        try {
          if (isExecutePathExist(wdStr)) {
            logger.info(
                "Will execute shell task under user-specified working-directory: '" + wdStr + "'");
            workingDirectory = wdStr;
          } else {
            logger.warn(
                "User-specified working-directory: '"
                    + wdStr
                    + "' does not exist or user does not have access permission. "
                    + "Will execute shell task under default working-directory. Please contact the administrator!");
          }
        } catch (Exception t) {
          logger.warn(
              "Cannot read user-input working-directory. Will execute shell task under default working-directory.",
              t);
        }
      }

      String[] generatedCode =
          argsArr == null || argsArr.length == 0
              ? generateRunCode(code)
              : generateRunCodeWithArgs(code, argsArr);

      ProcessBuilder processBuilder = new ProcessBuilder(generatedCode);
      if (StringUtils.isNotBlank(workingDirectory)) {
        processBuilder.directory(new File(workingDirectory));
      }

      processBuilder.redirectErrorStream(false);
      extractor = new YarnAppIdExtractor();
      process = processBuilder.start();

      bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      errorsReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      CountDownLatch counter = new CountDownLatch(2);
      inputReaderThread =
          new ReaderThread(engineExecutionContext, bufferedReader, extractor, true, counter);
      errReaderThread =
          new ReaderThread(engineExecutionContext, errorsReader, extractor, false, counter);

      inputReaderThread.start();
      errReaderThread.start();

      int exitCode = process.waitFor();
      counter.await();

      completed.set(true);

      if (exitCode != 0) {
        return new ErrorExecuteResponse("run shell failed", new ShellCodeErrorException());
      } else {
        return new SuccessExecuteResponse();
      }

    } catch (Exception e) {
      logger.error("Execute shell code failed, reason:", e);
      return new ErrorExecuteResponse("run shell failed", e);

    } finally {
      if (errorsReader != null) {
        inputReaderThread.onDestroy();
      }
      if (inputReaderThread != null) {
        errReaderThread.onDestroy();
      }
      IOUtils.closeQuietly(bufferedReader);
      IOUtils.closeQuietly(errorsReader);
    }
  }

  private boolean isExecutePathExist(String executePath) {
    File etlHomeDir = new File(executePath);
    return (etlHomeDir.exists() && etlHomeDir.isDirectory());
  }

  private String[] generateRunCode(String code) {
    return new String[] {"sh", "-c", code};
  }

  private String[] generateRunCodeWithArgs(String code, String[] args) {
    return new String[] {
      "sh", "-c", "echo \"dummy " + String.join(" ", args) + "\" | xargs sh -c \'" + code + "\'"
    };
  }

  @Override
  public String getId() {
    return Sender.getThisServiceInstance().getInstance() + "_" + id;
  }

  @Override
  public JobProgressInfo[] getProgressInfo(String taskID) {
    List<JobProgressInfo> jobProgressInfo = new ArrayList<>();
    if (this.engineExecutionContext == null) {
      return jobProgressInfo.toArray(new JobProgressInfo[0]);
    }

    String jobId =
        engineExecutionContext.getJobId().isDefined()
            ? engineExecutionContext.getJobId().get()
            : "";
    if (progress(taskID) == 0.0f) {
      jobProgressInfo.add(new JobProgressInfo(jobId, 1, 1, 0, 0));
    } else {
      jobProgressInfo.add(new JobProgressInfo(jobId, 1, 0, 0, 1));
    }
    return jobProgressInfo.toArray(new JobProgressInfo[0]);
  }

  @Override
  public float progress(String taskID) {
    if (this.engineExecutionContext != null) {
      return this.engineExecutionContext.getCurrentParagraph()
          / (float) this.engineExecutionContext.getTotalParagraph();
    } else {
      return 0.0f;
    }
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
  public void killTask(String taskID) {

    /*
     Kill sub-processes
    */
    int pid = getPid(process);
    GovernanceUtils.killProcess(String.valueOf(pid), "kill task " + taskID + " process", false);

    /*
     Kill yarn-applications
    */
    List<String> yarnAppIds = extractor.getExtractedYarnAppIds();
    GovernanceUtils.killYarnJobApp(yarnAppIds);
    logger.info("Finished kill yarn app ids in the engine of ({})", getId());
    super.killTask(taskID);
  }

  private int getPid(Process process) {
    try {
      Class<?> clazz = Class.forName("java.lang.UNIXProcess");
      Field field = clazz.getDeclaredField("pid");
      field.setAccessible(true);
      return field.getInt(process);
    } catch (Exception e) {
      logger.warn("Failed to acquire pid for shell process");
      return -1;
    }
  }

  @Override
  public void close() {
    try {
      process.destroy();
    } catch (Exception e) {
      logger.error("kill process " + process.toString() + " failed ", e);
    } catch (Throwable t) {
      logger.error("kill process " + process.toString() + " failed ", t);
    }
    super.close();
  }
}
