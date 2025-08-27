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

package org.apache.linkis.engineplugin.presto.executor;

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
import org.apache.linkis.engineplugin.presto.conf.PrestoConfiguration;
import org.apache.linkis.engineplugin.presto.conf.PrestoEngineConf;
import org.apache.linkis.engineplugin.presto.errorcode.PrestoErrorCodeSummary;
import org.apache.linkis.engineplugin.presto.exception.PrestoClientException;
import org.apache.linkis.engineplugin.presto.exception.PrestoStateInvalidException;
import org.apache.linkis.engineplugin.presto.utils.PrestoSQLHook;
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
import org.apache.linkis.storage.resultset.ResultSetFactory$;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import scala.Tuple2;

import com.facebook.presto.client.*;
import com.facebook.presto.spi.security.SelectedRole;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrestoEngineConnExecutor extends ConcurrentComputationExecutor {

  private static final Logger logger = LoggerFactory.getLogger(PrestoEngineConnExecutor.class);

  private static OkHttpClient okHttpClient =
      new OkHttpClient.Builder()
          .socketFactory(new SocketChannelSocketFactory())
          .connectTimeout(
              PrestoConfiguration.PRESTO_HTTP_CONNECT_TIME_OUT.getValue(), TimeUnit.SECONDS)
          .readTimeout(PrestoConfiguration.PRESTO_HTTP_READ_TIME_OUT.getValue(), TimeUnit.SECONDS)
          .build();
  private int id;
  private List<Label<?>> executorLabels = new ArrayList<>(2);
  private Map<String, StatementClient> statementClientCache = new ConcurrentHashMap<>();
  private Cache<String, ClientSession> clientSessionCache =
      CacheBuilder.newBuilder()
          .expireAfterAccess(
              Long.valueOf(EngineConnConf.ENGINE_TASK_EXPIRE_TIME().getValue().toString()),
              TimeUnit.MILLISECONDS)
          .maximumSize(EngineConnConstant.MAX_TASK_NUM())
          .build();

  public PrestoEngineConnExecutor(int outputPrintLimit, int id) {
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
    String user = getUserCreatorLabel(engineConnTask.getLables()).getUser();
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
          new PrestoEngineConf().getCacheMap(new Tuple2<>(userCreatorLabel, engineTypeLabel));
    }

    clientSessionCache.put(
        engineConnTask.getTaskId(),
        getClientSession(user, engineConnTask.getProperties(), configMap));
    return super.execute(engineConnTask);
  }

  @Override
  public ExecuteResponse executeLine(EngineExecutionContext engineExecutorContext, String code) {
    boolean enableSqlHook = PrestoConfiguration.PRESTO_SQL_HOOK_ENABLED.getValue();
    String realCode;
    if (StringUtils.isBlank(code)) {
      realCode = "SELECT 1";
    } else if (enableSqlHook) {
      realCode = PrestoSQLHook.preExecuteHook(code.trim());
    } else {
      realCode = code.trim();
    }
    logger.info("presto client begins to run psql code:\n {}", realCode);

    String taskId = engineExecutorContext.getJobId().get();
    ClientSession clientSession = clientSessionCache.getIfPresent(taskId);
    StatementClient statement =
        StatementClientFactory.newStatementClient(okHttpClient, clientSession, realCode);
    statementClientCache.put(taskId, statement);

    try {
      initialStatusUpdates(taskId, engineExecutorContext, statement);
      if (statement.isRunning()
          || (statement.isFinished() && statement.finalStatusInfo().getError() == null)) {
        queryOutput(taskId, engineExecutorContext, statement);
      }
      ErrorExecuteResponse errorResponse = null;
      try {
        errorResponse = verifyServerError(taskId, engineExecutorContext, statement);
      } catch (ErrorException e) {
        logger.error("Presto execute failed (#{}): {}", e.getErrCode(), e.getMessage());
      }
      if (errorResponse == null) {
        // update session
        clientSessionCache.put(taskId, updateSession(clientSession, statement));
        return new SuccessExecuteResponse();
      } else {
        return errorResponse;
      }
    } finally {
      statementClientCache.remove(taskId);
    }
  }

  @Override
  public ExecuteResponse executeCompletely(
      EngineExecutionContext engineExecutorContext, String code, String completedLine) {
    return null;
  }

  // todo
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
    StatementClient statement = statementClientCache.remove(taskId);
    if (null != statement) {
      statement.cancelLeafStage();
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
    return PrestoConfiguration.ENGINE_CONCURRENT_LIMIT.getValue();
  }

  private ClientSession getClientSession(
      String user, Map<String, Object> taskParams, Map<String, String> cacheMap) {
    Map<String, String> configMap = new HashMap<>();
    // The parameter priority specified at runtime is higher than the configuration priority of the
    // management console
    if (!CollectionUtils.isEmpty(cacheMap)) {
      configMap.putAll(cacheMap);
    }
    taskParams.entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        .forEach(entry -> configMap.put(entry.getKey(), String.valueOf(entry.getValue())));

    URI httpUri = URI.create(PrestoConfiguration.PRESTO_URL.getValue(configMap));
    String source = PrestoConfiguration.PRESTO_SOURCE.getValue(configMap);
    String catalog = PrestoConfiguration.PRESTO_CATALOG.getValue(configMap);
    String schema = PrestoConfiguration.PRESTO_SCHEMA.getValue(configMap);

    Map<String, String> properties =
        configMap.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("presto.session."))
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey().substring("presto.session.".length()),
                    Map.Entry::getValue));

    String clientInfo = "Linkis";
    String transactionId = null;
    Optional<String> traceToken = Optional.empty();
    Set<String> clientTags = Collections.emptySet();
    String timeZonId = TimeZone.getDefault().getID();
    Locale locale = Locale.getDefault();
    Map<String, String> resourceEstimates = Collections.emptyMap();
    Map<String, String> preparedStatements = Collections.emptyMap();
    Map<String, SelectedRole> roles = Collections.emptyMap();
    Map<String, String> extraCredentials = Collections.emptyMap();
    io.airlift.units.Duration clientRequestTimeout =
        new io.airlift.units.Duration(0, TimeUnit.MILLISECONDS);

    return new ClientSession(
        httpUri,
        user,
        source,
        traceToken,
        clientTags,
        clientInfo,
        catalog,
        schema,
        timeZonId,
        locale,
        resourceEstimates,
        properties,
        preparedStatements,
        roles,
        extraCredentials,
        transactionId,
        clientRequestTimeout);
  }

  private UserCreatorLabel getUserCreatorLabel(Label<?>[] labels) {
    return (UserCreatorLabel)
        Arrays.stream(labels).filter(label -> label instanceof UserCreatorLabel).findFirst().get();
  }

  private void initialStatusUpdates(
      String taskId, EngineExecutionContext engineExecutorContext, StatementClient statement) {
    while (statement.isRunning()
        && (statement.currentData().getData() == null
            || statement.currentStatusInfo().getUpdateType() != null)) {
      engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId));
      statement.advance();
    }
  }

  private void queryOutput(
      String taskId, EngineExecutionContext engineExecutorContext, StatementClient statement) {
    int columnCount = 0;
    int rows = 0;
    ResultSetWriter resultSetWriter =
        engineExecutorContext.createResultSetWriter(ResultSetFactory$.MODULE$.TABLE_TYPE());
    try {
      QueryStatusInfo results = null;
      if (statement.isRunning()) {
        results = statement.currentStatusInfo();
      } else {
        results = statement.finalStatusInfo();
      }
      if (results.getColumns() == null) {
        throw new RuntimeException("presto columns is null.");
      }
      List<Column> columns =
          results.getColumns().stream()
              .map(
                  column -> new Column(column.getName(), DataType.toDataType(column.getType()), ""))
              .collect(Collectors.toList());
      columnCount = columns.size();
      resultSetWriter.addMetaData(new TableMetaData(columns.toArray(new Column[0])));
      while (statement.isRunning()) {
        Iterable<List<Object>> data = statement.currentData().getData();
        if (data != null) {
          for (List<Object> row : data) {
            String[] rowArray = row.stream().map(r -> String.valueOf(r)).toArray(String[]::new);
            resultSetWriter.addRecord(new TableRecord(rowArray));
            rows += 1;
          }
        }
        engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId));
        statement.advance();
      }
    } catch (Exception e) {
      IOUtils.closeQuietly(resultSetWriter);
    }
    String message = String.format("Fetched %d col(s) : %d row(s) in presto", columnCount, rows);
    logger.info(message);
    engineExecutorContext.appendStdout(LogUtils.generateInfo(message));
    engineExecutorContext.sendResultSet(resultSetWriter);
  }

  private ErrorExecuteResponse verifyServerError(
      String taskId, EngineExecutionContext engineExecutorContext, StatementClient statement)
      throws ErrorException {
    engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId));
    if (statement.isFinished()) {
      QueryStatusInfo info = statement.finalStatusInfo();
      if (info.getError() != null) {
        QueryError error = Objects.requireNonNull(info.getError());
        logger.error("Presto execute failed (#{}): {}", info.getId(), error.getMessage());
        Throwable cause = null;
        if (error.getFailureInfo() != null) {
          cause = error.getFailureInfo().toException();
        }
        engineExecutorContext.appendStdout(
            LogUtils.generateERROR(ExceptionUtils.getStackTrace(cause)));
        return new ErrorExecuteResponse(ExceptionUtils.getMessage(cause), cause);
      } else {
        return null;
      }
    } else if (statement.isClientAborted()) {
      logger.warn("Presto statement is killed.");
      return null;
    } else if (statement.isClientError()) {
      throw new PrestoClientException(
          PrestoErrorCodeSummary.PRESTO_CLIENT_ERROR.getErrorCode(),
          PrestoErrorCodeSummary.PRESTO_CLIENT_ERROR.getErrorDesc());
    } else {
      throw new PrestoStateInvalidException(
          PrestoErrorCodeSummary.PRESTO_STATE_INVALID.getErrorCode(),
          PrestoErrorCodeSummary.PRESTO_STATE_INVALID.getErrorDesc());
    }
  }

  private ClientSession updateSession(ClientSession clientSession, StatementClient statement) {
    ClientSession newSession = clientSession;

    // update catalog and schema if present
    if (statement.getSetCatalog().isPresent() || statement.getSetSchema().isPresent()) {
      newSession =
          ClientSession.builder(newSession)
              .withCatalog(statement.getSetCatalog().orElse(newSession.getCatalog()))
              .withSchema(statement.getSetSchema().orElse(newSession.getSchema()))
              .build();
    }

    // update transaction ID if necessary
    if (statement.isClearTransactionId()) {
      newSession = ClientSession.stripTransactionId(newSession);
    }

    ClientSession.Builder builder = ClientSession.builder(newSession);

    if (statement.getStartedTransactionId() != null) {
      builder = builder.withTransactionId(statement.getStartedTransactionId());
    }

    // update session properties if present
    if (!statement.getSetSessionProperties().isEmpty()
        || !statement.getResetSessionProperties().isEmpty()) {
      Map<String, String> sessionProperties = new HashMap<>(newSession.getProperties());
      sessionProperties.putAll(statement.getSetSessionProperties());
      sessionProperties.keySet().removeAll(statement.getResetSessionProperties());
      builder = builder.withProperties(sessionProperties);
    }

    // update session roles
    if (!statement.getSetRoles().isEmpty()) {
      Map<String, SelectedRole> roles = new HashMap<>(newSession.getRoles());
      roles.putAll(statement.getSetRoles());
      builder = builder.withRoles(roles);
    }

    // update prepared statements if present
    if (!statement.getAddedPreparedStatements().isEmpty()
        || !statement.getDeallocatedPreparedStatements().isEmpty()) {
      Map<String, String> preparedStatements = new HashMap<>(newSession.getPreparedStatements());
      preparedStatements.putAll(statement.getAddedPreparedStatements());
      preparedStatements.keySet().removeAll(statement.getDeallocatedPreparedStatements());
      builder = builder.withPreparedStatements(preparedStatements);
    }

    return builder.build();
  }

  @Override
  public void killAll() {
    Iterator<StatementClient> iterator = statementClientCache.values().iterator();
    while (iterator.hasNext()) {
      StatementClient statement = iterator.next();
      if (statement != null) {
        statement.cancelLeafStage();
      }
    }
    statementClientCache.clear();
  }

  @Override
  public void close() {
    killAll();
    super.close();
  }
}
