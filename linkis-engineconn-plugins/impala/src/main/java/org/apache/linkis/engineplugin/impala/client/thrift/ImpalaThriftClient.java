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

package org.apache.linkis.engineplugin.impala.client.thrift;

import org.apache.linkis.engineplugin.impala.client.ExecutionListener;
import org.apache.linkis.engineplugin.impala.client.ImpalaClient;
import org.apache.linkis.engineplugin.impala.client.exception.ImpalaEngineException;
import org.apache.linkis.engineplugin.impala.client.exception.ImpalaErrorCodeSummary;
import org.apache.linkis.engineplugin.impala.client.protocol.ExecProgress;
import org.apache.linkis.engineplugin.impala.client.protocol.ExecStatus;
import org.apache.linkis.engineplugin.impala.client.protocol.ExecSummary;
import org.apache.linkis.engineplugin.impala.client.util.ThriftUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.hive.service.rpc.thrift.TExecuteStatementReq;
import org.apache.hive.service.rpc.thrift.TExecuteStatementResp;
import org.apache.hive.service.rpc.thrift.TOperationHandle;
import org.apache.hive.service.rpc.thrift.TSessionHandle;
import org.apache.impala.thrift.ImpalaHiveServer2Service;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpalaThriftClient extends TimerTask implements ImpalaClient {

  public static final Logger LOG = LoggerFactory.getLogger(ImpalaThriftClient.class.getName());

  private final Map<String, String> queryOptions;
  private final Map<String, ImpalaThriftExecution> executions;
  private final ImpalaThriftSessionFactory sessionFactory;
  private final ScheduledExecutorService executorService;
  private final long heartBeatsMillis;

  private int batchSize = 1000;
  private long queryTimeoutMillis = 3600 * 1000;
  private volatile boolean closed;

  public ImpalaThriftClient(ImpalaThriftSessionFactory sessionFactory, int heartBeatsInSeconds) {
    this.sessionFactory = sessionFactory;
    this.executions = new ConcurrentHashMap<>();
    this.queryOptions = new ConcurrentHashMap<>();
    this.closed = false;

    this.executorService = Executors.newSingleThreadScheduledExecutor();

    this.heartBeatsMillis = Math.max(heartBeatsInSeconds, 1);
    executorService.schedule(this, heartBeatsInSeconds, TimeUnit.SECONDS);
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void setQueryTimeoutInSeconds(int queryTimeout) {
    this.queryTimeoutMillis = 1000L * queryTimeout;
  }

  @Override
  public synchronized void close() {
    closed = true;
    executorService.shutdownNow();
    for (ImpalaThriftExecution execution : executions.values()) {
      execution.cancel();
    }
  }

  private ImpalaThriftExecution submit(
      String sql,
      Map<String, String> queryOptions,
      ExecutionListener executionListener,
      boolean sync)
      throws TException, ImpalaEngineException, InterruptedException, IOException {
    if (closed) {
      throw ImpalaEngineException.of(ImpalaErrorCodeSummary.ClosedError);
    }

    ImpalaThriftSession impalaSession = sessionFactory.openSession();

    TSessionHandle session = impalaSession.session();
    ImpalaHiveServer2Service.Client client = impalaSession.client();

    TExecuteStatementReq req = new TExecuteStatementReq(session, sql);
    req.setRunAsync(false);

    Map<String, String> options = new TreeMap<>(this.queryOptions);
    if (queryOptions != null && queryOptions.size() > 0) {
      options.putAll(queryOptions);
    }
    req.setConfOverlay(options);

    TExecuteStatementResp res = client.ExecuteStatement(req);
    ThriftUtil.checkStatus(res.getStatus(), executionListener);

    TOperationHandle operation = res.getOperationHandle();
    if (operation == null) {
      throw ImpalaEngineException.of(ImpalaErrorCodeSummary.RequestError);
    }

    String queryId = ThriftUtil.convertUniqueId(operation.getOperationId().getGuid());
    return new ImpalaThriftExecution(impalaSession, operation, queryId, executionListener, sync);
  }

  private boolean progress(ImpalaThriftExecution execution) {
    try {
      ExecSummary summary = execution.getExecSummary();

      ExecutionListener listener = execution.getListener();
      ExecStatus status = summary.getStatus();
      if (status.isActive()) {
        /* executing progress */
        if (listener != null && summary.getProgress() != null) {
          listener.progress(summary.getProgress());
        }

        /* check execution timeout */
        return queryTimeoutMillis <= 0
            || System.currentTimeMillis() - execution.getTimestamp() < queryTimeoutMillis;
      }

      if (status.hasError()) {
        if (listener != null) {
          listener.error(summary.getStatus());
        }
      } else {
        execution.tryFetchResult(batchSize);
      }
      return false;
    } catch (ImpalaEngineException | TException e) {
      LOG.warn("Progress failed for: {}", execution.getQueryId(), e);
      if (execution.errorIncrement() >= 3) {
        return false;
      }

      /* check execution timeout */
      return queryTimeoutMillis <= 0
          || System.currentTimeMillis() - execution.getTimestamp() < queryTimeoutMillis;
    }
  }

  @Override
  public void execute(
      String sql, ExecutionListener executionListener, Map<String, String> queryOptions)
      throws ImpalaEngineException, InterruptedException {
    try (ImpalaThriftExecution execution = submit(sql, queryOptions, executionListener, true)) {
      executions.put(execution.getQueryId(), execution);
      while (progress(execution)) {
        Thread.sleep(heartBeatsMillis);
      }
    } catch (IOException | TException e) {
      throw ImpalaEngineException.of(ImpalaErrorCodeSummary.RequestError, e);
    }
  }

  @Override
  public String executeAsync(
      String sql, ExecutionListener executionListener, Map<String, String> queryOptions)
      throws ImpalaEngineException {
    try {
      ImpalaThriftExecution execution = submit(sql, queryOptions, executionListener, false);
      String queryId = execution.getQueryId();
      executions.put(queryId, execution);
      return queryId;
    } catch (TException | InterruptedException | IOException e) {
      throw ImpalaEngineException.of(ImpalaErrorCodeSummary.RequestError, e);
    }
  }

  @Override
  public void cancel(String queryId) {
    cancel(executions.get(queryId));
  }

  private void cancel(ImpalaThriftExecution execution) {
    if (execution != null) {
      execution.cancel();
    }
  }

  @Override
  public ExecSummary getExecSummary(String queryId) throws ImpalaEngineException {
    ImpalaThriftExecution execution = executions.get(queryId);
    if (execution != null) {
      try {
        return execution.getExecSummary();
      } catch (TException e) {
        throw ImpalaEngineException.of(ImpalaErrorCodeSummary.RequestError, e);
      }
    }
    throw new IllegalArgumentException("query not found: " + queryId);
  }

  @Override
  public ExecProgress getExecProgress(String queryId) throws ImpalaEngineException {
    ExecSummary summary = getExecSummary(queryId);
    if (summary != null) {
      return summary.getProgress();
    }
    return null;
  }

  @Override
  public ExecStatus getExecStatus(String queryId) throws ImpalaEngineException {
    ImpalaThriftExecution execution = executions.get(queryId);
    if (execution != null) {
      try {
        return execution.getExecStatus();
      } catch (TException e) {
        e.printStackTrace();
      }
    }
    throw new IllegalArgumentException("query not found: " + queryId);
  }

  @Override
  public void setQueryOption(String key, String value) {
    if (StringUtils.isNotBlank(value)) {
      queryOptions.put(key, value);
    }
  }

  @Override
  public void unsetQueryOption(String key) {
    queryOptions.remove(key);
  }

  @Override
  public void run() {
    final ArrayList<ImpalaThriftExecution> runningExecutions = new ArrayList<>(executions.values());
    LOG.debug("Impala client heart beats, {} running executions", runningExecutions.size());
    for (ImpalaThriftExecution execution : runningExecutions) {
      /* sync execution, skipping */
      if (execution.isSync()) {
        continue;
      }

      if (execution.isClosed() || !progress(execution)) {
        executions.remove(execution.getQueryId());
      }
    }
  }

  @Override
  public int getRunningExecutionCount() {
    return sessionFactory.openedSessionCount();
  }

  @Override
  public Map<String, String> getQueryOptions() {
    return ImmutableMap.copyOf(queryOptions);
  }
}
