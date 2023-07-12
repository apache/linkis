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

package org.apache.linkis.engineplugin.trino.executor;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.io.resultset.ResultSetWriter;
import org.apache.linkis.common.log.LogUtils;
import org.apache.linkis.common.utils.OverloadUtils;
import org.apache.linkis.engineconn.common.conf.EngineConnConf;
import org.apache.linkis.engineconn.common.conf.EngineConnConstant;
import org.apache.linkis.engineconn.common.password.CommandPasswordCallback;
import org.apache.linkis.engineconn.common.password.StaticPasswordCallback;
import org.apache.linkis.engineconn.computation.executor.execute.ConcurrentComputationExecutor;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.engineconn.core.EngineConnObject;
import org.apache.linkis.engineplugin.trino.conf.TrinoEngineConfig;
import org.apache.linkis.engineplugin.trino.exception.TrinoClientException;
import org.apache.linkis.engineplugin.trino.exception.TrinoStateInvalidException;
import org.apache.linkis.engineplugin.trino.interceptor.PasswordInterceptor;
import org.apache.linkis.engineplugin.trino.socket.SocketChannelSocketFactory;
import org.apache.linkis.engineplugin.trino.utils.TrinoCode;
import org.apache.linkis.engineplugin.trino.utils.TrinoSQLHook;
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
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.util.CollectionUtils;

import javax.security.auth.callback.PasswordCallback;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import scala.Tuple2;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.trino.client.*;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.engineplugin.trino.conf.TrinoConfiguration.*;

public class TrinoEngineConnExecutor extends ConcurrentComputationExecutor {

  private static final Logger logger = LoggerFactory.getLogger(TrinoEngineConnExecutor.class);

  private List<Label<?>> executorLabels = new ArrayList<>(2);

  private ConcurrentHashMap<String, OkHttpClient> okHttpClientCache = new ConcurrentHashMap<>();

  private ConcurrentHashMap<String, StatementClient> statementClientCache =
      new ConcurrentHashMap<>();

  private Cache<String, ClientSession> clientSessionCache =
      CacheBuilder.newBuilder()
          .expireAfterAccess(
              Long.valueOf(EngineConnConf.ENGINE_TASK_EXPIRE_TIME().getValue().toString()),
              TimeUnit.MILLISECONDS)
          .maximumSize(EngineConnConstant.MAX_TASK_NUM())
          .build();

  private int id;

  public TrinoEngineConnExecutor(int outputPrintLimit, int id) {
    super(outputPrintLimit);
    this.id = id;
  }

  private Function<String, OkHttpClient> buildOkHttpClient =
      (user) -> {
        OkHttpClient.Builder builder =
            new OkHttpClient.Builder()
                .socketFactory(new SocketChannelSocketFactory())
                .connectTimeout(TRINO_HTTP_CONNECT_TIME_OUT.getValue(), TimeUnit.SECONDS)
                .readTimeout(TRINO_HTTP_READ_TIME_OUT.getValue(), TimeUnit.SECONDS);

        /* create password interceptor */
        String password = TRINO_PASSWORD.getValue();
        String passwordCmd = TRINO_PASSWORD_CMD.getValue();
        if (StringUtils.isNotBlank(user)) {
          PasswordCallback passwordCallback = null;
          if (StringUtils.isNotBlank(passwordCmd)) {
            passwordCallback = new CommandPasswordCallback(passwordCmd);
          } else if (StringUtils.isNotBlank(password)) {
            passwordCallback = new StaticPasswordCallback(password);
          }

          if (passwordCallback != null) {
            builder.addInterceptor(new PasswordInterceptor(user, passwordCallback));
          }
        }

        /* setup ssl */
        if (TRINO_SSL_INSECURED.getValue()) {
          OkHttpUtil.setupInsecureSsl(builder);
        } else {
          OkHttpUtil.setupSsl(
              builder,
              Optional.ofNullable(TRINO_SSL_KEYSTORE.getValue()),
              Optional.ofNullable(TRINO_SSL_KEYSTORE_PASSWORD.getValue()),
              Optional.ofNullable(TRINO_SSL_KEYSTORE_TYPE.getValue()),
              Optional.ofNullable(TRINO_SSL_TRUSTSTORE.getValue()),
              Optional.ofNullable(TRINO_SSL_TRUSTSTORE_PASSWORD.getValue()),
              Optional.ofNullable(TRINO_SSL_TRUSTSTORE_TYPE.getValue()));
        }
        return builder.build();
      };

