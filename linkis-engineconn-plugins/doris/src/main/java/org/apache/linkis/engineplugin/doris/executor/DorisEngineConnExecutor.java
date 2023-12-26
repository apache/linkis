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

package org.apache.linkis.engineplugin.doris.executor;

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSetWriter;
import org.apache.linkis.common.log.LogUtils;
import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.common.utils.OverloadUtils;
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask;
import org.apache.linkis.engineconn.computation.executor.execute.ConcurrentComputationExecutor;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.engineconn.core.EngineConnObject;
import org.apache.linkis.engineplugin.doris.conf.DorisConfiguration;
import org.apache.linkis.engineplugin.doris.conf.DorisEngineConf;
import org.apache.linkis.engineplugin.doris.errorcode.DorisErrorCodeSummary;
import org.apache.linkis.engineplugin.doris.exception.DorisException;
import org.apache.linkis.engineplugin.doris.exception.DorisParameterException;
import org.apache.linkis.engineplugin.doris.exception.DorisStreamLoadFileException;
import org.apache.linkis.engineplugin.doris.util.DorisUtils;
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
import org.apache.linkis.storage.LineMetaData;
import org.apache.linkis.storage.LineRecord;
import org.apache.linkis.storage.resultset.ResultSetFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import scala.Tuple2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.engineplugin.doris.constant.DorisConstant.*;

/**
 * Inspired by:
 * https://github.com/apache/doris/blob/master/samples/stream_load/java/DorisStreamLoad.java
 */
public class DorisEngineConnExecutor extends ConcurrentComputationExecutor {

  private static final Logger logger = LoggerFactory.getLogger(DorisEngineConnExecutor.class);
  private int id;

  private List<Label<?>> executorLabels = new ArrayList<>(2);
  private Map<String, Thread> threadCache = new ConcurrentHashMap<>();

  private Map<String, String> configMap = new HashMap<>();

  private static final String DORIS_LABEL_PREFIX = "linkis_doris_";

  public static final String DORIS_URL_BOOTSTRAP = "http://%s:%s/api/bootstrap";

  public static final String DORIS_URL_STREAM_LOAD = "http://%s:%s/api/%s/%s/_stream_load";

  public static final String DORIS_URL_STREAM_LOAD_2PC = "http://%s:%s/api/%s/%s/_stream_load_2pc";

  private String dorisHost;
  private String dorisDatabase;
  private String dorisTable;
  private String dorisUsername;
  private String dorisPassword;

  private String dorisStreamLoadFilePath;
  private Integer dorisHttpPort;
  private CloseableHttpClient client;

  public DorisEngineConnExecutor(int outputPrintLimit, int id) {
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
          new DorisEngineConf().getCacheMap(new Tuple2<>(userCreatorLabel, engineTypeLabel));
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

    checkParameter();

    this.client =
        HttpClients.custom()
            .setRedirectStrategy(
                new DefaultRedirectStrategy() {
                  @Override
                  protected boolean isRedirectable(String method) {
                    return true;
                  }
                })
            .build();
    return super.execute(engineConnTask);
  }

