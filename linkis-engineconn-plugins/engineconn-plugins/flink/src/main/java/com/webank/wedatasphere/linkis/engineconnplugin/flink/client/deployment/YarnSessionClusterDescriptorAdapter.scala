package com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.context.ExecutionContext
import org.apache.flink.api.common.JobID


class YarnSessionClusterDescriptorAdapter(executionContext: ExecutionContext) extends YarnPerJobClusterDescriptorAdapter(executionContext) {

  def deployCluster(): Unit = {
    val clusterSpecification = this.executionContext.getClusterClientFactory.getClusterSpecification(this.executionContext.getFlinkConfig)
    val clusterDescriptor = this.executionContext.createClusterDescriptor
    val clusterClientProvider = clusterDescriptor.deploySessionCluster(clusterSpecification)
    clusterClient = clusterClientProvider.getClusterClient
    clusterID = clusterClient.getClusterId
    webInterfaceUrl = clusterClient.getWebInterfaceURL
  }

  override def setJobId(jobId: JobID): Unit = super.setJobId(jobId)

}
