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

package com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment;

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration;
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.JobExecutionException;
import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.client.deployment.ClusterRetrieveException;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cluster Descriptor Adapter, adaptable with datastream/sql tasks(集群交互适配器，适合datastream、sql方式作业)
 */
public abstract class ClusterDescriptorAdapter implements Closeable {
	private static final Logger LOG = LoggerFactory.getLogger(ClusterDescriptorAdapter.class);

	public static final long CLIENT_REQUEST_TIMEOUT = FlinkEnvConfiguration.FLINK_CLIENT_REQUEST_TIMEOUT().getValue().toLong();

	protected final ExecutionContext executionContext;
	// jobId is not null only after job is submitted
	private JobID jobId;
	protected ApplicationId clusterID;
	protected ClusterClient<ApplicationId> clusterClient;
	private YarnClusterDescriptor clusterDescriptor;

	public void setJobId(JobID jobId){
		this.jobId = jobId;
	}

	public JobID getJobId() {
		return jobId;
	}

	public ApplicationId getClusterID() {
		return clusterID;
	}

	public String getWebInterfaceUrl() {
		return webInterfaceUrl;
	}

	protected String webInterfaceUrl;

	public ClusterDescriptorAdapter(
			ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	/**
	 * Returns the status of the flink job.
	 */
	public JobStatus getJobStatus() throws JobExecutionException {
		if (jobId == null) {
			throw new JobExecutionException("No job has been submitted. This is a bug.");
		}
		return bridgeClientRequest(this.executionContext, jobId, () -> clusterClient.getJobStatus(jobId), false);
	}

	/**
	 * Cancel the flink job.
	 */
	public void cancelJob() throws JobExecutionException {
		if (jobId == null) {
			LOG.info("No job has been submitted, ignore the method of cancelJob.");
			return;
		}
		LOG.info("Start to cancel job {}.", jobId);
		bridgeClientRequest(this.executionContext, jobId, () -> clusterClient.cancel(jobId), true);
	}

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
			ExecutionContext executionContext,
			JobID jobId,
		 	Supplier<CompletableFuture<R>> function, boolean ignoreError) throws JobExecutionException {
		if(clusterClient == null) {
			if (this.clusterID == null) {
				LOG.error("Cluster information don't exist.");
				throw new JobExecutionException("Cluster information don't exist.");
			}
			clusterDescriptor = executionContext.createClusterDescriptor();
			try {
				clusterClient = clusterDescriptor.retrieve(this.clusterID).getClusterClient();
			} catch (ClusterRetrieveException e) {
				LOG.error(String.format("Job: %s could not retrieve or create a cluster.", jobId), e);
				throw new JobExecutionException(String.format("Job: %s could not retrieve or create a cluster.", jobId), e);
			}
		}
		try {
			return function.get().get(CLIENT_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if(ignoreError) {
				return null;
			} else {
				LOG.error(String.format("Job: %s operation failed!", jobId), e);
				throw new JobExecutionException(String.format("Job: %s operation failed!", jobId), e);
			}
		}
	}

	@Override
	public String toString() {
		return "ClusterDescriptorAdapter{" +
				"jobId=" + jobId +
				", clusterID=" + clusterID +
				'}';
	}

	@Override
	public void close() {
		if(clusterClient != null) {
			clusterClient.shutDownCluster();
			clusterClient.close();
		}
		if(clusterDescriptor != null) {
			clusterDescriptor.close();
		}
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
	public abstract boolean isGloballyTerminalState() throws JobExecutionException;
}
