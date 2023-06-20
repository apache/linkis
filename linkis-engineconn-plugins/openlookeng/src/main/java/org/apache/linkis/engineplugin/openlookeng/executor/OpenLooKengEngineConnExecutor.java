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

package org.apache.linkis.engineplugin.openlookeng.executor;

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSetWriter;
import org.apache.linkis.common.log.LogUtils;
import org.apache.linkis.engineconn.common.conf.EngineConnConf;
import org.apache.linkis.engineconn.common.conf.EngineConnConstant;
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask;
import org.apache.linkis.engineconn.computation.executor.execute.ConcurrentComputationExecutor;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.engineconn.core.EngineConnObject;
import org.apache.linkis.engineplugin.openlookeng.conf.OpenLooKengConfiguration;
import org.apache.linkis.engineplugin.openlookeng.conf.OpenLooKengEngineConfCache;
import org.apache.linkis.engineplugin.openlookeng.exception.OpenLooKengClientException;
import org.apache.linkis.engineplugin.openlookeng.exception.OpenLooKengStateInvalidException;
import org.apache.linkis.governance.common.paser.SQLCodeParser;
import org.apache.linkis.manager.common.entity.resource.CommonNodeResource;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.manager.label.utils.LabelUtil;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse;
import org.apache.linkis.scheduler.executer.ExecuteResponse;
import org.apache.linkis.scheduler.executer.SuccessExecuteResponse;
import org.apache.linkis.storage.domain.DataType;
import org.apache.linkis.storage.resultset.ResultSetFactory;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.prestosql.client.ClientSelectedRole;
import io.prestosql.client.ClientSession;
import io.prestosql.client.QueryError;
import io.prestosql.client.QueryStatusInfo;
import io.prestosql.client.SocketChannelSocketFactory;
import io.prestosql.client.StatementClient;
import io.prestosql.client.StatementClientFactory;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.engineplugin.openlookeng.conf.OpenLooKengConfiguration.OPENLOOKENG_HTTP_CONNECT_TIME_OUT;
import static org.apache.linkis.engineplugin.openlookeng.conf.OpenLooKengConfiguration.OPENLOOKENG_HTTP_READ_TIME_OUT;
import static org.apache.linkis.engineplugin.openlookeng.errorcode.OpenLooKengErrorCodeSummary.OPENLOOKENG_CLIENT_ERROR;
import static org.apache.linkis.engineplugin.openlookeng.errorcode.OpenLooKengErrorCodeSummary.OPENLOOKENG_STATUS_ERROR;

