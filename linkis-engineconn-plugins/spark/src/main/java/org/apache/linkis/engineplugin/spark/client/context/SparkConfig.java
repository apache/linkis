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

  private String k8sConfigFile;

  private String k8sServiceAccount;

  private String k8sMasterUrl;

  private String k8sUsername;

  private String k8sPassword;

  private String k8sImage;

  private String k8sImagePullPolicy;

  private String k8sLanguageType;

  private String k8sRestartPolicy;

  private String k8sSparkVersion;

  private String k8sNamespace;
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

  public String getK8sImagePullPolicy() {
    return k8sImagePullPolicy;
  }

  public void setK8sImagePullPolicy(String k8sImagePullPolicy) {
    this.k8sImagePullPolicy = k8sImagePullPolicy;
  }

  public String getK8sLanguageType() {
    return k8sLanguageType;
  }

  public void setK8sLanguageType(String k8sLanguageType) {
    this.k8sLanguageType = k8sLanguageType;
  }

  public String getK8sRestartPolicy() {
    return k8sRestartPolicy;
  }

  public void setK8sRestartPolicy(String k8sRestartPolicy) {
    this.k8sRestartPolicy = k8sRestartPolicy;
  }

  public String getK8sSparkVersion() {
    return k8sSparkVersion;
  }

  public void setK8sSparkVersion(String k8sSparkVersion) {
    this.k8sSparkVersion = k8sSparkVersion;
  }

  public String getK8sNamespace() {
    return k8sNamespace;
  }

  public void setK8sNamespace(String k8sNamespace) {
    this.k8sNamespace = k8sNamespace;
  }

  public String getK8sMasterUrl() {
    return k8sMasterUrl;
  }

  public String getK8sConfigFile() {
    return k8sConfigFile;
  }

  public void setK8sConfigFile(String k8sConfigFile) {
    this.k8sConfigFile = k8sConfigFile;
  }

  public String getK8sServiceAccount() {
    return k8sServiceAccount;
  }

  public void setK8sServiceAccount(String k8sServiceAccount) {
    this.k8sServiceAccount = k8sServiceAccount;
  }

  public void setK8sMasterUrl(String k8sMasterUrl) {
    this.k8sMasterUrl = k8sMasterUrl;
  }

  public String getK8sUsername() {
    return k8sUsername;
  }

  public void setK8sUsername(String k8sUsername) {
    this.k8sUsername = k8sUsername;
  }

  public String getK8sPassword() {
    return k8sPassword;
  }

  public void setK8sPassword(String k8sPassword) {
    this.k8sPassword = k8sPassword;
  }

  public String getK8sImage() {
    return k8sImage;
  }

  public void setK8sImage(String k8sImage) {
    this.k8sImage = k8sImage;
  }

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
        + ", k8sConfigFile='"
        + k8sConfigFile
        + '\''
        + ", k8sServiceAccount='"
        + k8sServiceAccount
        + '\''
        + ", k8sMasterUrl='"
        + k8sMasterUrl
        + '\''
        + ", k8sImage='"
        + k8sImage
        + '\''
        + ", k8sImagePullPolicy='"
        + k8sImagePullPolicy
        + '\''
        + ", k8sLanguageType='"
        + k8sLanguageType
        + '\''
        + ", k8sRestartPolicy='"
        + k8sRestartPolicy
        + '\''
        + ", k8sSparkVersion='"
        + k8sSparkVersion
        + '\''
        + ", k8sNamespace='"
        + k8sNamespace
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
