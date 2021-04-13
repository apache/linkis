/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engine.flink.client.deployment;

import com.google.common.base.Predicates;
import com.webank.wedatasphere.linkis.engine.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engine.flink.exception.JobSubmitException;
import com.webank.wedatasphere.linkis.engine.flink.util.RetryUtil;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterDescriptor;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liangqilang on 01 20, 2021
 */
public class YarnApplicationClusterDescriptorAdapter<ClusterID> extends ClusterDescriptorAdapter<ClusterID> {

    private static final Logger  LOG                   = LoggerFactory.getLogger(YarnApplicationClusterDescriptorAdapter.class);
    private static final Integer TASK_FAILED_CODE      = -1;
    private static final Integer TASK_RETRY_CODE       = 1;
    private static final Integer TASK_RETRY_BREAK_CODE = 0;
    private final        Pattern containerPattern      = Pattern.compile("(container_e[0-9]*_[0-9]*_[0-9]*_[0-9]*_[0-9]*)");

    public YarnApplicationClusterDescriptorAdapter(ExecutionContext<ClusterID> executionContext, String sessionId, JobID jobId) {
        super(executionContext, sessionId, jobId);
    }

    @Override
    public void deployCluster(String[] programArguments, String applicationClassName) {
        ApplicationConfiguration     applicationConfiguration = new ApplicationConfiguration(programArguments, applicationClassName);
        ClusterSpecification         clusterSpecification     = this.executionContext.getClusterClientFactory().getClusterSpecification(this.executionContext.getFlinkConfig());
        ClusterDescriptor<ClusterID> clusterDescriptor        = this.executionContext.createClusterDescriptor();
        try {
            ClusterClientProvider<ClusterID> clusterClientProvider = clusterDescriptor.deployApplicationCluster(
                    clusterSpecification,
                    applicationConfiguration);
            ClusterClient<ClusterID> clusterClient = clusterClientProvider.getClusterClient();
            super.clusterID       = clusterClient.getClusterId();
            super.webInterfaceUrl = clusterClient.getWebInterfaceURL();
            YarnClusterDescriptor yarnClusterDescriptor = this.executionContext.createYarnClusterDescriptor();

            int resultRetryCode = RetryUtil.retry(() -> {
                        try {
                            YarnClient             yarnClient             = yarnClusterDescriptor.getYarnClient();
                            ApplicationReport      applicationReport      = yarnClient.getApplicationReport(ConverterUtils.toApplicationId(super.clusterID.toString()));
                            YarnApplicationState   applicationState       = applicationReport.getYarnApplicationState();
                            FinalApplicationStatus finalApplicationStatus = applicationReport.getFinalApplicationStatus();
                            //如果status 是 FINISHED  或者 final status 是 FAILED 则表示 任务已经失败
                            if (YarnApplicationState.FINISHED == applicationState
                                    || YarnApplicationState.FAILED == applicationState
                                    || FinalApplicationStatus.FAILED == finalApplicationStatus) {
                                return TASK_FAILED_CODE;
                            }
                        } catch (Exception e) {
                            LOG.info("retry scan application statue error!", e);
                        }
                        CompletableFuture<Collection<JobStatusMessage>> jobs = clusterClient.listJobs();
                        jobs.get().forEach(jobStatusMessage -> {
                            if (Objects.nonNull(jobStatusMessage.getJobId())) {
                                YarnApplicationClusterDescriptorAdapter.super.jobId = jobStatusMessage.getJobId();
                            }
                        });
                        return Objects.nonNull(YarnApplicationClusterDescriptorAdapter.super.jobId) ? TASK_RETRY_BREAK_CODE : TASK_RETRY_CODE;
                    }, Predicates.equalTo(TASK_RETRY_CODE), 10000L, 10000L,
                    TimeUnit.MILLISECONDS, 10000);
            if (resultRetryCode != TASK_RETRY_BREAK_CODE) {
                LOG.error("the task fail to retry get id.", super.clusterID.toString());
            }
        } catch (
                Exception e) {
            throw new JobSubmitException(e.getMessage());
        }
    }

    private String getMatchContainer(String logUrl) {
        Matcher matcher = containerPattern.matcher(logUrl);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return StringUtils.EMPTY;
    }

    @Override
    public boolean isGloballyTerminalState() {
        return false;
    }

}