  @Override
  public ExecuteResponse executeLine(EngineExecutionContext engineExecutorContext, String code) {
    String realCode;
    if (StringUtils.isBlank(code)) {
      throw new DorisException(
          DorisErrorCodeSummary.DORIS_CODE_IS_NOT_BLANK.getErrorCode(),
          DorisErrorCodeSummary.DORIS_CODE_IS_NOT_BLANK.getErrorDesc());
    } else {
      realCode = code.trim();
    }
    logger.info("Doris engine begins to run code:\n {}", realCode);

    checkRequiredParameter(realCode);

    String testConnectionUrl = String.format(DORIS_URL_BOOTSTRAP, dorisHost, dorisHttpPort);

    if (!testConnection(testConnectionUrl)) {
      logger.error("Test connection failed: {}", testConnectionUrl);
      throw new DorisException(
          DorisErrorCodeSummary.DORIS_TEST_CONNECTION_FAILED.getErrorCode(),
          DorisErrorCodeSummary.DORIS_TEST_CONNECTION_FAILED.getErrorDesc());
    }

    String taskId = engineExecutorContext.getJobId().get();

    initialStatusUpdates(taskId, engineExecutorContext);

    threadCache.put(taskId, Thread.currentThread());

    CloseableHttpResponse response = null;
    Boolean executeResponse = false;
    try {
      response = streamLoad(engineExecutorContext);
    } catch (Exception e) {
      String errorMessage = ExceptionUtils.getStackTrace(e);
      logger.error("Doris engine execute failed : {}", errorMessage);
      engineExecutorContext.appendStdout(LogUtils.generateERROR(errorMessage));
      return new ErrorExecuteResponse(errorMessage, null);
    }

    String resultMessage = entitytoString(response);
    StringBuilder resultMessageStringBuilder = new StringBuilder();
    resultMessageStringBuilder.append(resultMessage);

    logger.info("Doris stream load execution result: {}", resultMessage);
    Map<String, String> resultMap = responseToMap(resultMessage);
    int statusCode = response.getStatusLine().getStatusCode();
    Boolean enabled2PC = DorisConfiguration.DORIS_RECONNECT_ENABLED.getValue(configMap);

    // If two phase commit is enabled, commit if executed successfully, abort otherwise
    if (statusCode == HTTP_SUCCEED && isSuccess(resultMap)) {
      executeResponse = true;
      if (enabled2PC && resultMap.containsKey(TXN_ID)) {
        String commitMessage = "doris begin commit";
        logger.info(commitMessage);
        engineExecutorContext.appendStdout(commitMessage);
        executeResponse =
            dorisCommitOrAbort(resultMap.get(TXN_ID), COMMIT, resultMessageStringBuilder);
      }
    } else {
      executeResponse = false;
      if (enabled2PC && resultMap.containsKey(TXN_ID)) {
        String abortMessage = "doris stream load failed, begin abort";
        logger.error(abortMessage);
        engineExecutorContext.appendStdout(abortMessage);
        dorisCommitOrAbort(resultMap.get(TXN_ID), ABORT, resultMessageStringBuilder);
      }
    }

    engineExecutorContext.appendStdout(resultMessageStringBuilder.toString());
    ResultSetWriter<? extends MetaData, ? extends Record> resultSetWriter =
        engineExecutorContext.createResultSetWriter(ResultSetFactory.TEXT_TYPE);
    try {
      resultSetWriter.addMetaData(new LineMetaData());
      resultSetWriter.addRecord(new LineRecord(resultMessageStringBuilder.toString()));
    } catch (IOException e) {
      logger.error("Failed to get the task result");
    } finally {
      IOUtils.closeQuietly(resultSetWriter);
    }

    if (executeResponse) {
      return new SuccessExecuteResponse();
    } else {
      return new ErrorExecuteResponse(resultMessageStringBuilder.toString(), null);
    }
  }