  @Override
  public void init() {
    setCodeParser(new SQLCodeParser());
    super.init();
  }

  @Override
  public ExecuteResponse executeLine(EngineExecutionContext engineExecutorContext, String code) {
    boolean enableSqlHook = TRINO_SQL_HOOK_ENABLED.getValue();
    String realCode;
    if (StringUtils.isBlank(code)) {
      realCode = "SELECT 1";
    } else if (enableSqlHook) {
      realCode = TrinoSQLHook.preExecuteHook(code.trim());
    } else {
      realCode = code.trim();
    }

    try {
      TrinoCode.checkCode(realCode);
    } catch (ErrorException e) {
      logger.error("Execute trino code failed, reason:", e);
      return new ErrorExecuteResponse("Execute trino code failed", e);
    }

    logger.info("trino client begins to run psql code:\n {} ", realCode);

    String currentUser = getCurrentUser(engineExecutorContext.getLabels());
    String trinoUser = Optional.ofNullable(TRINO_DEFAULT_USER.getValue()).orElse(currentUser);
    String taskId = engineExecutorContext.getJobId().get();
    ClientSession clientSession = null;
    try {
      clientSession =
          clientSessionCache.get(
              taskId,
              () -> {
                UserCreatorLabel userCreatorLabel =
                    (UserCreatorLabel)
                        Arrays.stream(engineExecutorContext.getLabels())
                            .filter(label -> label instanceof UserCreatorLabel)
                            .findFirst()
                            .orElse(null);
                EngineTypeLabel engineTypeLabel =
                    (EngineTypeLabel)
                        Arrays.stream(engineExecutorContext.getLabels())
                            .filter(label -> label instanceof EngineTypeLabel)
                            .findFirst()
                            .orElse(null);
                Map<String, String> configMap = null;
                if (userCreatorLabel != null && engineTypeLabel != null) {
                  configMap =
                      new TrinoEngineConfig()
                          .getCacheMap(new Tuple2<>(userCreatorLabel, engineTypeLabel));
                }
                return getClientSession(
                    currentUser, engineExecutorContext.getProperties(), configMap);
              });
    } catch (ExecutionException e) {
      logger.error("Execute trino code failed, reason:", e);
      return new ErrorExecuteResponse("Execute trino code failed", e);
    }
    StatementClient statement =
        StatementClientFactory.newStatementClient(
            okHttpClientCache.computeIfAbsent(trinoUser, this.buildOkHttpClient),
            clientSession,
            realCode);
    statementClientCache.put(taskId, statement);
    try {
      initialStatusUpdates(taskId, engineExecutorContext, statement);
      if (statement.isRunning()
          || (statement.isFinished() && statement.finalStatusInfo().getError() == null)) {
        queryOutput(taskId, engineExecutorContext, statement);
      }
      ErrorExecuteResponse errorResponse =
          verifyServerError(taskId, engineExecutorContext, statement);
      if (errorResponse == null) {
        clientSessionCache.put(taskId, updateSession(clientSession, statement));
        return new SuccessExecuteResponse();
      } else {
        return errorResponse;
      }
    } catch (Exception e) {
      logger.error("Execute trino code failed, reason:", e);
      return new ErrorExecuteResponse("Execute trino code failed", e);
    } finally {
      statementClientCache.remove(taskId);
    }
  }

  @Override
  public ExecuteResponse executeCompletely(
      EngineExecutionContext engineExecutorContext, String code, String completedLine) {
    return null;
  }

  @Override
  public float progress(String taskID) {
    StatementClient statement = statementClientCache.get(taskID);
    if (statement != null) {
      QueryStatusInfo results = statement.currentStatusInfo();
      if (results != null) {
        StatementStats stats = results.getStats();
        if (stats != null) {
          return (float) (stats.getProgressPercentage().orElse(0.0) / 100.0);
        }
      }
    }
    return 0.0f;
  }

