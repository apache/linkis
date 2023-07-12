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

package org.apache.linkis.engineconnplugin.flink.client.deployment;

import org.apache.linkis.engineconnplugin.flink.client.context.ExecutionContext;
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.JobExecutionException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterDescriptor;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.program.ClusterClientProvider;

public class YarnSessionClusterDescriptorAdapter extends AbstractSessionClusterDescriptorAdapter {

  public YarnSessionClusterDescriptorAdapter(ExecutionContext executionContext) {
    super(executionContext);
  }

  @Override
  public boolean isGloballyTerminalState() throws JobExecutionException {
    return super.isGloballyTerminalStateByYarn();
  }

  public void deployCluster() throws JobExecutionException {
    try {
      ClusterSpecification clusterSpecification =
          executionContext
              .getClusterClientFactory()
              .getClusterSpecification(executionContext.getFlinkConfig());
      ClusterDescriptor clusterDescriptor = executionContext.createClusterDescriptor();
      ClusterClientProvider clusterClientProvider =
          clusterDescriptor.deploySessionCluster(clusterSpecification);
      clusterClient = clusterClientProvider.getClusterClient();
      clusterID = clusterClient.getClusterId();
      webInterfaceUrl = clusterClient.getWebInterfaceURL();
      bindApplicationId();
    } catch (Exception e) {
      throw new JobExecutionException(ExceptionUtils.getRootCauseMessage(e), e);
    }
  }

  @Override
  public void setJobId(JobID jobId) {
    super.setJobId(jobId);
  }
}
