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

import com.webank.wedatasphere.linkis.engine.flink.client.cluster.MultipleYarnJobClusterExecutor;
import org.apache.flink.api.dag.Pipeline;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.core.execution.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @program: linkis
 * @description: 集群部署 Table program
 * @author: hui zhu
 * @create: 2020-09-21 12:04
 */
public class ProgramDeployer {
	private static final Logger LOG = LoggerFactory.getLogger(ProgramDeployer.class);

	private final Configuration configuration;
	private final Pipeline pipeline;
	private final String jobName;

	/**
	 * Deploys a table program on the cluster.
	 *
	 * @param configuration  the {@link Configuration} that is used for deployment
	 * @param jobName        job name of the Flink job to be submitted
	 * @param pipeline       Flink {@link Pipeline} to execute
	 */
	public ProgramDeployer(
			Configuration configuration,
			String jobName,
			Pipeline pipeline) {
		this.configuration = configuration;
		this.pipeline = pipeline;
		this.jobName = jobName;
	}

	public CompletableFuture<JobClient> deploy() {
		LOG.info("Submitting job {} for query {}`", pipeline, jobName);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Submitting job {} with configuration: \n{}", pipeline, configuration);
		}
		if (configuration.get(DeploymentOptions.TARGET) == null) {
			throw new RuntimeException("No execution.target specified in your configuration file.");
		}
		final PipelineExecutor executor = new MultipleYarnJobClusterExecutor();
		CompletableFuture<JobClient> jobClient;
		try {
			jobClient = executor.execute(pipeline, configuration);
		} catch (Exception e) {
			throw new RuntimeException("Could not execute program.", e);
		}
		return jobClient;
	}
}