  @Override
  public JobProgressInfo[] getProgressInfo(String taskID) {
    StatementClient statement = statementClientCache.get(taskID);
    if (statement != null) {
      QueryStatusInfo results = statement.currentStatusInfo();
      if (results != null) {
        StatementStats stats = results.getStats();
        if (stats != null) {
          return new JobProgressInfo[] {
            new JobProgressInfo(
                taskID,
                stats.getTotalSplits(),
                stats.getRunningSplits(),
                0,
                stats.getCompletedSplits())
          };
        }
      }
    }
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
    return ENGINE_CONCURRENT_LIMIT.getValue();
  }

  private ClientSession getClientSession(
      String user, Map<String, Object> taskParams, Map<String, String> cacheMap) {
    Map<String, String> configMap = new HashMap<>();
    // override configMap with taskParams
    if (!CollectionUtils.isEmpty(cacheMap)) configMap.putAll(cacheMap);
    taskParams.forEach(
        (key, value) -> {
          if (value != null) {
            configMap.put(key, String.valueOf(value));
          }
        });
    URI httpUri = URI.create(TRINO_URL.getValue(configMap));
    String source = TRINO_SOURCE.getValue(configMap);
    String catalog = TRINO_CATALOG.getValue(configMap);
    String schema = TRINO_SCHEMA.getValue(configMap);

    Map<String, String> properties =
        configMap.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("trino.session."))
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey().substring("trino.session.".length()),
                    Map.Entry::getValue));

    String clientInfo = "Linkis";
    String transactionId = null;
    String path = null;
    Optional<String> traceToken = Optional.empty();
    Set<String> clientTags = Collections.emptySet();
    ZoneId timeZoneId = TimeZone.getDefault().toZoneId();
    Locale locale = Locale.getDefault();
    Map<String, String> resourceEstimates = Collections.emptyMap();
    Map<String, String> preparedStatements = Collections.emptyMap();
    Map<String, ClientSelectedRole> roles = Collections.emptyMap();
    Map<String, String> extraCredentials = Collections.emptyMap();
    boolean compressionDisabled = true;

    io.airlift.units.Duration clientRequestTimeout =
        new io.airlift.units.Duration(0, TimeUnit.MILLISECONDS);

    return new ClientSession(
        httpUri,
        user,
        Optional.of(user),
        source,
        traceToken,
        clientTags,
        clientInfo,
        catalog,
        schema,
        path,
        timeZoneId,
        locale,
        resourceEstimates,
        properties,
        preparedStatements,
        roles,
        extraCredentials,
        transactionId,
        clientRequestTimeout,
        compressionDisabled);
  }

  private String getCurrentUser(Label<?>[] labels) {
    return Arrays.stream(labels)
        .filter(label -> label instanceof UserCreatorLabel)
        .map(label -> ((UserCreatorLabel) label).getUser())
        .findFirst()
        .orElse(TRINO_DEFAULT_USER.getValue());
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
      String taskId, EngineExecutionContext engineExecutorContext, StatementClient statement)
      throws IOException {
    int columnCount = 0;
    int rows = 0;
    ResultSetWriter resultSetWriter =
        engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE);
    try {
      QueryStatusInfo results = null;
      if (statement.isRunning()) {
        results = statement.currentStatusInfo();
      } else {
        results = statement.finalStatusInfo();
      }
      if (results.getColumns() == null) {
        throw new RuntimeException("Trino columns is null.");
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
      throw e;
    }
    String message = String.format("Fetched %d col(s) : %d row(s) in Trino", columnCount, rows);
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
        logger.error("Trino execute failed (#{}): {}", info.getId(), error.getMessage());
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
      logger.warn("Trino statement is killed.");
      return null;
    } else if (statement.isClientError()) {
      throw new TrinoClientException("trino client error.");
    } else {
      throw new TrinoStateInvalidException("trino status error. Statement is not finished.");
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
      Map<String, ClientSelectedRole> roles = new HashMap<>(newSession.getRoles());
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