  private CloseableHttpResponse streamLoad(EngineExecutionContext engineExecutorContext)
      throws Exception {
    String loadUrl =
        String.format(DORIS_URL_STREAM_LOAD, dorisHost, dorisHttpPort, dorisDatabase, dorisTable);

    logger.info("Doris engine stream load begins to run loadUrl:\n {}", loadUrl);
    engineExecutorContext.appendStdout(
        String.format("Doris engine stream load begins to run loadUrl:\n %s", loadUrl));

    HttpPut httpPut = new HttpPut(loadUrl);

    // Set the doris configuration, which has a low priority and will be overwritten by other
    // configurations
    String dorisConf = DorisConfiguration.DORIS_CONF.getValue(configMap);
    if (StringUtils.isNotBlank(dorisConf)) {
      String[] confs = dorisConf.split(",");
      for (String conf : confs) {
        String[] keyValue = conf.split(":");
        if (keyValue.length == 2) {
          String key = keyValue[0];
          String value = keyValue[1];
          httpPut.setHeader(key, value);
          logger.info("doris set param {} : {}", key, value);
        }
      }
    }

    addCommonHeader(httpPut);

    String dorisColumns = DorisConfiguration.DORIS_COLUMNS.getValue(configMap);

    if (StringUtils.isBlank(dorisColumns)) {
      Integer dorisJdbcPort = DorisConfiguration.DORIS_JDBC_PORT.getValue(configMap);
      List<String> dorisCloumns =
          DorisUtils.getDorisCloumns(
              dorisHost, dorisJdbcPort, dorisUsername, dorisPassword, dorisDatabase, dorisTable);
      if (org.apache.commons.collections.CollectionUtils.isNotEmpty(dorisCloumns)) {
        dorisColumns =
            String.join(
                ",",
                dorisCloumns.stream()
                    .map(f -> String.format("`%s`", f))
                    .collect(Collectors.toList()));
      }
    }

    // dorisColumns may fail to obtain metadata and is empty
    if (StringUtils.isNotBlank(dorisColumns)) {
      httpPut.setHeader(COLUMNS, dorisColumns);
      logger.info("doris set param {} : {}", COLUMNS, dorisColumns);
    }

    // the label header is optional, not necessary
    // use label header can ensure at most once semantics
    String dorisLabel = DorisConfiguration.DORIS_LABEL.getValue(configMap);
    if (StringUtils.isBlank(dorisLabel)) {
      dorisLabel = DORIS_LABEL_PREFIX + UUID.randomUUID();
    }
    httpPut.setHeader(LABEL, dorisLabel);
    logger.info("doris set param {} : {}", LABEL, dorisLabel);

    File dorisStreamLoadFile = new File(dorisStreamLoadFilePath);
    if (!dorisStreamLoadFile.isFile()) {
      throw new DorisStreamLoadFileException(
          DorisErrorCodeSummary.DORIS_STREAM_LOAD_FILE_PATH_NOT_FILE.getErrorCode(),
          DorisErrorCodeSummary.DORIS_STREAM_LOAD_FILE_PATH_NOT_FILE.getErrorDesc());
    }

    String fileExtension = FilenameUtils.getExtension(dorisStreamLoadFilePath);

    // Currently only csv、json、parquet、orc format are supported
    if (!isSupportedType(fileExtension)) {
      logger.error(
          "The supported types are csv, json, parquet, and orc,This file type is not currently supported: {}",
          fileExtension);
      throw new DorisStreamLoadFileException(
          DorisErrorCodeSummary.DORIS_STREAM_LOAD_FILE_PATH_NOT_SUPPORTED_TYPE_FILE.getErrorCode(),
          DorisErrorCodeSummary.DORIS_STREAM_LOAD_FILE_PATH_NOT_SUPPORTED_TYPE_FILE.getErrorDesc());
    }

    httpPut.setHeader(FORMAT, fileExtension);

    Boolean enabled2PC = DorisConfiguration.DORIS_RECONNECT_ENABLED.getValue(configMap);
    httpPut.setHeader(TWO_PHASE_COMMIT, String.valueOf(enabled2PC));
    logger.info("doris set param {} : {}", TWO_PHASE_COMMIT, enabled2PC);

    if (fileExtension.equals(JSON)) {
      Boolean stripOuterArray = DorisConfiguration.DORIS_STRIP_OUTER_ARRAY.getValue(configMap);
      httpPut.setHeader(STRIP_OUTER_ARRAY, String.valueOf(stripOuterArray));
      logger.info("doris set param {} : {}", STRIP_OUTER_ARRAY, stripOuterArray);
    }

    String dorisColumnSeparator = DorisConfiguration.DORIS_COLUMN_SEPARATOR.getValue(configMap);
    httpPut.setHeader(COLUMN_SEPARATOR, dorisColumnSeparator);
    logger.info("doris set param {} : {}", COLUMN_SEPARATOR, dorisColumnSeparator);

    String dorisLineDelimiter = DorisConfiguration.DORIS_LINE_DELIMITER.getValue(configMap);
    httpPut.setHeader(LINE_DELIMITER, dorisLineDelimiter);
    logger.info("doris set param {} : {}", LINE_DELIMITER, dorisLineDelimiter);

    FileEntity entity = new FileEntity(dorisStreamLoadFile);
    httpPut.setEntity(entity);
    engineExecutorContext.appendStdout(
        String.format("doris stread load file path: %s", dorisStreamLoadFile.getAbsolutePath()));

    String allHeaders = Arrays.toString(httpPut.getAllHeaders());
    logger.info("doris param: {}", allHeaders);
    engineExecutorContext.appendStdout(String.format("doris param: %s", allHeaders));

    return client.execute(httpPut);
  }

