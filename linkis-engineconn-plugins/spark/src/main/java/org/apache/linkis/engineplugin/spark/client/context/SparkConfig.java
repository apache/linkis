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

package org.apache.linkis.engineplugin.spark.client.context;

import java.util.HashMap;
import java.util.Map;

public class SparkConfig {

  private String javaHome; // ("")
  private String sparkHome; // ("")
  private String master = "yarn"; // ("yarn")
  private String deployMode = "client"; // ("client") // todo cluster
  private String appResource; // ("")
  private String appName; // ("")
  private String jars; // ("--jars", "")
  private String packages; // ("--packages", "")
  private String excludePackages; // ("--exclude-packages", "")
  private String repositories; // ("--repositories", "")
  private String files; // ("--files", "")
  private String archives; // ("--archives", "")
  private Map<String, String> conf = new HashMap<>(); // ("", "")
  private String propertiesFile; // ("")
  private String driverMemory; // ("--driver-memory", "")
  private String driverJavaOptions; // ("--driver-java-options", "")
  private String driverLibraryPath; // ("--driver-library-path", "")
  private String driverClassPath; // ("--driver-class-path", "")
  private String executorMemory; // ("--executor-memory", "")
  private String proxyUser; // ("--proxy-user", "")
  private boolean verbose = false; // (false)
  private Integer driverCores; // ("--driver-cores", "") // Cluster deploy mode only
  private String totalExecutorCores; // ("--total-executor-cores", "")
  private Integer executorCores; // ("--executor-cores", "")
  private Integer numExecutors; // ("--num-executors", "")
  private String principal; // ("--principal", "")
  private String keytab; // ("--keytab", "")
  private String queue; // ("--queue", "")

  public String getJavaHome() {
    return javaHome;
  }

  public void setJavaHome(String javaHome) {
    this.javaHome = javaHome;
  }

  public String getSparkHome() {
    return sparkHome;
  }

  public void setSparkHome(String sparkHome) {
    this.sparkHome = sparkHome;
  }

  public String getMaster() {
    return master;
  }

  public void setMaster(String master) {
    this.master = master;
  }

  public String getDeployMode() {
    return deployMode;
  }

  public void setDeployMode(String deployMode) {
    this.deployMode = deployMode;
  }

  public String getAppResource() {
    return appResource;
  }

  public void setAppResource(String appResource) {
    this.appResource = appResource;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getJars() {
    return jars;
  }

  public void setJars(String jars) {
    this.jars = jars;
  }

  public String getPackages() {
    return packages;
  }

  public void setPackages(String packages) {
    this.packages = packages;
  }

  public String getExcludePackages() {
    return excludePackages;
  }

  public void setExcludePackages(String excludePackages) {
    this.excludePackages = excludePackages;
  }

  public String getRepositories() {
    return repositories;
  }

  public void setRepositories(String repositories) {
    this.repositories = repositories;
  }

  public String getFiles() {
    return files;
  }

  public void setFiles(String files) {
    this.files = files;
  }

  public String getArchives() {
    return archives;
  }

  public void setArchives(String archives) {
    this.archives = archives;
  }

  public Map<String, String> getConf() {
    return conf;
  }

  public void setConf(Map<String, String> conf) {
    this.conf = conf;
  }

  public String getPropertiesFile() {
    return propertiesFile;
  }

  public void setPropertiesFile(String propertiesFile) {
    this.propertiesFile = propertiesFile;
  }

  public String getDriverMemory() {
    return driverMemory;
  }

  public void setDriverMemory(String driverMemory) {
    this.driverMemory = driverMemory;
  }

  public String getDriverJavaOptions() {
    return driverJavaOptions;
  }

  public void setDriverJavaOptions(String driverJavaOptions) {
    this.driverJavaOptions = driverJavaOptions;
  }

  public String getDriverLibraryPath() {
    return driverLibraryPath;
  }

  public void setDriverLibraryPath(String driverLibraryPath) {
    this.driverLibraryPath = driverLibraryPath;
  }

  public String getDriverClassPath() {
    return driverClassPath;
  }

  public void setDriverClassPath(String driverClassPath) {
    this.driverClassPath = driverClassPath;
  }

  public String getExecutorMemory() {
    return executorMemory;
  }

  public void setExecutorMemory(String executorMemory) {
    this.executorMemory = executorMemory;
  }

  public String getProxyUser() {
    return proxyUser;
  }

  public void setProxyUser(String proxyUser) {
    this.proxyUser = proxyUser;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public Integer getDriverCores() {
    return driverCores;
  }

  public void setDriverCores(Integer driverCores) {
    this.driverCores = driverCores;
  }

  public String getTotalExecutorCores() {
    return totalExecutorCores;
  }

  public void setTotalExecutorCores(String totalExecutorCores) {
    this.totalExecutorCores = totalExecutorCores;
  }

  public Integer getExecutorCores() {
    return executorCores;
  }

  public void setExecutorCores(Integer executorCores) {
    this.executorCores = executorCores;
  }

  public Integer getNumExecutors() {
    return numExecutors;
  }

  public void setNumExecutors(Integer numExecutors) {
    this.numExecutors = numExecutors;
  }

  public String getPrincipal() {
    return principal;
  }

  public void setPrincipal(String principal) {
    this.principal = principal;
  }

  public String getKeytab() {
    return keytab;
  }

  public void setKeytab(String keytab) {
    this.keytab = keytab;
  }

  public String getQueue() {
    return queue;
  }

  public void setQueue(String queue) {
    this.queue = queue;
  }

  @Override
  public String toString() {
    return "SparkConfig{"
        + "javaHome='"
        + javaHome
        + '\''
        + ", sparkHome='"
        + sparkHome
        + '\''
        + ", master='"
        + master
        + '\''
        + ", deployMode='"
        + deployMode
        + '\''
        + ", appName='"
        + appName
        + '\''
        + ", jars='"
        + jars
        + '\''
        + ", packages='"
        + packages
        + '\''
        + ", excludePackages='"
        + excludePackages
        + '\''
        + ", repositories='"
        + repositories
        + '\''
        + ", files='"
        + files
        + '\''
        + ", archives='"
        + archives
        + '\''
        + ", conf="
        + conf
        + ", propertiesFile='"
        + propertiesFile
        + '\''
        + ", driverMemory='"
        + driverMemory
        + '\''
        + ", driverJavaOptions='"
        + driverJavaOptions
        + '\''
        + ", driverLibraryPath='"
        + driverLibraryPath
        + '\''
        + ", driverClassPath='"
        + driverClassPath
        + '\''
        + ", executorMemory='"
        + executorMemory
        + '\''
        + ", proxyUser='"
        + proxyUser
        + '\''
        + ", verbose="
        + verbose
        + ", driverCores="
        + driverCores
        + ", totalExecutorCores='"
        + totalExecutorCores
        + '\''
        + ", executorCores="
        + executorCores
        + ", numExecutors="
        + numExecutors
        + ", principal='"
        + principal
        + '\''
        + ", keytab='"
        + keytab
        + '\''
        + ", queue='"
        + queue
        + '\''
        + '}';
  }
}
