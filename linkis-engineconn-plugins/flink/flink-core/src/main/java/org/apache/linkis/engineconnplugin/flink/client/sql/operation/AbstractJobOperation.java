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

package org.apache.linkis.engineconnplugin.flink.client.sql.operation;

import org.apache.linkis.engineconnplugin.flink.client.deployment.AbstractSessionClusterDescriptorAdapter;
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.JobExecutionException;
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.SqlExecutionException;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ColumnInfo;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultKind;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet;
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;
import org.apache.linkis.engineconnplugin.flink.listener.FlinkListenerGroupImpl;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.table.types.logical.VarCharType;
import org.apache.flink.types.Row;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.NOT_SUPPORT_TRANSFORM;

/** A default implementation of JobOperation. */
public abstract class AbstractJobOperation extends FlinkListenerGroupImpl implements JobOperation {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractJobOperation.class);

  protected final FlinkEngineConnContext context;
  // clusterDescriptorAdapter is not null only after job is submitted
  private AbstractSessionClusterDescriptorAdapter clusterDescriptorAdapter;
  private volatile JobID jobId;
  protected boolean noMoreResult;

  private volatile boolean isJobCanceled;

  protected final Object lock = new Object();

  public AbstractJobOperation(FlinkEngineConnContext context) {
    this.context = context;
    this.isJobCanceled = false;
    this.noMoreResult = false;
  }

  @Override
  public void setClusterDescriptorAdapter(
      AbstractSessionClusterDescriptorAdapter clusterDescriptor) {
    this.clusterDescriptorAdapter = clusterDescriptor;
  }

  @Override
  public ResultSet execute() throws SqlExecutionException {
    jobId = submitJob();
    clusterDescriptorAdapter.setJobId(jobId);
    String strJobId = jobId.toString();
    return ResultSet.builder()
        .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
        .columns(ColumnInfo.create("jobId", new VarCharType(false, strJobId.length())))
        .data(Row.of(strJobId))
        .build();
  }

  public JobID transformToJobInfo(ResultSet resultSet) throws SqlExecutionException {
    if (resultSet.getColumns().size() != 1) {
      throw new SqlExecutionException(NOT_SUPPORT_TRANSFORM.getErrorDesc());
    }
    Row row = resultSet.getData().get(0);
    return JobID.fromHexString(row.getField(0).toString());
  }

  @Override
  public JobStatus getJobStatus() throws JobExecutionException {
    synchronized (lock) {
      return clusterDescriptorAdapter.getJobStatus();
    }
  }

  @Override
  public void cancelJob() throws JobExecutionException {
    if (isJobCanceled) {
      // just for fast failure
      return;
    }
    synchronized (lock) {
      if (jobId == null) {
        LOG.error("No job has been submitted. This is a bug.");
        throw new IllegalStateException("No job has been submitted. This is a bug.");
      }
      if (isJobCanceled) {
        return;
      }

      cancelJobInternal();
      isJobCanceled = true;
    }
  }

  protected abstract JobID submitJob() throws SqlExecutionException;

  protected void cancelJobInternal() throws JobExecutionException {
    LOG.info("Start to cancel job {} and result retrieval.", getJobId());
    // ignore if there is no more result
    // the job might has finished earlier. it's hard to say whether it need to be canceled,
    // so the clients should be care for the exceptions ???
    if (noMoreResult) {
      return;
    }
    clusterDescriptorAdapter.cancelJob();
  }

  @Override
  public JobID getJobId() {
    if (jobId == null) {
      throw new IllegalStateException("No job has been submitted. This is a bug.");
    }
    return jobId;
  }

  @Override
  public synchronized Optional<ResultSet> getJobResult() throws SqlExecutionException {
    Optional<Tuple2<List<Row>, List<Boolean>>> newResults = fetchJobResults();
    return newResults.map(
        r ->
            ResultSet.builder()
                .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
                .columns(getColumnInfos())
                .data(r.f0)
                .changeFlags(newResults.get().f1)
                .build());
  }

  protected abstract Optional<Tuple2<List<Row>, List<Boolean>>> fetchJobResults()
      throws SqlExecutionException;

  protected abstract List<ColumnInfo> getColumnInfos();
}