  private boolean isSupportedType(String fileExtension) {
    if (StringUtils.isBlank(fileExtension)) {
      return false;
    }
    if (fileExtension.equals(CSV)
        || fileExtension.equals(JSON)
        || fileExtension.equals(PARQUET)
        || fileExtension.equals(ORC)) {
      return true;
    }
    return false;
  }

  private void checkParameter() {
    String dorisHost = DorisConfiguration.DORIS_HOST.getValue(configMap);
    String dorisUsername = DorisConfiguration.DORIS_USER_NAME.getValue(configMap);
    Integer dorisHttpPort = DorisConfiguration.DORIS_HTTP_PORT.getValue(configMap);

    if (StringUtils.isBlank(dorisHost)
        || StringUtils.isBlank(dorisUsername)
        || dorisHttpPort == null) {
      logger.error("Doris check param failed.");
      throw new DorisParameterException(
          DorisErrorCodeSummary.CHECK_DORIS_PARAMETER_FAILED.getErrorCode(),
          DorisErrorCodeSummary.CHECK_DORIS_PARAMETER_FAILED.getErrorDesc());
    }

    this.dorisHost = dorisHost;
    this.dorisUsername = dorisUsername;
    this.dorisHttpPort = dorisHttpPort;
    this.dorisPassword = DorisConfiguration.DORIS_PASSWORD.getValue(configMap);
  }

  private void checkRequiredParameter(String code) {
    Map<String, String> codeMap = new HashMap<>();

    try {
      codeMap =
          JsonUtils.jackson().readValue(code, new TypeReference<HashMap<String, String>>() {});
    } catch (JsonProcessingException e) {
      throw new DorisException(
          DorisErrorCodeSummary.DORIS_CODE_FAILED_TO_CONVERT_JSON.getErrorCode(),
          DorisErrorCodeSummary.DORIS_CODE_FAILED_TO_CONVERT_JSON.getErrorDesc());
    }

    String dorisStreamLoadFilePath =
        codeMap.getOrDefault(DorisConfiguration.DORIS_STREAM_LOAD_FILE_PATH.key(), "");
    String dorisDatabase = codeMap.getOrDefault(DorisConfiguration.DORIS_DATABASE.key(), "");
    String dorisTable = codeMap.getOrDefault(DorisConfiguration.DORIS_TABLE.key(), "");

    if (StringUtils.isBlank(dorisStreamLoadFilePath)
        || StringUtils.isBlank(dorisDatabase)
        || StringUtils.isBlank(dorisTable)) {
      logger.error(
          "Check whether `{}`, `{}`, and `{}` are included in code json",
          DorisConfiguration.DORIS_STREAM_LOAD_FILE_PATH.key(),
          DorisConfiguration.DORIS_DATABASE.key(),
          DorisConfiguration.DORIS_TABLE.key());
      throw new DorisException(
          DorisErrorCodeSummary.DORIS_REQUIRED_PARAMETER_IS_NOT_BLANK.getErrorCode(),
          DorisErrorCodeSummary.DORIS_REQUIRED_PARAMETER_IS_NOT_BLANK.getErrorDesc());
    }

    this.dorisStreamLoadFilePath = dorisStreamLoadFilePath;
    this.dorisDatabase = dorisDatabase;
    this.dorisTable = dorisTable;
    logger.info(
        "Doris parameter dorisStreamLoadFilePath: {}, dorisDatabase: {}, dorisTable: {}.",
        this.dorisStreamLoadFilePath,
        this.dorisDatabase,
        this.dorisTable);
  }

