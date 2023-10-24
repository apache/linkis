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

package org.apache.linkis.engineplugin.repl.executor;

import org.apache.linkis.common.log.LogUtils;
import org.apache.linkis.common.utils.OverloadUtils;
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask;
import org.apache.linkis.engineconn.computation.executor.execute.ConcurrentComputationExecutor;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.engineconn.core.EngineConnObject;
import org.apache.linkis.engineplugin.repl.conf.ReplConfiguration;
import org.apache.linkis.engineplugin.repl.conf.ReplEngineConf;
import org.apache.linkis.engineplugin.repl.errorcode.ReplErrorCodeSummary;
import org.apache.linkis.engineplugin.repl.exception.ReplException;
import org.apache.linkis.manager.common.entity.resource.CommonNodeResource;
import org.apache.linkis.manager.common.entity.resource.LoadResource;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.executer.ExecuteResponse;
import org.apache.linkis.scheduler.executer.SuccessExecuteResponse;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import scala.Tuple2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplEngineConnExecutor extends ConcurrentComputationExecutor {

  private static final Logger logger = LoggerFactory.getLogger(ReplEngineConnExecutor.class);
  private int id;

  private ReplAdapter replAdapter;
  private List<Label<?>> executorLabels = new ArrayList<>(2);
  private Map<String, Thread> threadCache = new ConcurrentHashMap<>();

  private Map<String, String> configMap = new HashMap<>();

  public ReplEngineConnExecutor(int outputPrintLimit, int id) {
    super(outputPrintLimit);
    this.id = id;
  }

  @Override
  public void init() {
    super.init();
  }

  @Override
  public ExecuteResponse execute(EngineConnTask engineConnTask) {
    Optional<Label<?>> userCreatorLabelOp =
        Arrays.stream(engineConnTask.getLables())
            .filter(label -> label instanceof UserCreatorLabel)
            .findFirst();
    Optional<Label<?>> engineTypeLabelOp =
        Arrays.stream(engineConnTask.getLables())
            .filter(label -> label instanceof EngineTypeLabel)
            .findFirst();

    if (userCreatorLabelOp.isPresent() && engineTypeLabelOp.isPresent()) {
      UserCreatorLabel userCreatorLabel = (UserCreatorLabel) userCreatorLabelOp.get();
      EngineTypeLabel engineTypeLabel = (EngineTypeLabel) engineTypeLabelOp.get();

      Map<String, String> cacheMap =
          new ReplEngineConf().getCacheMap(new Tuple2<>(userCreatorLabel, engineTypeLabel));
      if (MapUtils.isNotEmpty(cacheMap)) {
        configMap.putAll(cacheMap);
      }
    }

    Map<String, Object> taskParams = engineConnTask.getProperties();

    if (MapUtils.isNotEmpty(taskParams)) {
      taskParams.entrySet().stream()
          .filter(entry -> entry.getValue() != null)
          .forEach(entry -> configMap.put(entry.getKey(), String.valueOf(entry.getValue())));
    }

    String replType = ReplConfiguration.REPL_TYPE.getValue(configMap);

    replAdapter = ReplAdapterFactory.create(replType);
    return super.execute(engineConnTask);
  }

  @Override
  public ExecuteResponse executeLine(EngineExecutionContext engineExecutorContext, String code) {
    String realCode;
    if (StringUtils.isBlank(code)) {
      throw new ReplException(
          ReplErrorCodeSummary.REPL_CODE_IS_NOT_BLANK.getErrorCode(),
          ReplErrorCodeSummary.REPL_CODE_IS_NOT_BLANK.getErrorDesc());
    } else {
      realCode = code.trim();
    }
    logger.info("Repl engine begins to run code:\n {}", realCode);

    String taskId = engineExecutorContext.getJobId().get();

    initialStatusUpdates(taskId, engineExecutorContext);

    String classpathDir = ReplConfiguration.CLASSPATH_DIR.getValue(configMap);

    String methodName = ReplConfiguration.METHOD_NAME.getValue(configMap);

    threadCache.put(taskId, Thread.currentThread());

    try {
      replAdapter.executorCode(realCode, classpathDir, methodName);
    } catch (Exception e) {
      String errorMessage = ExceptionUtils.getStackTrace(e);
      logger.error("Repl engine execute failed : {}", errorMessage);
      engineExecutorContext.appendStdout(LogUtils.generateERROR(errorMessage));
    }

    return new SuccessExecuteResponse();
  }

  @Override
  public ExecuteResponse executeCompletely(
      EngineExecutionContext engineExecutorContext, String code, String completedLine) {
    return null;
  }

  @Override
  public float progress(String taskID) {
    return 0.0f;
  }

  @Override
  public JobProgressInfo[] getProgressInfo(String taskID) {
    return new JobProgressInfo[0];
  }

  @Override
  public void killTask(String taskId) {
    Thread thread = threadCache.remove(taskId);
    if (null != thread) {
      thread.interrupt();
    }
    super.killTask(taskId);
  }

  @Override
  public List<Label<?>> getExecutorLabels() {
    return executorLabels;
  }

  @Override
  public void setExecutorLabels(List<Label<?>> labels) {
    if (!CollectionUtils.isEmpty(labels)) {
      executorLabels.clear();
      executorLabels.addAll(labels);
    }
  }

  @Override
  public boolean supportCallBackLogs() {
    return false;
  }

  @Override
  public NodeResource requestExpectedResource(NodeResource expectedResource) {
    return null;
  }

  @Override
  public NodeResource getCurrentNodeResource() {
    NodeResourceUtils.appendMemoryUnitIfMissing(
        EngineConnObject.getEngineCreationContext().getOptions());

    CommonNodeResource resource = new CommonNodeResource();
    LoadResource usedResource = new LoadResource(OverloadUtils.getProcessMaxMemory(), 1);
    resource.setUsedResource(usedResource);
    return resource;
  }

  @Override
  public String getId() {
    return Sender.getThisServiceInstance().getInstance() + "_" + id;
  }

  @Override
  public int getConcurrentLimit() {
    return ReplConfiguration.ENGINE_CONCURRENT_LIMIT.getValue();
  }

  private void initialStatusUpdates(String taskId, EngineExecutionContext engineExecutorContext) {
    engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId));
  }

  @Override
  public void killAll() {
    Iterator<Thread> iterator = threadCache.values().iterator();
    while (iterator.hasNext()) {
      Thread thread = iterator.next();
      if (thread != null) {
        thread.interrupt();
      }
    }
    threadCache.clear();
  }

  @Override
  public void close() {
    killAll();
    super.close();
  }
}
