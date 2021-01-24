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

package com.webank.wedatasphere.linkis.engine.flink.client.deployment;

import com.webank.wedatasphere.linkis.engine.flink.client.context.ExecutionContext;

import org.apache.flink.api.common.JobID;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.flink.yarn.executors.YarnJobClusterExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: linkis
 * @description: 集群交互适配器工厂
 * @author: hui zhu
 * @create: 2020-09-21 12:04
 */
public class ClusterDescriptorAdapterFactory {

	private static Logger LOG = LoggerFactory.getLogger(ClusterDescriptorAdapterFactory.class);


	public static <ClusterID> ClusterDescriptorAdapter<ClusterID> create(
			ExecutionContext<ClusterID> executionContext,
			String sessionId,
			JobID jobId) {
		String yarnDeploymentTarget = executionContext.getFlinkConfig().get(DeploymentOptions.TARGET);
 		ClusterDescriptorAdapter<ClusterID> clusterDescriptorAdapter = null;
		if (YarnDeploymentTarget.PER_JOB.getName().equals(yarnDeploymentTarget)) {
			clusterDescriptorAdapter = new YarnPerJobClusterDescriptorAdapter<>(
					executionContext,
					sessionId,
					jobId);
		}
		if (YarnDeploymentTarget.SESSION.getName().equals(yarnDeploymentTarget)) {
			clusterDescriptorAdapter = new SessionClusterDescriptorAdapter<>(
					executionContext,
					sessionId,
					jobId);
		}
		if (YarnDeploymentTarget.APPLICATION.getName().equals(yarnDeploymentTarget)) {
			clusterDescriptorAdapter = new YarnApplicationClusterDescriptorAdapter<>(
					executionContext,
					sessionId,
					jobId);
		}
		return clusterDescriptorAdapter;
	}

}
