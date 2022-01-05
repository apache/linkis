/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineconnplugin.flink.client.deployment;

import org.apache.linkis.engineconnplugin.flink.client.context.ExecutionContext;
import org.apache.linkis.engineconnplugin.flink.exception.JobExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.client.program.ClusterClient;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YarnPerJobClusterDescriptorAdapter extends ClusterDescriptorAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(YarnPerJobClusterDescriptorAdapter.class);

	YarnPerJobClusterDescriptorAdapter(
			ExecutionContext executionContext) {
		super(executionContext);
	}

	@Override
	public boolean isGloballyTerminalState() throws JobExecutionException {
		boolean isGloballyTerminalState;
		try {
			JobStatus jobStatus = getJobStatus();
			isGloballyTerminalState = jobStatus.isGloballyTerminalState();
		} catch (JobExecutionException e) {
			if (isYarnApplicationStopped(e)) {
				isGloballyTerminalState = true;
			} else {
				throw e;
			}
		}

		return isGloballyTerminalState;
	}

	/**
	 * The yarn application is not running when its final status is not UNDEFINED.
	 *
	 * <p>In this case, it will throw
	 * <code>RuntimeException("The Yarn application " + applicationId + " doesn't run anymore.")</code>
	 * from retrieve method in YarnClusterDescriptor.java
	 */
	private boolean isYarnApplicationStopped(Throwable e) {
		do {
			String exceptionMessage = e.getMessage();
			if (StringUtils.equals(exceptionMessage, "The Yarn application " + clusterID + " doesn't run anymore.")) {
				LOG.info("{} is stopped.", clusterID);
				return true;
			}
			e = e.getCause();
		} while (e != null);
		return false;
	}

    public void deployCluster(JobID jobId, ClusterClient<ApplicationId> clusterClient) throws JobExecutionException {
		this.clusterClient = clusterClient;
		this.setJobId(jobId);
		this.clusterID = clusterClient.getClusterId();
		webInterfaceUrl = clusterClient.getWebInterfaceURL();
		bindApplicationId();
	}

}
