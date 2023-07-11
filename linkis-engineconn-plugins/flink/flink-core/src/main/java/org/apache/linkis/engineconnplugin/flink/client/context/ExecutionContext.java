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

import org.apache.linkis.engineconnplugin.flink.client.config.Environment;
import org.apache.linkis.engineconnplugin.flink.client.factory.LinkisKubernetesClusterClientFactory;
import org.apache.linkis.engineconnplugin.flink.client.factory.LinkisYarnClusterClientFactory;
import org.apache.linkis.engineconnplugin.flink.client.shims.FlinkShims;
import org.apache.linkis.engineconnplugin.flink.client.shims.SessionState;
import org.apache.linkis.engineconnplugin.flink.exception.SqlExecutionException;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.client.ClientUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.kubernetes.KubernetesClusterDescriptor;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.internal.TableEnvironmentInternal;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.delegation.Executor;
import org.apache.flink.table.factories.*;
import org.apache.flink.table.functions.*;
import org.apache.flink.table.module.Module;
import org.apache.flink.util.TemporaryClassLoaderContext;
import org.apache.flink.yarn.YarnClusterDescriptor;

import javax.annotation.Nullable;

import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.engineconnplugin.flink.errorcode.FlinkErrorCodeSummary.*;

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

    //    if (sessionState == null) {
    //      MutableURLClassLoader mutableURLClassLoader =
    //          FlinkUserCodeClassLoaders.create(new URL[0], classLoader, flinkConfig);
    //      final ClientResourceManager resourceManager =
    //          new ClientResourceManager(flinkConfig, mutableURLClassLoader);
    //
    //      final ModuleManager moduleManager = new ModuleManager();
    //
    //      final EnvironmentSettings settings =
    //          EnvironmentSettings.newInstance().withConfiguration(flinkConfig).build();
    //
    //      final CatalogManager catalogManager =
    //          CatalogManager.newBuilder()
    //              .classLoader(classLoader)
    //              .config(flinkConfig)
    //              .defaultCatalog(
    //                  settings.getBuiltInCatalogName(),
    //                  new GenericInMemoryCatalog(
    //                      settings.getBuiltInCatalogName(), settings.getBuiltInDatabaseName()))
    //              .build();
    //
    //      final FunctionCatalog functionCatalog =
    //          new FunctionCatalog(flinkConfig, resourceManager, catalogManager, moduleManager);
    //      this.sessionState =
    //          new SessionState(catalogManager, moduleManager, resourceManager, functionCatalog);
    //    } else {
    //      this.sessionState = sessionState;
    //    }

    //    this.tableEnv = createTableEnvironment();
    this.tableEnv =
        (TableEnvironment)
            flinkShims.createTableEnvironment(
                flinkConfig, streamExecEnv, sessionState, classLoader);
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
    return tableEnv;
  }

  // ------------------------------------------------------------------------------------------------------------------
  // Helper to create Table Environment
  // ------------------------------------------------------------------------------------------------------------------

  //  private StreamTableEnvironment createTableEnvironment() {
  //    EnvironmentSettings settings =
  //        EnvironmentSettings.newInstance().withConfiguration(flinkConfig).build();
  //
  //    streamExecEnv = new StreamExecutionEnvironment(new Configuration(flinkConfig), classLoader);
  //
  //    final Executor executor = lookupExecutor(streamExecEnv, classLoader);
  //
  //    return createStreamTableEnvironment(
  //        streamExecEnv,
  //        settings,
  //        executor,
  //        sessionState.catalogManager,
  //        sessionState.moduleManager,
  //        sessionState.resourceManager,
  //        sessionState.functionCatalog,
  //        classLoader);
  //  }
  //
  //  private static StreamTableEnvironment createStreamTableEnvironment(
  //      StreamExecutionEnvironment env,
  //      EnvironmentSettings settings,
  //      Executor executor,
  //      CatalogManager catalogManager,
  //      ModuleManager moduleManager,
  //      ResourceManager resourceManager,
  //      FunctionCatalog functionCatalog,
  //      ClassLoader userClassLoader) {
  //
  //    TableConfig tableConfig = TableConfig.getDefault();
  //    tableConfig.setRootConfiguration(executor.getConfiguration());
  //    tableConfig.addConfiguration(settings.getConfiguration());
  //
  //    final Planner planner =
  //        PlannerFactoryUtil.createPlanner(
  //            executor, tableConfig, userClassLoader, moduleManager, catalogManager,
  // functionCatalog);
  //
  //    return new StreamTableEnvironmentImpl(
  //        catalogManager,
  //        moduleManager,
  //        resourceManager,
  //        functionCatalog,
  //        tableConfig,
  //        env,
  //        planner,
  //        executor,
  //        settings.isStreamingMode());
  //  }
  //
  //  private static Executor lookupExecutor(
  //      StreamExecutionEnvironment executionEnvironment, ClassLoader userClassLoader) {
  //    try {
  //      final ExecutorFactory executorFactory =
  //          FactoryUtil.discoverFactory(
  //              userClassLoader, ExecutorFactory.class, ExecutorFactory.DEFAULT_IDENTIFIER);
  //      final Method createMethod =
  //          executorFactory.getClass().getMethod("create", StreamExecutionEnvironment.class);
  //
  //      return (Executor) createMethod.invoke(executorFactory, executionEnvironment);
  //    } catch (Exception e) {
  //      throw new TableException(
  //          "Could not instantiate the executor. Make sure a planner module is on the classpath",
  // e);
  //    }
  //  }

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

  private Module createModule(Map<String, String> moduleProperties, ClassLoader classLoader) {
    final ModuleFactory factory =
        TableFactoryService.find(ModuleFactory.class, moduleProperties, classLoader);
    return factory.createModule(moduleProperties);
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
