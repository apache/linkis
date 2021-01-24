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
import com.webank.wedatasphere.linkis.engine.flink.exception.JobExecutionException;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlExecutionException;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlGatewayException;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.client.deployment.ClusterDescriptor;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.configuration.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * @program: linkis
 * @description: 集群交互适配器，适合datastream、sql方式作业
 * @author: hui zhu
 * @create: 2020-09-21 12:04
 */
public abstract class ClusterDescriptorAdapter<ClusterID> {
	public static final Logger LOG = LoggerFactory.getLogger(ClusterDescriptorAdapter.class);

	private static final int DEFAULT_TIMEOUT_SECONDS = 30;

	protected final ExecutionContext<ClusterID> executionContext;
	// Only used for logging
	private final String sessionId;
	// jobId is not null only after job is submitted
	protected  JobID jobId;
	protected  ClusterID clusterID;

	public JobID getJobId() {
		return jobId;
	}

	public ClusterID getClusterID() {
		return clusterID;
	}

	public String getWebInterfaceUrl() {
		return webInterfaceUrl;
	}

	protected String webInterfaceUrl;

	public ClusterDescriptorAdapter(
			ExecutionContext<ClusterID> executionContext,
			String sessionId,
			JobID jobId) {
		this.executionContext = executionContext;
		this.sessionId = sessionId;
		this.jobId = jobId;
		this.clusterID = executionContext.getClusterClientFactory().getClusterId(executionContext.getFlinkConfig());
	}

	/**
	 * Returns the status of the flink job.
	 */
	public JobStatus getJobStatus(){
		if (jobId == null) {
			LOG.error("Session: {}. No job has been submitted. This is a bug.", sessionId);
			throw new IllegalStateException("No job has been submitted. This is a bug.");
		}
		return bridgeClientRequest(this.executionContext, jobId, sessionId, clusterClient -> {
			try {
				return clusterClient.getJobStatus(jobId).get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				LOG.error(String.format("Session: %s. Failed to fetch job status for job %s", sessionId, jobId), e);
				throw new JobExecutionException("Failed to fetch job status for job " + jobId, e);
			}
		});
	}

	/**
	 * Cancel the flink job.
	 */
	public void cancelJob() {
		if (jobId == null) {
			LOG.error("Session: {}. No job has been submitted. This is a bug.", sessionId);
			throw new IllegalStateException("No job has been submitted. This is a bug.");
		}
		LOG.info("Session: {}. Start to cancel job {}.", sessionId, jobId);
		bridgeClientRequest(this.executionContext, jobId, sessionId, clusterClient -> {
			try {
				clusterClient.cancel(jobId).get();
			} catch (Throwable t) {
				// the job might has finished earlier
			}
			return null;
		});
	}
	
	public  abstract void  deployCluster(String[] programArguments, String applicationClassName);

		/**
         * The reason of using ClusterClient instead of JobClient to retrieve a cluster is
         * the JobClient can't know whether the job is finished on yarn-per-job mode.
         *
         * <p>If a job is finished, JobClient always get java.util.concurrent.TimeoutException
         * when getting job status and canceling a job after job is finished.
         * This method will throw org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException
         * when creating a ClusterClient if the job is finished. This is more user-friendly.
         */
	protected <R> R bridgeClientRequest(
			ExecutionContext<ClusterID> executionContext,
			JobID jobId,
			String sessionId,
			Function<ClusterClient<?>, R> function) {
		if (this.clusterID == null) {
			LOG.error("Session: {}. Cluster information don't exist.", sessionId);
			throw new IllegalStateException("Cluster information don't exist.");
		}
		// stop Flink job
		try (final ClusterDescriptor<ClusterID> clusterDescriptor =
				executionContext.createClusterDescriptor()) {
			try (ClusterClient<ClusterID> clusterClient =
						clusterDescriptor.retrieve(this.clusterID).getClusterClient()) {
				// retrieve existing cluster
				return function.apply(clusterClient);
			} catch (Exception e) {
				LOG.error(
						String.format("Session: %s, job: %s. Could not retrieve or create a cluster.", sessionId, jobId),
						e);
				throw new JobExecutionException("Could not retrieve or create a cluster.", e);
			}
		} catch (JobExecutionException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(
					String.format("Session: %s, job: %s. Could not locate a cluster.", sessionId, jobId), e);
			throw new JobExecutionException("Could not locate a cluster.", e);
		}
	}

	@Override
	public String toString() {
		return "ClusterDescriptorAdapter{" +
				"sessionId='" + sessionId + '\'' +
				", jobId=" + jobId +
				", clusterID=" + clusterID +
				'}';
	}

	/**
	 * Checks whether this job state is <i>globally terminal</i>.
	 * A globally terminal job is complete and cannot fail any more
	 * and will not be restarted or recovered by another standby master node.
	 *
	 * <p>When a globally terminal state has been reached,
	 * all recovery data for the job is dropped from the high-availability services.
	 *
	 * @return True, if this job status is globally terminal, false otherwise.
	 */
	public abstract boolean isGloballyTerminalState();
}
