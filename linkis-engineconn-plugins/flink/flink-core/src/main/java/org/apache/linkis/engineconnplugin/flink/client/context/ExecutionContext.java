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

package org.apache.linkis.engineconnplugin.flink.client.context;

import org.apache.linkis.engineconnplugin.flink.client.factory.LinkisKubernetesClusterClientFactory;
import org.apache.linkis.engineconnplugin.flink.client.factory.LinkisYarnClusterClientFactory;
import org.apache.linkis.engineconnplugin.flink.client.shims.FlinkShims;
import org.apache.linkis.engineconnplugin.flink.client.shims.SessionState;
import org.apache.linkis.engineconnplugin.flink.client.shims.config.Environment;
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.SqlExecutionException;
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.client.ClientUtils;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.StreamContextEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.execution.DefaultExecutorServiceLoader;
import org.apache.flink.kubernetes.KubernetesClusterDescriptor;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.internal.TableEnvironmentInternal;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.delegation.Executor;
import org.apache.flink.util.TemporaryClassLoaderContext;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.hadoop.yarn.api.records.ApplicationId;

import javax.annotation.Nullable;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionContext {

  private static final Logger LOG = LoggerFactory.getLogger(ExecutionContext.class);

  private final Environment environment;
  private final ClassLoader classLoader;

  private final Configuration flinkConfig;

  private final String flinkVersion;

  private FlinkShims flinkShims;
  private LinkisYarnClusterClientFactory clusterClientFactory;

  private LinkisKubernetesClusterClientFactory kubernetesClusterClientFactory;

  private TableEnvironment tableEnv;
  private ExecutionEnvironment execEnv;
  private StreamExecutionEnvironment streamExecEnv;
  private Executor executor;

  // Members that should be reused in the same session.
  private SessionState sessionState;

  private ExecutionContext(
      Environment environment,
      @Nullable SessionState sessionState,
      List<URL> dependencies,
      Configuration flinkConfig,
      String flinkVersion) {
    this(
        environment,
        sessionState,
        dependencies,
        flinkConfig,
        new LinkisYarnClusterClientFactory(),
        new LinkisKubernetesClusterClientFactory(),
        flinkVersion);
  }

  private ExecutionContext(
      Environment environment,
      @Nullable SessionState sessionState,
      List<URL> dependencies,
      Configuration flinkConfig,
      LinkisYarnClusterClientFactory clusterClientFactory,
      String flinkVersion) {
    this(
        environment,
        sessionState,
        dependencies,
        flinkConfig,
        clusterClientFactory,
        new LinkisKubernetesClusterClientFactory(),
        flinkVersion);
  }

  private ExecutionContext(
      Environment environment,
      @Nullable SessionState sessionState,
      List<URL> dependencies,
      Configuration flinkConfig,
      LinkisYarnClusterClientFactory clusterClientFactory,
      LinkisKubernetesClusterClientFactory linkisKubernetesClusterClientFactory,
      String flinkVersion) {
    this.flinkVersion = flinkVersion;

    try {
      this.flinkShims = FlinkShims.getInstance(flinkVersion);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    classLoader =
        ClientUtils.buildUserCodeClassLoader(
            dependencies, Collections.emptyList(), this.getClass().getClassLoader(), flinkConfig);
    this.environment = environment;
    this.flinkConfig = flinkConfig;
    this.sessionState = sessionState;

    // create class loader
    if (dependencies == null) {
      dependencies = Collections.emptyList();
    }
    LOG.debug("Deployment descriptor: {}", environment.getDeployment());
    LOG.info("flinkConfig config: {}", flinkConfig);
    this.clusterClientFactory = clusterClientFactory;
    this.kubernetesClusterClientFactory = linkisKubernetesClusterClientFactory;
  }

  public TableEnvironment getTableEnvironment() {
    if (tableEnv == null) {
      synchronized (this) {
        if (tableEnv == null) {
          if (this.flinkVersion.equals(FlinkEnvConfiguration.FLINK_1_12_2_VERSION())) {
            // Initialize the TableEnvironment.
            this.streamExecEnv = createStreamExecutionEnvironment();
            try {
              this.tableEnv =
                  (TableEnvironment)
                      flinkShims.initializeTableEnvironment(
                          environment, flinkConfig, streamExecEnv, sessionState, classLoader);
            } catch (SqlExecutionException e) {
              throw new RuntimeException(e);
            }
          } else if (this.flinkVersion.equals(FlinkEnvConfiguration.FLINK_1_16_2_VERSION())) {
            this.streamExecEnv =
                new StreamExecutionEnvironment(new Configuration(flinkConfig), classLoader);
            this.tableEnv =
                (TableEnvironment)
                    flinkShims.createTableEnvironment(
                        flinkConfig, streamExecEnv, sessionState, classLoader);
          } else {
            throw new RuntimeException(
                "Unsupported flink versions, Currently  only 1.12.2 and 1.16.2 are supported");
          }
        }
      }
    }
    return tableEnv;
  }

  private StreamExecutionEnvironment createStreamExecutionEnvironment() {
    StreamContextEnvironment.setAsContext(
        new DefaultExecutorServiceLoader(), flinkConfig, classLoader, false, false);
    final StreamExecutionEnvironment env =
        StreamExecutionEnvironment.getExecutionEnvironment(flinkConfig);
    env.setRestartStrategy(environment.getExecution().getRestartStrategy());
    env.setParallelism(environment.getExecution().getParallelism());
    env.setMaxParallelism(environment.getExecution().getMaxParallelism());
    env.setStreamTimeCharacteristic(environment.getExecution().getTimeCharacteristic());
    if (env.getStreamTimeCharacteristic() == TimeCharacteristic.EventTime) {
      env.getConfig()
          .setAutoWatermarkInterval(environment.getExecution().getPeriodicWatermarksInterval());
    }
    return env;
  }

  public StreamExecutionEnvironment getStreamExecutionEnvironment() throws SqlExecutionException {
    if (streamExecEnv == null) {
      getTableEnvironment();
    }
    return streamExecEnv;
  }

  public void setString(String key, String value) {
    this.flinkConfig.setString(key, value);
  }

  public void setBoolean(String key, boolean value) {
    this.flinkConfig.setBoolean(key, value);
  }

  public Configuration getFlinkConfig() {
    return flinkConfig;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public YarnClusterDescriptor createClusterDescriptor() {
    return clusterClientFactory.createClusterDescriptor(this.flinkConfig);
  }

  public KubernetesClusterDescriptor createKubernetesClusterDescriptor() {
    return kubernetesClusterClientFactory.createClusterDescriptor(this.flinkConfig);
  }

  public Map<String, Catalog> getCatalogs() {
    Map<String, Catalog> catalogs = new HashMap<>();
    for (String name : tableEnv.listCatalogs()) {
      tableEnv.getCatalog(name).ifPresent(c -> catalogs.put(name, c));
    }
    return catalogs;
  }

  public SessionState getSessionState() {
    return this.sessionState;
  }

  /**
   * Executes the given supplier using the execution context's classloader as thread classloader.
   */
  public <R> R wrapClassLoader(Supplier<R> supplier) {
    try (TemporaryClassLoaderContext ignored = TemporaryClassLoaderContext.of(classLoader)) {
      return supplier.get();
    }
  }

  public <R> R wrapClassLoader(Function<TableEnvironmentInternal, R> function)
      throws SqlExecutionException {
    try (TemporaryClassLoaderContext ignored = TemporaryClassLoaderContext.of(classLoader)) {
      return function.apply((TableEnvironmentInternal) getTableEnvironment());
    }
  }

  /**
   * Executes the given Runnable using the execution context's classloader as thread classloader.
   */
  void wrapClassLoader(Runnable runnable) {
    try (TemporaryClassLoaderContext ignored = TemporaryClassLoaderContext.of(classLoader)) {
      runnable.run();
    }
  }

  public ExecutionConfig getExecutionConfig() {
    if (streamExecEnv != null) {
      return streamExecEnv.getConfig();
    } else {
      return execEnv.getConfig();
    }
  }

  public LinkisYarnClusterClientFactory getClusterClientFactory() {
    return clusterClientFactory;
  }

  /** Returns a builder for this {@link ExecutionContext}. */
  public static Builder builder(
      Environment defaultEnv,
      Environment sessionEnv,
      List<URL> dependencies,
      Configuration configuration,
      String flinkVersion) {
    return new Builder(defaultEnv, sessionEnv, dependencies, configuration, flinkVersion);
  }

  public ExecutionContext cloneExecutionContext(Builder builder) {
    ExecutionContext newExecutionContext =
        builder.clusterClientFactory(clusterClientFactory).build();
    if (this.tableEnv != null) {
      newExecutionContext.tableEnv = tableEnv;
      newExecutionContext.execEnv = execEnv;
      newExecutionContext.streamExecEnv = streamExecEnv;
      newExecutionContext.executor = executor;
    }
    return newExecutionContext;
  }

  public CompletableFuture<String> triggerSavepoint(
      ClusterClient<ApplicationId> clusterClient, JobID jobId, String savepoint) {
    return flinkShims.triggerSavepoint(clusterClient, jobId, savepoint);
  }

  public CompletableFuture<String> cancelWithSavepoint(
      ClusterClient<ApplicationId> clusterClient, JobID jobId, String savepoint) {
    return flinkShims.cancelWithSavepoint(clusterClient, jobId, savepoint);
  }

  public CompletableFuture<String> stopWithSavepoint(
      ClusterClient<ApplicationId> clusterClient,
      JobID jobId,
      boolean advanceToEndOfEventTime,
      String savepoint) {
    return flinkShims.stopWithSavepoint(clusterClient, jobId, advanceToEndOfEventTime, savepoint);
  }

  // ~ Inner Class -------------------------------------------------------------------------------

  /** Builder for {@link ExecutionContext}. */
  public static class Builder {
    // Required members.
    private final Environment sessionEnv;
    private final List<URL> dependencies;
    private final Configuration configuration;
    private Environment defaultEnv;
    private Environment currentEnv;
    private String flinkVersion;

    private LinkisYarnClusterClientFactory clusterClientFactory;

    // Optional members.
    @Nullable private SessionState sessionState;

    private Builder(
        Environment defaultEnv,
        @Nullable Environment sessionEnv,
        List<URL> dependencies,
        Configuration configuration,
        String flinkVersion) {
      this.defaultEnv = defaultEnv;
      this.sessionEnv = sessionEnv;
      this.dependencies = dependencies;
      this.configuration = configuration;
      this.flinkVersion = flinkVersion;
    }

    public Builder env(Environment environment) {
      this.currentEnv = environment;
      return this;
    }

    public Builder sessionState(SessionState sessionState) {
      this.sessionState = sessionState;
      return this;
    }

    Builder clusterClientFactory(LinkisYarnClusterClientFactory clusterClientFactory) {
      this.clusterClientFactory = clusterClientFactory;
      return this;
    }

    public ExecutionContext build() {
      if (sessionEnv == null) {
        this.currentEnv = defaultEnv;
      }
      if (clusterClientFactory == null) {
        return new ExecutionContext(
            this.currentEnv == null ? Environment.merge(defaultEnv, sessionEnv) : this.currentEnv,
            this.sessionState,
            this.dependencies,
            this.configuration,
            this.flinkVersion);
      } else {
        return new ExecutionContext(
            this.currentEnv == null ? Environment.merge(defaultEnv, sessionEnv) : this.currentEnv,
            this.sessionState,
            this.dependencies,
            this.configuration,
            this.clusterClientFactory,
            this.flinkVersion);
      }
    }
  }
}
