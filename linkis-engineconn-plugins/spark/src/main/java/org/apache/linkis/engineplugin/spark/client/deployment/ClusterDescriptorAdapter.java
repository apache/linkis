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

package org.apache.linkis.engineplugin.spark.client.deployment;

import org.apache.linkis.engineplugin.spark.client.context.ExecutionContext;

import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClusterDescriptorAdapter implements Closeable {
  protected static final Logger logger = LoggerFactory.getLogger(ClusterDescriptorAdapter.class);

  protected final ExecutionContext executionContext;
  protected String applicationId;
  protected SparkLauncher sparkLauncher;
  protected SparkAppHandle sparkAppHandle;
  protected SparkAppHandle.State jobState;

  public String getApplicationId() {
    return applicationId;
  }

  public ClusterDescriptorAdapter(ExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  /** Returns the state of the spark job. */
  public SparkAppHandle.State getJobState() {
    return jobState;
  }

  /** Cancel the spark job. */
  public void cancelJob() {
    if (sparkAppHandle != null) {
      logger.info("Start to cancel job {}.", sparkAppHandle.getAppId());
      this.stopJob();
    } else {
      logger.warn("Cancel job: sparkAppHandle is null");
    }
  }

  @Override
  public String toString() {
    return "ClusterDescriptorAdapter{" + "applicationId=" + sparkAppHandle.getAppId() + '}';
  }

  @Override
  public void close() {
    if (sparkAppHandle != null) {
      logger.info("Start to close job {}.", sparkAppHandle.getAppId());
      this.stopJob();
    } else {
      logger.warn("Close job: sparkAppHandle is null");
    }
  }

  private void stopJob() {
    if (sparkAppHandle == null) {
      return;
    }
    if (sparkAppHandle.getState().isFinal()) {
      logger.info("Job has finished, stop action return.");
      return;
    }
    try {
      logger.info("Try to stop job {}.", sparkAppHandle.getAppId());
      sparkAppHandle.stop();
    } catch (Exception e) {
      logger.error("Stop job failed.", e);
      try {
        logger.info("Try to kill job {}.", sparkAppHandle.getAppId());
        sparkAppHandle.kill();
      } catch (Exception ex) {
        logger.error("Kill job failed.", ex);
      }
    }
  }
}
