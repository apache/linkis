/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation;

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment.ClusterDescriptorAdapter;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ColumnInfo;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultKind;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.JobExecutionException;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.SqlExecutionException;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.listener.FlinkListenerGroupImpl;
import java.util.List;
import java.util.Optional;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.table.types.logical.VarCharType;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default implementation of JobOperation.
 */
public abstract class AbstractJobOperation extends FlinkListenerGroupImpl implements JobOperation {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJobOperation.class);

    protected final FlinkEngineConnContext context;
    // clusterDescriptorAdapter is not null only after job is submitted
    protected ClusterDescriptorAdapter clusterDescriptorAdapter;
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
    public ResultSet execute() throws SqlExecutionException, JobExecutionException {
        JobInfo jobInfo = submitJob();
        jobId = jobInfo.getJobId();
        String strJobId = jobId.toString();
        return ResultSet.builder()
            .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
            .columns(
                ColumnInfo.create("jobId", new VarCharType(false, strJobId.length())),
                ColumnInfo.create("applicationId", new VarCharType(false, jobInfo.getApplicationId().length())),
                ColumnInfo.create("webInterfaceUrl", new VarCharType(false, jobInfo.getWebInterfaceUrl().length()))
            )
            .data(Row.of(strJobId, jobInfo.getApplicationId(), jobInfo.getWebInterfaceUrl()))
            .build();
    }

    public JobInfo transformToJobInfo(ResultSet resultSet) throws JobExecutionException {
        if(resultSet.getColumns().size() != 3) {
            throw new JobExecutionException("Not support to transform this resultSet to JobInfo.");
        }
        Row row = resultSet.getData().get(0);
        return new JobInfoImpl(JobID.fromHexString(row.getField(0).toString()),
            row.getField(1).toString(),
            row.getField(2).toString());
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

    protected abstract JobInfo submitJob() throws JobExecutionException, SqlExecutionException;

    protected abstract void cancelJobInternal() throws JobExecutionException;

    @Override
    public JobID getJobId() {
        if (jobId == null) {
            throw new IllegalStateException("No job has been submitted. This is a bug.");
        }
        return jobId;
    }

    @Override
    public synchronized Optional<ResultSet> getJobResult() throws JobExecutionException, SqlExecutionException {
        Optional<Tuple2<List<Row>, List<Boolean>>> newResults = fetchJobResults();
        return newResults.map(r -> ResultSet.builder()
            .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
            .columns(getColumnInfos())
            .data(r.f0)
            .changeFlags(newResults.get().f1)
            .build());
    }

    protected abstract Optional<Tuple2<List<Row>, List<Boolean>>> fetchJobResults() throws JobExecutionException, SqlExecutionException;

    protected abstract List<ColumnInfo> getColumnInfos();


    protected class JobInfoImpl implements JobInfo {

        private JobID jobId;
        private String applicationId, webInterfaceUrl;

        public JobInfoImpl(JobID jobId, String applicationId, String webInterfaceUrl) {
            this.jobId = jobId;
            this.applicationId = applicationId;
            this.webInterfaceUrl = webInterfaceUrl;
        }

        @Override
        public JobID getJobId() {
            return jobId;
        }

        @Override
        public String getApplicationId() {
            return applicationId;
        }

        @Override
        public String getWebInterfaceUrl() {
            return webInterfaceUrl;
        }
    }

}
