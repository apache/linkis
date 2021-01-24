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

package com.webank.wedatasphere.linkis.engine.flink.client.sql.operation;

import com.webank.wedatasphere.linkis.engine.flink.client.context.SessionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.deployment.ClusterDescriptorAdapter;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ColumnInfo;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultKind;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlGatewayException;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A default implementation of JobOperation.
 */
public abstract class AbstractJobOperation implements JobOperation {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJobOperation.class);

    protected final SessionContext context;
    // clusterDescriptorAdapter is not null only after job is submitted
    protected ClusterDescriptorAdapter<?> clusterDescriptorAdapter;
    protected final String sessionId;
    protected volatile JobID jobId;
    protected boolean noMoreResult;

    private volatile boolean isJobCanceled;

    protected final Object lock = new Object();

    public AbstractJobOperation(SessionContext context) {
        this.context = context;
        this.sessionId = context.getSessionId();
        this.isJobCanceled = false;
        this.noMoreResult = false;
    }

    @Override
    public JobStatus getJobStatus() {
        synchronized (lock) {
            return clusterDescriptorAdapter.getJobStatus();
        }
    }

    @Override
    public void cancelJob() {
        if (isJobCanceled) {
            // just for fast failure
            return;
        }
        synchronized (lock) {
            if (jobId == null) {
                LOG.error("Session: {}. No job has been submitted. This is a bug.", sessionId);
                throw new IllegalStateException("No job has been submitted. This is a bug.");
            }
            if (isJobCanceled) {
                return;
            }

            cancelJobInternal();
            isJobCanceled = true;
        }
    }

    protected abstract void cancelJobInternal();

    protected String getJobName(String statement) {
        return String.format("%s:%s", sessionId, statement);
    }

    @Override
    public JobID getJobId() {
        if (jobId == null) {
            throw new IllegalStateException("No job has been submitted. This is a bug.");
        }
        return jobId;
    }

    @Override
    public synchronized Optional<ResultSet> getJobResult() {
        Optional<Tuple2<List<Row>, List<Boolean>>> newResults = fetchJobResults();
        if (!newResults.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(
                ResultSet.builder()
                        .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
                        .columns(getColumnInfos())
                        .data(newResults.get().f0)
                        .changeFlags(newResults.get().f1)
                        .build()
        );
    }

    protected abstract Optional<Tuple2<List<Row>, List<Boolean>>> fetchJobResults();

    protected abstract List<ColumnInfo> getColumnInfos();

}
