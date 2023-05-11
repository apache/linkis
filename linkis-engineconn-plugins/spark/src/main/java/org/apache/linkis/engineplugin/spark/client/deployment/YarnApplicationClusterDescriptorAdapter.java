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
import org.apache.linkis.engineplugin.spark.client.context.SparkConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.launcher.CustomSparkSubmitLauncher;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class YarnApplicationClusterDescriptorAdapter extends ClusterDescriptorAdapter {

  public YarnApplicationClusterDescriptorAdapter(ExecutionContext executionContext) {
    super(executionContext);
  }

  public void deployCluster(String mainClass, String args, Map<String, String> confMap)
      throws IOException {
    SparkConfig sparkConfig = executionContext.getSparkConfig();

    sparkLauncher = new CustomSparkSubmitLauncher();
    // region set args
    sparkLauncher
        .setJavaHome(sparkConfig.getJavaHome())
        .setSparkHome(sparkConfig.getSparkHome())
        .setMaster(sparkConfig.getMaster())
        .setDeployMode(sparkConfig.getDeployMode())
        .setAppName(sparkConfig.getAppName())
        .setVerbose(true);
    sparkLauncher.setConf("spark.app.name", sparkConfig.getAppName());
    if (confMap != null) confMap.forEach((k, v) -> sparkLauncher.setConf(k, v));
    addSparkArg(sparkLauncher, "--jars", sparkConfig.getJars());
    addSparkArg(sparkLauncher, "--packages", sparkConfig.getPackages());
    addSparkArg(sparkLauncher, "--exclude-packages", sparkConfig.getExcludePackages());
    addSparkArg(sparkLauncher, "--repositories", sparkConfig.getRepositories());
    addSparkArg(sparkLauncher, "--files", sparkConfig.getFiles());
    addSparkArg(sparkLauncher, "--archives", sparkConfig.getArchives());
    addSparkArg(sparkLauncher, "--driver-memory", sparkConfig.getDriverMemory());
    addSparkArg(sparkLauncher, "--driver-java-options", sparkConfig.getDriverJavaOptions());
    addSparkArg(sparkLauncher, "--driver-library-path", sparkConfig.getDriverLibraryPath());
    addSparkArg(sparkLauncher, "--driver-class-path", sparkConfig.getDriverClassPath());
    addSparkArg(sparkLauncher, "--executor-memory", sparkConfig.getExecutorMemory());
    addSparkArg(sparkLauncher, "--proxy-user", sparkConfig.getProxyUser());
    addSparkArg(sparkLauncher, "--driver-cores", sparkConfig.getDriverCores().toString());
    addSparkArg(sparkLauncher, "--total-executor-cores", sparkConfig.getTotalExecutorCores());
    addSparkArg(sparkLauncher, "--executor-cores", sparkConfig.getExecutorCores().toString());
    addSparkArg(sparkLauncher, "--num-executors", sparkConfig.getNumExecutors().toString());
    addSparkArg(sparkLauncher, "--principal", sparkConfig.getPrincipal());
    addSparkArg(sparkLauncher, "--keytab", sparkConfig.getKeytab());
    addSparkArg(sparkLauncher, "--queue", sparkConfig.getQueue());
    sparkLauncher.setAppResource(sparkConfig.getAppResource());
    sparkLauncher.setMainClass(mainClass);
    Arrays.stream(args.split("\\s+"))
        .filter(StringUtils::isNotBlank)
        .forEach(arg -> sparkLauncher.addAppArgs(arg));
    sparkAppHandle =
        sparkLauncher.startApplication(
            new SparkAppHandle.Listener() {
              @Override
              public void stateChanged(SparkAppHandle sparkAppHandle) {
                jobState = sparkAppHandle.getState();
                // print log when state change
                if (sparkAppHandle.getAppId() != null) {
                  logger.info(
                      "{} stateChanged: {}", sparkAppHandle.getAppId(), jobState.toString());
                } else {
                  logger.info("stateChanged: {}", jobState.toString());
                }
              }

              @Override
              public void infoChanged(SparkAppHandle sparkAppHandle) {}
            });
    sparkLauncher.setSparkAppHandle(sparkAppHandle);
  }

  private void addSparkArg(SparkLauncher sparkLauncher, String key, String value) {
    if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
      sparkLauncher.addSparkArg(key, value);
    }
  }

  public boolean initJobId() {
    this.applicationId = sparkAppHandle.getAppId();
    // When the job is not finished, the appId is monitored; otherwise, the status is
    // monitored(当任务没结束时，监控appId，反之，则监控状态，这里主要防止任务过早结束，导致一直等待)
    return null != getApplicationId() || (jobState != null && jobState.isFinal());
  }
}
