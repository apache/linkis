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
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.client.ClientUtils;

import java.util.Map;

/**
 * Handle job actions based on session mode.
 */
public class SessionClusterDescriptorAdapter<ClusterID> extends ClusterDescriptorAdapter<ClusterID> {
	public SessionClusterDescriptorAdapter(
			ExecutionContext<ClusterID> executionContext,
			String sessionId,
			JobID jobId) {
		super(executionContext, sessionId, jobId);
	}


	@Override
	public void deployCluster(String[] programArguments, String applicationClassName) {

	}

	@Override
	public boolean isGloballyTerminalState() {
		JobStatus jobStatus = getJobStatus();
		return jobStatus.isGloballyTerminalState();
	}
}