public class OpenLooKengEngineConnExecutor extends ConcurrentComputationExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(OpenLooKengEngineConnExecutor.class);

  private int id;

  private OkHttpClient okHttpClient =
      new OkHttpClient.Builder()
          .socketFactory(new SocketChannelSocketFactory())
          .connectTimeout(OPENLOOKENG_HTTP_CONNECT_TIME_OUT.getValue(), TimeUnit.SECONDS)
          .readTimeout(OPENLOOKENG_HTTP_READ_TIME_OUT.getValue(), TimeUnit.SECONDS)
          .build();

  private List<Label<?>> executorLabels = new ArrayList<Label<?>>();

  private Cache<String, ClientSession> clientSessionCache =
      CacheBuilder.newBuilder()
          .expireAfterAccess(
              Long.valueOf(EngineConnConf.ENGINE_TASK_EXPIRE_TIME().getValue().toString()),
              TimeUnit.MILLISECONDS)
          .maximumSize(EngineConnConstant.MAX_TASK_NUM())
          .build();

  public OpenLooKengEngineConnExecutor(int outputPrintLimit, int id) {
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
    List<Label<?>> labelList = Arrays.asList(engineConnTask.getLables());
    UserCreatorLabel userCreatorLabel = LabelUtil.getUserCreatorLabel(labelList);
    String user = userCreatorLabel.getUser();
    EngineTypeLabel engineTypeLabel = LabelUtil.getEngineTypeLabel(labelList);
    clientSessionCache.put(
        engineConnTask.getTaskId(),
        getClientSession(
            user,
            engineConnTask.getProperties(),
            OpenLooKengEngineConfCache.getConfMap(userCreatorLabel, engineTypeLabel)));
    return super.execute(engineConnTask);
  }

  @Override
  public ExecuteResponse executeLine(EngineExecutionContext engineExecutorContext, String code) {

    String taskId = engineExecutorContext.getJobId().get();

    ClientSession clientSession = clientSessionCache.getIfPresent(taskId);
    StatementClient statement =
        StatementClientFactory.newStatementClient(okHttpClient, clientSession, code);

    initialStatusUpdates(taskId, engineExecutorContext, statement);

    try {
      if (statement.isRunning()
          || (statement.isFinished() && statement.finalStatusInfo().getError() == null)) {
        queryOutput(taskId, engineExecutorContext, statement);
      }

      ErrorExecuteResponse errorResponse =
          verifyServerError(taskId, engineExecutorContext, statement);
      if (errorResponse == null) {
        // update session
        clientSessionCache.put(taskId, updateSession(clientSession, statement));
        return new SuccessExecuteResponse();
      } else {
        return errorResponse;
      }
    } catch (Exception e) {
      return new ErrorExecuteResponse(e.getMessage(), e);
    }
  }

  @Override
  public ExecuteResponse executeCompletely(
      EngineExecutionContext engineExecutorContext, String code, String completedLine) {
    return null;
  }

  @Override
  public float progress(String taskID) {
    return 0;
  }

  @Override
  public JobProgressInfo[] getProgressInfo(String taskID) {
    return new JobProgressInfo[0];
  }

  @Override
  public boolean supportCallBackLogs() {
    return false;
  }

  @Override
  public String getId() {
    return Sender.getThisServiceInstance().getInstance() + this.id;
  }

  @Override
  public int getConcurrentLimit() {
    return OpenLooKengConfiguration.OPENLOOKENG_CONCURRENT_LIMIT.getValue();
  }

  @Override
  public void killAll() {}

  @Override
  public List<Label<?>> getExecutorLabels() {
    return executorLabels;
  }

  @Override
  public void setExecutorLabels(List<Label<?>> labels) {
    if (null != labels && !labels.isEmpty()) {
      executorLabels.clear();
      executorLabels.addAll(labels);
    }
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

  private ClientSession getClientSession(
      String user, Map<String, Object> taskParams, Map<String, String> cacheMap) {
    Map<String, String> configMap = new HashMap<>();
    if (null != cacheMap && !cacheMap.isEmpty()) {
      configMap.putAll(cacheMap);
    }

    for (Map.Entry<String, Object> keyValue : taskParams.entrySet()) {
      configMap.put(keyValue.getKey(), String.valueOf(keyValue.getValue()));
    }

    URI httpUri = URI.create(OpenLooKengConfiguration.OPENLOOKENG_URL.getValue(configMap));
    String source = OpenLooKengConfiguration.OPENLOOKENG_SOURCE.getValue(configMap);
    String catalog = OpenLooKengConfiguration.OPENLOOKENG_CATALOG.getValue(configMap);
    String schema = OpenLooKengConfiguration.OPENLOOKENG_SCHEMA.getValue(configMap);

    Map<String, String> properties = new HashMap<>();

    for (Map.Entry<String, String> keyValue : configMap.entrySet()) {
      if (keyValue.getKey().startsWith("presto.session.")) {
        properties.put(
            keyValue.getKey().substring("presto.session.".length()), keyValue.getValue());
      }
    }

    String clientInfo = "Linkis";
    String transactionId = null;
    Optional<String> traceToken = Optional.empty();
    Set<String> clientTags = Collections.emptySet();
    ZoneId timeZonId = TimeZone.getDefault().toZoneId();
    Locale locale = Locale.getDefault();
    Map<String, String> resourceEstimates = Collections.emptyMap();
    Map<String, String> preparedStatements = Collections.emptyMap();
    Map<String, ClientSelectedRole> roles = Collections.emptyMap();
    Map<String, String> extraCredentials = Collections.emptyMap();

    io.airlift.units.Duration clientRequestTimeout =
        new io.airlift.units.Duration(0, TimeUnit.MILLISECONDS);

    ClientSession session =
        new ClientSession(
            httpUri,
            user,
            source,
            traceToken,
            clientTags,
            clientInfo,
            catalog,
            schema,
            "",
            timeZonId,
            locale,
            resourceEstimates,
            properties,
            preparedStatements,
            roles,
            extraCredentials,
            transactionId,
            clientRequestTimeout);
    return session;
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
    ResultSetWriter<? extends MetaData, ? extends Record> resultSetWriter =
        engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE);
    try {
      QueryStatusInfo results = null;
      if (statement.isRunning()) {
        results = statement.currentStatusInfo();
      } else {
        results = statement.finalStatusInfo();
      }
      if (results.getColumns() == null) {
        throw new RuntimeException("openlookeng columns is null.");
      }
      org.apache.linkis.storage.domain.Column[] columns =
          results.getColumns().stream()
              .map(
                  column ->
                      new org.apache.linkis.storage.domain.Column(
                          column.getName(), DataType.toDataType(column.getType()), ""))
              .toArray(org.apache.linkis.storage.domain.Column[]::new);
      columnCount = columns.length;
      resultSetWriter.addMetaData(new TableMetaData(columns));
      while (statement.isRunning()) {
        Iterable<List<Object>> data = statement.currentData().getData();
        if (data != null) {
          for (List<Object> row : data) {
            Object[] rowArray = row.stream().map(String::valueOf).toArray();
            resultSetWriter.addRecord(new TableRecord(rowArray));
            rows += 1;
          }
        }
        engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId));
        statement.advance();
      }
      LOG.warn("Fetched {} col(s) : {} row(s) in openlookeng", columnCount, rows);
      engineExecutorContext.sendResultSet(resultSetWriter);
    } catch (Exception e) {
      IOUtils.closeQuietly(resultSetWriter);
      throw e;
    }
  }

  // check error
  private ErrorExecuteResponse verifyServerError(
      String taskId, EngineExecutionContext engineExecutorContext, StatementClient statement)
      throws OpenLooKengClientException, OpenLooKengStateInvalidException {
    engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId));
    if (statement.isFinished()) {
      QueryStatusInfo info = statement.finalStatusInfo();
      if (info.getError() != null) {
        QueryError error = Objects.requireNonNull(info.getError());
        String message = "openlookeng execute failed (#" + info.getId() + "):" + error.getMessage();
        Throwable cause = null;
        if (error.getFailureInfo() != null) {
          cause = error.getFailureInfo().toException();
        }
        String errorString = "";
        if (cause == null) {
          errorString = ExceptionUtils.getStackTrace(cause);
        }
        engineExecutorContext.appendStdout(LogUtils.generateERROR(errorString));
        return new ErrorExecuteResponse(ExceptionUtils.getMessage(cause), cause);
      }
    } else if (statement.isClientAborted()) {
      LOG.warn("openlookeng statement is killed.");
    } else if (statement.isClientError()) {
      throw new OpenLooKengClientException(
          OPENLOOKENG_CLIENT_ERROR.getErrorCode(), OPENLOOKENG_CLIENT_ERROR.getErrorDesc());
    } else {
      throw new OpenLooKengStateInvalidException(
          OPENLOOKENG_STATUS_ERROR.getErrorCode(), OPENLOOKENG_STATUS_ERROR.getErrorDesc());
    }
    return null;
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
    if (statement.isClearTransactionId()) newSession = ClientSession.stripTransactionId(newSession);

    ClientSession.Builder builder = ClientSession.builder(newSession);

    if (statement.getStartedTransactionId() != null)
      builder = builder.withTransactionId(statement.getStartedTransactionId());

    // update session properties if present
    if (!statement.getSetSessionProperties().isEmpty()
        || !statement.getResetSessionProperties().isEmpty()) {
      Map<String, String> sessionProperties = new HashMap(newSession.getProperties());
      sessionProperties.putAll(statement.getSetSessionProperties());
      sessionProperties.keySet().removeAll(statement.getResetSessionProperties());
      builder = builder.withProperties(sessionProperties);
    }

    // update session roles
    if (!statement.getSetRoles().isEmpty()) {
      Map<String, ClientSelectedRole> roles = new HashMap(newSession.getRoles());
      roles.putAll(statement.getSetRoles());
      builder = builder.withRoles(roles);
    }

    // update prepared statements if present
    if (!statement.getAddedPreparedStatements().isEmpty()
        || !statement.getDeallocatedPreparedStatements().isEmpty()) {
      Map<String, String> preparedStatements = new HashMap(newSession.getPreparedStatements());
      preparedStatements.putAll(statement.getAddedPreparedStatements());
      preparedStatements.keySet().removeAll(statement.getDeallocatedPreparedStatements());
      builder = builder.withPreparedStatements(preparedStatements);
    }
    return builder.build();
  }
}
