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

package org.apache.linkis.engineplugin.elasticsearch.executor;

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSetWriter;
import org.apache.linkis.common.utils.OverloadUtils;
import org.apache.linkis.engineconn.common.conf.EngineConnConf;
import org.apache.linkis.engineconn.common.conf.EngineConnConstant;
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask;
import org.apache.linkis.engineconn.computation.executor.execute.ConcurrentComputationExecutor;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.engineconn.core.EngineConnObject;
import org.apache.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration;
import org.apache.linkis.engineplugin.elasticsearch.conf.ElasticSearchEngineConsoleConf;
import org.apache.linkis.engineplugin.elasticsearch.executor.client.*;
import org.apache.linkis.engineplugin.elasticsearch.executor.client.impl.ElasticSearchExecutorImpl;
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus;
import org.apache.linkis.manager.common.entity.resource.CommonNodeResource;
import org.apache.linkis.manager.common.entity.resource.LoadResource;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.executer.AliasOutputExecuteResponse;
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse;
import org.apache.linkis.scheduler.executer.ExecuteResponse;
import org.apache.linkis.storage.LineRecord;
import org.apache.linkis.storage.resultset.ResultSetFactory;
import org.apache.linkis.storage.resultset.table.TableMetaData;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;

import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchEngineConnExecutor extends ConcurrentComputationExecutor {
  private static final Logger logger =
      LoggerFactory.getLogger(ElasticSearchEngineConnExecutor.class);

  private int id;
  private String runType;
  private List<Label<?>> executorLabels = new ArrayList<>(2);

  private Cache<String, ElasticSearchExecutor> elasticSearchExecutorCache =
      CacheBuilder.newBuilder()
          .expireAfterAccess(
              Long.valueOf(EngineConnConf.ENGINE_TASK_EXPIRE_TIME().getValue().toString()),
              TimeUnit.MILLISECONDS)
          .removalListener(
              new RemovalListener<String, ElasticSearchExecutor>() {
                @Override
                public void onRemoval(
                    RemovalNotification<String, ElasticSearchExecutor> notification) {
                  notification.getValue().close();
                  EngineConnTask task = getTaskById(notification.getKey());
                  if (!ExecutionNodeStatus.isCompleted(task.getStatus())) {
                    killTask(notification.getKey());
                  }
                }
              })
          .maximumSize(EngineConnConstant.MAX_TASK_NUM())
          .build();

  public ElasticSearchEngineConnExecutor(int outputPrintLimit, int id, String runType) {
    super(outputPrintLimit);
    this.id = id;
    this.runType = runType;
  }

  @Override
  public void init() {
    super.init();
  }

  @Override
  public ExecuteResponse execute(EngineConnTask engineConnTask) {
    Map<String, String> properties = buildRuntimeParams(engineConnTask);
    logger.info("The elasticsearch properties is: {}", properties);

    //    ElasticSearchExecutor elasticSearchExecutor = ElasticSearchExecutor.apply(runType,
    // properties);
    ElasticSearchExecutor elasticSearchExecutor =
        new ElasticSearchExecutorImpl(runType, properties);
    try {
      elasticSearchExecutor.open();
    } catch (Exception e) {
      logger.error("Execute es code failed, reason:", e);
      return new ErrorExecuteResponse("run es failed", e);
    }
    elasticSearchExecutorCache.put(engineConnTask.getTaskId(), elasticSearchExecutor);
    return super.execute(engineConnTask);
  }

  @Override
  public ExecuteResponse executeLine(EngineExecutionContext engineExecutorContext, String code) {
    String taskId = engineExecutorContext.getJobId().get();
    ElasticSearchExecutor elasticSearchExecutor = elasticSearchExecutorCache.getIfPresent(taskId);
    ElasticSearchResponse elasticSearchResponse = elasticSearchExecutor.executeLine(code);

    try {

      if (elasticSearchResponse instanceof ElasticSearchTableResponse) {
        ElasticSearchTableResponse tableResponse =
            (ElasticSearchTableResponse) elasticSearchResponse;
        TableMetaData metaData = new TableMetaData(tableResponse.columns());
        ResultSetWriter<? extends MetaData, ? extends Record> resultSetWriter =
            engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE);
        resultSetWriter.addMetaData(metaData);
        Arrays.asList(tableResponse.records())
            .forEach(
                record -> {
                  try {
                    resultSetWriter.addRecord(record);
                  } catch (IOException e) {
                    logger.warn("es addRecord failed", e);
                    throw new RuntimeException("es addRecord failed", e);
                  }
                });
        String output = resultSetWriter.toString();
        IOUtils.closeQuietly(resultSetWriter);
        return new AliasOutputExecuteResponse(null, output);
      } else if (elasticSearchResponse instanceof ElasticSearchJsonResponse) {
        ElasticSearchJsonResponse jsonResponse = (ElasticSearchJsonResponse) elasticSearchResponse;
        ResultSetWriter<? extends MetaData, ? extends Record> resultSetWriter =
            engineExecutorContext.createResultSetWriter(ResultSetFactory.TEXT_TYPE);
        resultSetWriter.addMetaData(null);
        Arrays.stream(jsonResponse.value().split("\\n"))
            .forEach(
                item -> {
                  try {
                    resultSetWriter.addRecord(new LineRecord(item));
                  } catch (IOException e) {
                    logger.warn("es addRecord failed", e);
                    throw new RuntimeException("es addRecord failed", e);
                  }
                });
        String output = resultSetWriter.toString();
        IOUtils.closeQuietly(resultSetWriter);
        return new AliasOutputExecuteResponse(null, output);
      } else if (elasticSearchResponse instanceof ElasticSearchErrorResponse) {
        ElasticSearchErrorResponse errorResponse =
            (ElasticSearchErrorResponse) elasticSearchResponse;
        return new ErrorExecuteResponse(errorResponse.message(), errorResponse.cause());
      }
    } catch (IOException e) {
      logger.warn("es addMetaData failed", e);
      return new ErrorExecuteResponse("es addMetaData failed", e);
    }

    return new ErrorExecuteResponse("es executeLine failed", null);
  }

  private Map<String, String> buildRuntimeParams(EngineConnTask engineConnTask) {
    // Parameters specified at runtime
    Map<String, String> executorProperties =
        engineConnTask.getProperties().entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, entry -> Objects.toString(entry.getValue(), null)));

    // Global engine params by console
    Map<String, String> globalConfig =
        new ElasticSearchEngineConsoleConf().getCacheMap(engineConnTask.getLables());

    if (MapUtils.isNotEmpty(executorProperties)) {
      globalConfig.putAll(executorProperties);
    }

    return globalConfig;
  }

  @Override
  public ExecuteResponse executeCompletely(
      EngineExecutionContext engineExecutorContext, String code, String completedLine) {
    return null;
  }

  @Override
  public float progress(String taskId) {
    return 0.0f;
  }

  @Override
  public JobProgressInfo[] getProgressInfo(String taskId) {
    return new JobProgressInfo[0];
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
    return ElasticSearchConfiguration.ENGINE_CONCURRENT_LIMIT.getValue();
  }

  @Override
  public void killTask(String taskId) {

    ElasticSearchExecutor elasticSearchExecutor = elasticSearchExecutorCache.getIfPresent(taskId);
    if (elasticSearchExecutor != null) {
      elasticSearchExecutor.close();
    }

    super.killTask(taskId);
  }

  @Override
  public void killAll() {
    elasticSearchExecutorCache.asMap().values().forEach(e -> e.close());
  }

  @Override
  public void transformTaskStatus(EngineConnTask task, ExecutionNodeStatus newStatus) {
    super.transformTaskStatus(task, newStatus);
    if (ExecutionNodeStatus.isCompleted(newStatus)) {
      elasticSearchExecutorCache.invalidate(task.getTaskId());
    }
  }

  @Override
  public boolean supportCallBackLogs() {
    return false;
  }
}
