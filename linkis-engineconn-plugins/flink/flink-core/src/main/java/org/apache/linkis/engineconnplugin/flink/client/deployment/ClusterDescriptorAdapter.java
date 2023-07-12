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
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.client.deployment.ClusterRetrieveException;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnConfigOptions;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.util.ConverterUtils;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.*;

/** Cluster Descriptor Adapter, adaptable with datastream/sql tasks(集群交互适配器，适合datastream、sql方式作业) */
public abstract class ClusterDescriptorAdapter implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(ClusterDescriptorAdapter.class);

  public static final long CLIENT_REQUEST_TIMEOUT =
      FlinkEnvConfiguration.FLINK_CLIENT_REQUEST_TIMEOUT().getValue().toLong();

  protected final ExecutionContext executionContext;
  // jobId is not null only after job is submitted
  private JobID jobId;
  protected ApplicationId clusterID;

  protected String kubernetesClusterID;
  protected ClusterClient<ApplicationId> clusterClient;

  protected ClusterClient<String> kubernetesClusterClient;

  private YarnClusterDescriptor clusterDescriptor;

  public void setJobId(JobID jobId) {
    this.jobId = jobId;
  }

  public JobID getJobId() {
    return jobId;
  }

  public ApplicationId getClusterID() {
    return clusterID;
  }

  public String getKubernetesClusterID() {
    return kubernetesClusterID;
  }

  public String getWebInterfaceUrl() {
    return webInterfaceUrl;
  }

  protected String webInterfaceUrl;

  public ClusterDescriptorAdapter(ExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  /** Returns the status of the flink job. */
  public JobStatus getJobStatus() throws JobExecutionException {
    if (jobId == null) {
      throw new JobExecutionException(NO_JOB_SUBMITTED.getErrorDesc());
    }
    return bridgeClientRequest(
        this.executionContext, jobId, () -> clusterClient.getJobStatus(jobId), false);
  }

  /** Cancel the flink job. */
  public void cancelJob() throws JobExecutionException {
    if (jobId == null) {
      LOG.info("No job has been submitted, ignore the method of cancelJob.");
      return;
    }
    LOG.info("Start to cancel job {}.", jobId);
    bridgeClientRequest(this.executionContext, jobId, () -> clusterClient.cancel(jobId), true);
  }

  public String doSavepoint(String savepoint, String mode) throws JobExecutionException {
    LOG.info("try to {} savepoint in path {}.", mode, savepoint);
    Supplier<CompletableFuture<String>> function;
    switch (mode) {
      case "trigger":
        function = () -> executionContext.triggerSavepoint(clusterClient, jobId, savepoint);
        break;
      case "cancel":
        function = () -> executionContext.cancelWithSavepoint(clusterClient, jobId, savepoint);

        break;
      case "stop":
        function = () -> executionContext.stopWithSavepoint(clusterClient, jobId, false, savepoint);
        break;
      default:
        throw new JobExecutionException(NOT_SAVEPOINT_MODE.getErrorDesc() + mode);
    }
    return bridgeClientRequest(this.executionContext, jobId, function, false);
  }

  /**
   * The reason of using ClusterClient instead of JobClient to retrieve a cluster is the JobClient
   * can't know whether the job is finished on yarn-per-job mode.
   *
   * <p>If a job is finished, JobClient always get java.util.concurrent.TimeoutException when
   * getting job status and canceling a job after job is finished. This method will throw
   * org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException when creating a ClusterClient if
   * the job is finished. This is more user-friendly.
   */
  protected <R> R bridgeClientRequest(
      ExecutionContext executionContext,
      JobID jobId,
      Supplier<CompletableFuture<R>> function,
      boolean ignoreError)
      throws JobExecutionException {
    if (clusterClient == null) {
      if (this.clusterID == null) {
        LOG.error("Cluster information don't exist.");
        throw new JobExecutionException(CLUSTER_NOT_EXIST.getErrorDesc());
      }
      clusterDescriptor = executionContext.createClusterDescriptor();
      try {
        clusterClient = clusterDescriptor.retrieve(this.clusterID).getClusterClient();
      } catch (ClusterRetrieveException e) {
        LOG.error(String.format("Job: %s could not retrieve or create a cluster.", jobId), e);
        throw new JobExecutionException(
            String.format("Job: %s could not retrieve or create a cluster.", jobId), e);
      }
    }
    try {
      return function.get().get(CLIENT_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      if (ignoreError) {
        return null;
      } else {
        LOG.error(String.format("Job: %s operation failed!", jobId), e);
        throw new JobExecutionException(String.format("Job: %s operation failed!", jobId), e);
      }
    }
  }

  protected void bindApplicationId() throws JobExecutionException {
    Method method = null;
    try {
      method = StreamExecutionEnvironment.class.getDeclaredMethod("getConfiguration");
    } catch (NoSuchMethodException e) {
      throw new JobExecutionException(NOT_FLINK_VERSION.getErrorDesc(), e);
    }
    method.setAccessible(true);
    Configuration configuration;
    try {
      configuration =
          (Configuration) method.invoke(executionContext.getStreamExecutionEnvironment());
    } catch (Exception e) {
      throw new JobExecutionException(EXECUTE_FAILED.getErrorDesc(), e);
    }
    String applicationId = configuration.getString(YarnConfigOptions.APPLICATION_ID);
    if (StringUtils.isNotBlank(applicationId)) {
      LOG.info(
          "The applicationId {} is exists in StreamExecutionEnvironment, ignore to bind applicationId to StreamExecutionEnvironment.",
          applicationId);
      return;
    }
    applicationId = executionContext.getFlinkConfig().getString(YarnConfigOptions.APPLICATION_ID);
    if (StringUtils.isBlank(applicationId) && this.clusterID == null) {
      throw new JobExecutionException(APPLICATIONID_NOT_EXIST.getErrorDesc());
    } else if (StringUtils.isNotBlank(applicationId)) {
      configuration.setString(YarnConfigOptions.APPLICATION_ID, applicationId);
      LOG.info("Bind applicationId {} to StreamExecutionEnvironment.", applicationId);
    } else {
      configuration.setString(YarnConfigOptions.APPLICATION_ID, ConverterUtils.toString(clusterID));
      LOG.info("Bind applicationId {} to StreamExecutionEnvironment.", clusterID);
    }
  }

  @Override
  public String toString() {
    return "ClusterDescriptorAdapter{" + "jobId=" + jobId + ", clusterID=" + clusterID + '}';
  }

  @Override
  public void close() {
    if (clusterClient != null) {
      clusterClient.shutDownCluster();
      clusterClient.close();
    }
    if (clusterDescriptor != null) {
      clusterDescriptor.close();
    }
  }

  /**
   * Checks whether this job state is <i>globally terminal</i>. A globally terminal job is complete
   * and cannot fail any more and will not be restarted or recovered by another standby master node.
   *
   * <p>When a globally terminal state has been reached, all recovery data for the job is dropped
   * from the high-availability services.
   *
   * @return True, if this job status is globally terminal, false otherwise.
   */
  public abstract boolean isGloballyTerminalState() throws JobExecutionException;

  public boolean isGloballyTerminalStateByYarn() throws JobExecutionException {
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
   * <p>In this case, it will throw <code>
   * RuntimeException("The Yarn application " + applicationId + " doesn't run anymore.")</code> from
   * retrieve method in YarnClusterDescriptor.java
   */
  private boolean isYarnApplicationStopped(Throwable e) {
    do {
      String exceptionMessage = e.getMessage();
      if (StringUtils.equals(
          exceptionMessage, "The Yarn application " + clusterID + " doesn't run anymore.")) {
        LOG.info("{} is stopped.", clusterID);
        return true;
      }
      e = e.getCause();
    } while (e != null);
    return false;
  }
}
