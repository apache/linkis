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

package org.apache.linkis.engineconnplugin.flink.client.result;

import org.apache.linkis.engineconnplugin.flink.client.shims.exception.SqlExecutionException;
import org.apache.linkis.engineconnplugin.flink.listener.RowsType;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.streaming.experimental.SocketStreamIterator;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.sinks.TableSink;
import org.apache.flink.types.Row;
import org.apache.hadoop.yarn.api.records.ApplicationId;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.ERROR_SUBMITTING_JOB;
import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.NOT_SOCKET_RETRIEVAL;

public class ChangelogResult extends AbstractResult<ApplicationId, Tuple2<Boolean, Row>> {

  private static final Logger LOG = LoggerFactory.getLogger(ChangelogResult.class);

  private final SocketStreamIterator<Tuple2<Boolean, Row>> iterator;
  private final CollectStreamTableSink collectTableSink;
  private final ResultRetrievalThread retrievalThread;
  private CompletableFuture<JobExecutionResult> jobExecutionResultFuture;

  private final Object resultLock;
  private AtomicReference<SqlExecutionException> executionException = new AtomicReference<>();
  private final List<Tuple2<Boolean, Row>> changeRecordBuffer;
  private final int maxBufferSize;

  ChangelogResult(
      RowTypeInfo outputType,
      TableSchema tableSchema,
      ExecutionConfig config,
      InetAddress gatewayAddress,
      int gatewayPort,
      int maxBufferSize)
      throws SqlExecutionException {
    resultLock = new Object();

    // create socket stream iterator
    final TypeInformation<Tuple2<Boolean, Row>> socketType = Types.TUPLE(Types.BOOLEAN, outputType);
    final TypeSerializer<Tuple2<Boolean, Row>> serializer = socketType.createSerializer(config);
    try {
      // pass gateway port and address such that iterator knows where to bind to
      iterator = new SocketStreamIterator<>(gatewayPort, gatewayAddress, serializer);
    } catch (IOException e) {
      throw new SqlExecutionException(NOT_SOCKET_RETRIEVAL.getErrorDesc(), e);
    }

    // create table sink
    // pass binding address and port such that sink knows where to send to
    collectTableSink =
        new CollectStreamTableSink(
            iterator.getBindAddress(), iterator.getPort(), serializer, tableSchema);
    retrievalThread = new ResultRetrievalThread();
    // prepare for changelog
    changeRecordBuffer = new ArrayList<>();
    this.maxBufferSize = maxBufferSize;
  }

  @Override
  public void startRetrieval(JobClient jobClient) {
    // start listener thread
    retrievalThread.setName(jobClient.getJobID().toHexString() + "-JobResult-Fetch-Thread");
    retrievalThread.start();

    jobExecutionResultFuture =
        CompletableFuture.completedFuture(jobClient)
            .thenCompose(JobClient::getJobExecutionResult)
            .whenComplete(
                (unused, throwable) -> {
                  if (throwable != null) {
                    LOG.warn("throwable is not null", throwable);
                    executionException.compareAndSet(
                        null,
                        new SqlExecutionException(ERROR_SUBMITTING_JOB.getErrorDesc(), throwable));
                  } else {
                    LOG.warn("throwable is null");
                  }
                });
  }

  @Override
  public TypedResult<List<Tuple2<Boolean, Row>>> retrieveChanges() throws SqlExecutionException {
    synchronized (resultLock) {
      // retrieval thread is alive return a record if available
      // but the program must not have failed
      if (isRetrieving() && executionException.get() == null) {
        if (changeRecordBuffer.isEmpty()) {
          return TypedResult.empty();
        } else {
          final List<Tuple2<Boolean, Row>> change = new ArrayList<>(changeRecordBuffer);
          changeRecordBuffer.clear();
          resultLock.notifyAll();
          return TypedResult.payload(change);
        }
      }
      // retrieval thread is dead but there is still a record to be delivered
      else if (!isRetrieving() && !changeRecordBuffer.isEmpty()) {
        final List<Tuple2<Boolean, Row>> change = new ArrayList<>(changeRecordBuffer);
        changeRecordBuffer.clear();
        return TypedResult.payload(change);
      }
      // no results can be returned anymore
      else {
        return handleMissingResult();
      }
    }
  }

  @Override
  public TableSink<?> getTableSink() {
    return collectTableSink;
  }

  @Override
  public void close() {
    retrievalThread.isRunning = false;
    iterator.close();
  }

  // --------------------------------------------------------------------------------------------

  private <T> TypedResult<T> handleMissingResult() throws SqlExecutionException {

    // check if the monitoring thread is still there
    // we need to wait until we know what is going on
    if (!jobExecutionResultFuture.isDone()) {
      return TypedResult.empty();
    }

    if (executionException.get() != null) {
      throw executionException.get();
    }

    // we assume that a bounded job finished
    return TypedResult.endOfStream();
  }

  private boolean isRetrieving() {
    return retrievalThread.isRunning;
  }

  private void processRecord(Tuple2<Boolean, Row> change) {
    synchronized (resultLock) {
      // wait if the buffer is full
      while (changeRecordBuffer.size() >= maxBufferSize) {
        try {
          getFlinkStreamingResultSetListeners()
              .forEach(listener -> listener.onResultSetPulled(changeRecordBuffer.size()));
          resultLock.wait();
        } catch (InterruptedException e) {
          // ignore
        }
      }
      changeRecordBuffer.add(change);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Accept the streaming result, row is {}.", change.f1);
    }
  }

  // --------------------------------------------------------------------------------------------

  private class ResultRetrievalThread extends Thread {

    volatile boolean isRunning = true;
    private boolean isStatusListenersNotified = false;

    @Override
    public void run() {
      int rows = 0;
      try {
        while (isRunning && iterator.hasNext()) {
          final Tuple2<Boolean, Row> change = iterator.next();
          processRecord(change);
          rows++;
        }
      } catch (Exception e) {
        // ignore socket exceptions
        LOG.warn(getName() + " has finished with an error, ignore it.", e);
      }
      if (!changeRecordBuffer.isEmpty()) {
        dealOrFailed(
            () -> {
              getFlinkStreamingResultSetListeners()
                  .forEach(listener -> listener.onResultSetPulled(changeRecordBuffer.size()));
              return null;
            });
      }
      try {
        jobExecutionResultFuture.get();
      } catch (Exception e) {
        LOG.warn(getName() + " has finished with an error, ignore it.", e);
      }
      int totalRows = rows;
      LOG.warn("executionException is", executionException.get());
      if (!isStatusListenersNotified) {
        dealOrFailed(
            () -> {
              SqlExecutionException exception = executionException.get();
              if (exception != null) {
                getFlinkStatusListeners()
                    .forEach(
                        listener ->
                            listener.onFailed(
                                ExceptionUtils.getRootCauseMessage(exception),
                                exception,
                                RowsType.Fetched()));
              } else {
                getFlinkStatusListeners()
                    .forEach(listener -> listener.onSuccess(totalRows, RowsType.Fetched()));
              }
              return null;
            });
      }
      // no result anymore
      // either the job is done or an error occurred
      isRunning = false;
    }

    private void dealOrFailed(Supplier<Void> supplier) {
      try {
        supplier.get();
      } catch (Exception e) {
        LOG.error("Listener execute failed!", e);
        isStatusListenersNotified = true;
        getFlinkStatusListeners()
            .forEach(
                listener ->
                    listener.onFailed(
                        ExceptionUtils.getRootCauseMessage(e), e, RowsType.Fetched()));
      }
    }
  }
}
