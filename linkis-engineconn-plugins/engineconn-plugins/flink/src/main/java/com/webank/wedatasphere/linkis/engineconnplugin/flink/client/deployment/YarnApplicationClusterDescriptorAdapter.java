package com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment;

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.ExecutorInitException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.JobExecutionException;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.hadoop.yarn.api.records.ApplicationId;

/**
 * @program: flink-parent
 * @description:
 */
public class YarnApplicationClusterDescriptorAdapter extends ClusterDescriptorAdapter  {

    public YarnApplicationClusterDescriptorAdapter(ExecutionContext executionContext, JobID jobId) {
        super(executionContext, jobId);
    }

    @Override
    public void deployCluster(String[] programArguments, String applicationClassName) throws JobExecutionException {
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(programArguments,applicationClassName);
        ClusterSpecification clusterSpecification  = this.executionContext.getClusterClientFactory().getClusterSpecification(this.executionContext.getFlinkConfig());
        YarnClusterDescriptor clusterDescriptor =  this.executionContext.createClusterDescriptor();
        try {
            ClusterClientProvider<ApplicationId> clusterClientProvider = clusterDescriptor.deployApplicationCluster(
                    clusterSpecification,
                    applicationConfiguration);
            clusterClient = clusterClientProvider.getClusterClient();
            super.clusterID  = clusterClient.getClusterId();
            super.webInterfaceUrl = clusterClient.getWebInterfaceURL();
        } catch (Exception e) {
            throw new JobExecutionException(e.getMessage());
        }

    }

    public boolean initJobId() throws Exception {
        clusterClient.listJobs().thenAccept(list -> list.forEach(jobStatusMessage -> {
            if (Objects.nonNull(jobStatusMessage.getJobId())) {
                jobId = jobStatusMessage.getJobId();
            }
        })).get(CLIENT_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        return null != jobId;
    }


    @Override
    public boolean isGloballyTerminalState() {
        return false;
    }

}
