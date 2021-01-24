package com.webank.wedatasphere.linkis.engine.flink.client.deployment;

import com.google.common.base.Predicates;
import com.webank.wedatasphere.linkis.engine.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engine.flink.exception.JobSubmitException;
import com.webank.wedatasphere.linkis.engine.flink.util.RetryUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterDeploymentException;
import org.apache.flink.client.deployment.ClusterDescriptor;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.hadoop.yarn.api.records.ApplicationId;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @program: flink-parent
 * @description:
 * @author: hui zhu
 * @create: 2020-12-16 19:02
 */
public class  YarnApplicationClusterDescriptorAdapter<ClusterID> extends ClusterDescriptorAdapter<ClusterID>  {
    public YarnApplicationClusterDescriptorAdapter(ExecutionContext<ClusterID> executionContext, String sessionId, JobID jobId) {
        super(executionContext, sessionId, jobId);
    }

    @Override
    public void deployCluster(String[] programArguments, String applicationClassName) {
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(programArguments,applicationClassName);
        ClusterSpecification  clusterSpecification  = this.executionContext.getClusterClientFactory().getClusterSpecification(this.executionContext.getFlinkConfig());
        ClusterDescriptor<ClusterID> clusterDescriptor =  this.executionContext.createClusterDescriptor();
        try {
            ClusterClientProvider<ClusterID> clusterClientProvider = clusterDescriptor.deployApplicationCluster(
                    clusterSpecification,
                    applicationConfiguration);
            ClusterClient<ClusterID> clusterClient =clusterClientProvider.getClusterClient();
            super.clusterID  = clusterClient.getClusterId();
            super.webInterfaceUrl = clusterClient.getWebInterfaceURL();
            boolean resultRetry = RetryUtil.retry(new Callable<Boolean>() {
                                                  @Override
                                                  public Boolean call() throws Exception {
                                                      CompletableFuture<Collection<JobStatusMessage>> jobs = clusterClient.listJobs();
                                                      jobs.get().forEach(jobStatusMessage -> {
                                                          if (Objects.nonNull(jobStatusMessage.getJobId())) {
                                                              YarnApplicationClusterDescriptorAdapter.super.jobId = jobStatusMessage.getJobId();
                                                          }
                                                      });
                                                      return null!=YarnApplicationClusterDescriptorAdapter.super.jobId?true:false;
                                                  }
                                              }, Predicates.equalTo(false), 2000L, 10000L,
                    TimeUnit.MILLISECONDS,5);

            if (!resultRetry){
                LOG.error("the task fail to retry get id.",super.clusterID.toString());
            }
        } catch (Exception e) {
            throw new JobSubmitException(e.getMessage());
        }

    }


    @Override
    public boolean isGloballyTerminalState() {
        return false;
    }

}
