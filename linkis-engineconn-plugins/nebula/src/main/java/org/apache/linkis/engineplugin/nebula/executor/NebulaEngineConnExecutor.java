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

package org.apache.linkis.engineplugin.nebula.executor;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.io.resultset.ResultSetWriter;
import org.apache.linkis.common.log.LogUtils;
import org.apache.linkis.common.utils.OverloadUtils;
import org.apache.linkis.engineconn.common.conf.EngineConnConf;
import org.apache.linkis.engineconn.common.conf.EngineConnConstant;
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask;
import org.apache.linkis.engineconn.computation.executor.execute.ConcurrentComputationExecutor;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.engineconn.core.EngineConnObject;
import org.apache.linkis.engineplugin.nebula.conf.NebulaConfiguration;
import org.apache.linkis.engineplugin.nebula.conf.NebulaEngineConf;
import org.apache.linkis.engineplugin.nebula.errorcode.NebulaErrorCodeSummary;
import org.apache.linkis.engineplugin.nebula.exception.NebulaClientException;
import org.apache.linkis.engineplugin.nebula.exception.NebulaExecuteError;
import org.apache.linkis.governance.common.paser.SQLCodeParser;
import org.apache.linkis.manager.common.entity.resource.CommonNodeResource;
import org.apache.linkis.manager.common.entity.resource.LoadResource;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse;
import org.apache.linkis.scheduler.executer.ExecuteResponse;
import org.apache.linkis.scheduler.executer.SuccessExecuteResponse;
import org.apache.linkis.storage.domain.Column;
import org.apache.linkis.storage.domain.DataType;
import org.apache.linkis.storage.resultset.ResultSetFactory;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import scala.Tuple2;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vesoft.nebula.ErrorCode;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NebulaEngineConnExecutor extends ConcurrentComputationExecutor {

  private static final Logger logger = LoggerFactory.getLogger(NebulaEngineConnExecutor.class);
  private int id;
  private List<Label<?>> executorLabels = new ArrayList<>(2);
  private Map<String, Session> sessionCache = new ConcurrentHashMap<>();

  private Map<String, String> configMap = new HashMap<>();

  private Cache<String, NebulaPool> nebulaPoolCache =
      CacheBuilder.newBuilder()
          .expireAfterAccess(
              Long.valueOf(EngineConnConf.ENGINE_TASK_EXPIRE_TIME().getValue().toString()),
              TimeUnit.MILLISECONDS)
          .maximumSize(EngineConnConstant.MAX_TASK_NUM())
          .build();

  public NebulaEngineConnExecutor(int outputPrintLimit, int id) {
    super(outputPrintLimit);
    this.id = id;
  }

  @Override
  public void init() {
    setCodeParser(new SQLCodeParser());
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

    Map<String, String> configMap = null;
    if (userCreatorLabelOp.isPresent() && engineTypeLabelOp.isPresent()) {
      UserCreatorLabel userCreatorLabel = (UserCreatorLabel) userCreatorLabelOp.get();
      EngineTypeLabel engineTypeLabel = (EngineTypeLabel) engineTypeLabelOp.get();

      configMap =
          new NebulaEngineConf().getCacheMap(new Tuple2<>(userCreatorLabel, engineTypeLabel));
    }

    nebulaPoolCache.put(
        engineConnTask.getTaskId(), getNebulaPool(engineConnTask.getProperties(), configMap));
    return super.execute(engineConnTask);
  }

  @Override
  public ExecuteResponse executeLine(EngineExecutionContext engineExecutorContext, String code) {
    String realCode;
    if (StringUtils.isBlank(code)) {
      realCode = "SHOW SPACES";
    } else {
      realCode = code.trim();
    }
    logger.info("Nebula client begins to run ngql code:\n {}", realCode);

    String taskId = engineExecutorContext.getJobId().get();
    NebulaPool nebulaPool = nebulaPoolCache.getIfPresent(taskId);
    Session session = getSession(taskId, nebulaPool);

    initialStatusUpdates(taskId, engineExecutorContext, session);
    ResultSet resultSet = null;

    try {
      resultSet = session.execute(code);
    } catch (Exception e) {
      logger.error("Nebula executor error.");
      throw new NebulaExecuteError(
          NebulaErrorCodeSummary.NEBULA_EXECUTOR_ERROR.getErrorCode(),
          NebulaErrorCodeSummary.NEBULA_EXECUTOR_ERROR.getErrorDesc());
    }

    if (resultSet.isSucceeded() && !resultSet.isEmpty()) {
      queryOutput(taskId, engineExecutorContext, resultSet);
    }
    ErrorExecuteResponse errorResponse = null;
    try {
      errorResponse = verifyServerError(taskId, engineExecutorContext, resultSet);
    } catch (ErrorException e) {
      logger.error("Nebula execute failed (#{}): {}", e.getErrCode(), e.getMessage());
    }
    if (errorResponse == null) {
      return new SuccessExecuteResponse();
    } else {
      return errorResponse;
    }
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
    Session session = sessionCache.remove(taskId);
    if (null != session) {
      session.release();
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
    return NebulaConfiguration.ENGINE_CONCURRENT_LIMIT.getValue();
  }

  private NebulaPool getNebulaPool(Map<String, Object> taskParams, Map<String, String> cacheMap) {
    if (!CollectionUtils.isEmpty(cacheMap)) {
      configMap.putAll(cacheMap);
    }
    taskParams.entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        .forEach(entry -> configMap.put(entry.getKey(), String.valueOf(entry.getValue())));

    String host = NebulaConfiguration.NEBULA_HOST.getValue(configMap);
    Integer port = NebulaConfiguration.NEBULA_PORT.getValue(configMap);
    Integer maxConnSize = NebulaConfiguration.NEBULA_MAX_CONN_SIZE.getValue(configMap);

    NebulaPool nebulaPool = new NebulaPool();
    Boolean initResult = false;
    try {

      NebulaPoolConfig nebulaPoolConfig = new NebulaPoolConfig();
      nebulaPoolConfig.setMaxConnSize(maxConnSize);
      List<HostAddress> addresses = Arrays.asList(new HostAddress(host, port));
      initResult = nebulaPool.init(addresses, nebulaPoolConfig);
    } catch (Exception e) {
      logger.error("NebulaPool initialization failed.");
      throw new NebulaClientException(
          NebulaErrorCodeSummary.NEBULA_CLIENT_INITIALIZATION_FAILED.getErrorCode(),
          NebulaErrorCodeSummary.NEBULA_CLIENT_INITIALIZATION_FAILED.getErrorDesc());
    }
    if (!initResult) {
      logger.error("NebulaPool initialization failed.");
      throw new NebulaClientException(
          NebulaErrorCodeSummary.NEBULA_CLIENT_INITIALIZATION_FAILED.getErrorCode(),
          NebulaErrorCodeSummary.NEBULA_CLIENT_INITIALIZATION_FAILED.getErrorDesc());
    }
    return nebulaPool;
  }

  private Session getSession(String taskId, NebulaPool nebulaPool) {
    if (sessionCache.containsKey(taskId)
        && sessionCache.get(taskId) != null
        && sessionCache.get(taskId).ping()) {
      return sessionCache.get(taskId);
    } else {
      Session session;
      String username = NebulaConfiguration.NEBULA_USER_NAME.getValue(configMap);
      String password = NebulaConfiguration.NEBULA_PASSWORD.getValue(configMap);
      Boolean reconnect = NebulaConfiguration.NEBULA_RECONNECT_ENABLED.getValue(configMap);

      try {
        session = nebulaPool.getSession(username, password, reconnect);
      } catch (Exception e) {
        logger.error("Nebula Session initialization failed.");
        throw new NebulaClientException(
            NebulaErrorCodeSummary.NEBULA_CLIENT_INITIALIZATION_FAILED.getErrorCode(),
            NebulaErrorCodeSummary.NEBULA_CLIENT_INITIALIZATION_FAILED.getErrorDesc());
      }

      sessionCache.put(taskId, session);
      return session;
    }
  }

  private void initialStatusUpdates(
      String taskId, EngineExecutionContext engineExecutorContext, Session session) {
    if (session.ping()) {
      engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId));
    }
  }

  private void queryOutput(
      String taskId, EngineExecutionContext engineExecutorContext, ResultSet resultSet) {
    int columnCount = 0;
    ResultSetWriter resultSetWriter =
        engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE);

    try {
      List<String> colNames = resultSet.keys();

      if (CollectionUtils.isEmpty(colNames)) {
        throw new RuntimeException("Nebula columns is null.");
      }

      List<Column> columns =
          colNames.stream()
              .map(column -> new Column(column, DataType.toDataType("string"), ""))
              .collect(Collectors.toList());
      columnCount = columns.size();
      resultSetWriter.addMetaData(new TableMetaData(columns.toArray(new Column[0])));
      if (!resultSet.isEmpty()) {
        for (int i = 0; i < resultSet.rowsSize(); i++) {
          ResultSet.Record record = resultSet.rowValues(i);
          if (record != null) {
            String[] rowArray =
                record.values().stream()
                    .map(
                        x -> {
                          try {
                            return x.toString();
                          } catch (Exception e) {
                            return "";
                          }
                        })
                    .toArray(String[]::new);
            resultSetWriter.addRecord(new TableRecord(rowArray));
          }
        }
        engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId));
      }
    } catch (Exception e) {
      IOUtils.closeQuietly(resultSetWriter);
    }
    String message =
        String.format("Fetched %d col(s) : %d row(s) in Nebula", columnCount, resultSet.rowsSize());
    logger.info(message);
    engineExecutorContext.appendStdout(LogUtils.generateInfo(message));
    engineExecutorContext.sendResultSet(resultSetWriter);
  }

  private ErrorExecuteResponse verifyServerError(
      String taskId, EngineExecutionContext engineExecutorContext, ResultSet resultSet)
      throws ErrorException {
    engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId));

    if (!resultSet.isSucceeded() || resultSet.getErrorCode() != ErrorCode.SUCCEEDED.getValue()) {
      logger.error(
          "Nebula execute failed (#{}): {}", resultSet.getErrorCode(), resultSet.getErrorMessage());
      engineExecutorContext.appendStdout(LogUtils.generateERROR(resultSet.getErrorMessage()));
      return new ErrorExecuteResponse(resultSet.getErrorMessage(), null);
    }
    return null;
  }

  @Override
  public void killAll() {
    Iterator<Session> iterator = sessionCache.values().iterator();
    while (iterator.hasNext()) {
      Session session = iterator.next();
      if (session != null) {
        session.release();
      }
    }
    sessionCache.clear();
  }

  @Override
  public void close() {
    killAll();
    super.close();
  }
}