  private boolean isSuccess(Map<String, String> map) {
    if (org.apache.commons.collections.MapUtils.isEmpty(map)) {
      return false;
    }

    if (map.containsKey(STATUS) && map.get(STATUS).equalsIgnoreCase(SUCCESS)) {
      return true;
    }

    // Sometimes Status is uppercase and sometimes lowercase
    if (map.containsKey(STATUS.toLowerCase())
        && map.get(STATUS.toLowerCase()).equalsIgnoreCase(SUCCESS)) {
      return true;
    }

    return false;
  }

  /** After the two phase commit is enabled, you can use it to commit or abort */
  private boolean dorisCommitOrAbort(
      String id, String type, StringBuilder resultMessageStringBuilder) {
    String load2PCUrl =
        String.format(
            DORIS_URL_STREAM_LOAD_2PC, dorisHost, dorisHttpPort, dorisDatabase, dorisTable);
    HttpPut commmitHttpPut = new HttpPut(load2PCUrl);
    addCommonHeader(commmitHttpPut);
    commmitHttpPut.setHeader(TXN_ID_LOWER, id);
    commmitHttpPut.setHeader(TXN_OPERATION, type);

    CloseableHttpResponse commmitResponse = null;
    try {
      commmitResponse = client.execute(commmitHttpPut);
    } catch (IOException e) {
      logger.error("doris {} failed", type, e);
      return false;
    }
    String commmitLoadResult = entitytoString(commmitResponse);
    logger.info("Doris stream load {} execution result: {}", type, commmitLoadResult);
    resultMessageStringBuilder.append("\r\n").append(commmitLoadResult);

    Map<String, String> commmitResultMap = responseToMap(commmitLoadResult);
    int statusCode = commmitResponse.getStatusLine().getStatusCode();

    if (statusCode == HTTP_SUCCEED && isSuccess(commmitResultMap)) {
      return true;
    }
    return false;
  }

  private static String entitytoString(CloseableHttpResponse response) {
    String loadResult = "";
    if (response.getEntity() != null) {
      try {
        loadResult = EntityUtils.toString(response.getEntity());
      } catch (IOException e) {
        logger.error("Doris httpResponse entity conversion to string failed", e);
      }
    }
    return loadResult;
  }

  private void addCommonHeader(HttpPut httpPut) {
    if (httpPut == null) return;
    httpPut.setHeader(HttpHeaders.EXPECT, "100-continue");
    httpPut.setHeader(HttpHeaders.AUTHORIZATION, basicAuthHeader(dorisUsername, dorisPassword));
  }

  private Map<String, String> responseToMap(String response) {
    Map<String, String> resultMap = new HashMap<>();

    if (StringUtils.isBlank(response)) {
      return resultMap;
    }

    try {
      resultMap =
          JsonUtils.jackson().readValue(response, new TypeReference<HashMap<String, String>>() {});
    } catch (JsonProcessingException e) {
      logger.error("doris response to map failed", e);
      return resultMap;
    }

    return resultMap;
  }

  private boolean testConnection(String testUrl) {
    try {
      URL url = new URL(testUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(5000);
      connection.setRequestMethod("GET");
      connection.connect();
      int responseCode = connection.getResponseCode();
      if (responseCode == HTTP_SUCCEED) {
        return true;
      }
    } catch (Exception e) {
      return false;
    }
    return false;
  }

  private String basicAuthHeader(String username, String password) {
    String tobeEncode = username + ":" + password;
    byte[] encoded = Base64.encodeBase64(tobeEncode.getBytes(StandardCharsets.UTF_8));
    return "Basic " + new String(encoded);
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
    return DorisConfiguration.ENGINE_CONCURRENT_LIMIT.getValue();
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
    try {
      if (client != null) {
        this.client.close();
      }
    } catch (IOException e) {
      logger.warn("close doris HttpClient failed");
    }
    super.close();
  }
}
