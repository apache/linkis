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
import org.apache.linkis.engineplugin.impala.client.exception.ImpalaEngineException;
import org.apache.linkis.engineplugin.impala.client.protocol.ExecProgress;
import org.apache.linkis.engineplugin.impala.client.protocol.ExecStatus;
import org.apache.linkis.engineplugin.impala.client.protocol.ExecSummary;
import org.apache.linkis.engineplugin.impala.client.util.ThriftUtil;

import org.apache.hive.service.rpc.thrift.TCancelOperationReq;
import org.apache.hive.service.rpc.thrift.TCloseOperationReq;
import org.apache.hive.service.rpc.thrift.TGetOperationStatusReq;
import org.apache.hive.service.rpc.thrift.TGetOperationStatusResp;
import org.apache.hive.service.rpc.thrift.TOperationHandle;
import org.apache.hive.service.rpc.thrift.TOperationState;
import org.apache.impala.thrift.TExecProgress;
import org.apache.impala.thrift.TExecSummary;
import org.apache.impala.thrift.TGetExecSummaryReq;
import org.apache.impala.thrift.TGetExecSummaryResp;
import org.apache.thrift.TException;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpalaThriftExecution implements AutoCloseable {
  public static final Logger LOG = LoggerFactory.getLogger(ImpalaThriftExecution.class.getName());

  private final ImpalaThriftSession impalaSession;
  private final TOperationHandle operation;
  private final String queryId;
  private final ExecutionListener listener;
  private final boolean sync;

  private long timestamp;
  private int errors;

  private volatile boolean closed;

  public ImpalaThriftExecution(
      ImpalaThriftSession impalaSession,
      TOperationHandle operation,
      String queryId,
      ExecutionListener listener,
      boolean sync) {
    this.impalaSession = impalaSession;
    this.operation = operation;
    this.queryId = queryId;
    this.listener = listener;
    this.sync = sync;

    if (listener != null) {
      listener.created(queryId);
    }

    this.timestamp = System.currentTimeMillis();
    this.errors = 0;
  }

  public ExecutionListener getListener() {
    return listener;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void tryFetchResult(int batchSize) {
    if (operation.isHasResultSet()) {
      try (ImpalaThriftResultSetV7 resultSetV7 =
          new ImpalaThriftResultSetV7(impalaSession.client(), operation, batchSize)) {
        if (listener != null) {
          listener.success(resultSetV7);
        }
      }
    }
  }

  public synchronized int errorIncrement() {
    return ++errors;
  }

  public String getQueryId() {
    return queryId;
  }

  @Override
  public synchronized void close() {
    if (closed) {
      return;
    }
    closed = true;

    try {
      TCloseOperationReq closeReq = new TCloseOperationReq();
      closeReq.setOperationHandle(operation);
      impalaSession.client().CloseOperation(closeReq);
    } catch (Exception e) {
      LOG.error("Failed to close the operation", e);
    } finally {
      impalaSession.close();
    }
  }

  public synchronized boolean cancel() {
    if (closed) {
      return false;
    }

    try {
      TCancelOperationReq cancelReq = new TCancelOperationReq();
      cancelReq.setOperationHandle(operation);
      impalaSession.client().CancelOperation(cancelReq);
    } catch (Exception e) {
      LOG.error("Failed to safely cancel the query", e);
    }
    close();
    return true;
  }

  public boolean isClosed() {
    return closed;
  }

  public synchronized ExecStatus getExecStatus() throws TException, ImpalaEngineException {
    TGetOperationStatusReq statusReq = new TGetOperationStatusReq(operation);
    TGetOperationStatusResp statusResp = impalaSession.client().GetOperationStatus(statusReq);
    ThriftUtil.checkStatus(statusResp.getStatus());

    TOperationState operationState = statusResp.getOperationState();

    return new ExecStatus(
        operationState.getValue(), operationState.name(), statusResp.getErrorMessage());
  }

  public synchronized ExecSummary getExecSummary() throws TException, ImpalaEngineException {
    ExecProgress progress = ExecProgress.DEFAULT_PROGRESS;
    int nodeNum = -1;

    ExecStatus status = getExecStatus();
    if (status.isActive()) {
      TGetExecSummaryReq getExecSummaryReq = new TGetExecSummaryReq();
      getExecSummaryReq.setOperationHandle(operation);
      getExecSummaryReq.setSessionHandle(impalaSession.session());
      TGetExecSummaryResp execSummaryResp =
          impalaSession.client().GetExecSummary(getExecSummaryReq);

      ThriftUtil.checkStatus(execSummaryResp.getStatus());
      TExecSummary summary = execSummaryResp.getSummary();
      nodeNum = summary.getNodesSize();
      if (summary.isIs_queued()) {
        if (listener != null) {
          listener.message(Lists.newArrayList(summary.getQueued_reason()));
        }
      } else {
        TExecProgress p = summary.getProgress();
        if (p != null) {
          progress = new ExecProgress(p.total_scan_ranges, p.num_completed_scan_ranges, nodeNum);
        }
      }
    }

    return new ExecSummary(status, progress, nodeNum);
  }

  public boolean isSync() {
    return sync;
  }
}
