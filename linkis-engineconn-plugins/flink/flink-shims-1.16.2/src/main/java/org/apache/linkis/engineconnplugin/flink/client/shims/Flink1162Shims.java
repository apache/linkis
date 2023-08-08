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

package org.apache.linkis.engineconnplugin.flink.client.shims;

import org.apache.flink.api.common.JobID;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.execution.SavepointFormatType;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableConfig;
import org.apache.flink.table.api.TableException;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.bridge.java.internal.StreamTableEnvironmentImpl;
import org.apache.flink.table.catalog.CatalogManager;
import org.apache.flink.table.catalog.FunctionCatalog;
import org.apache.flink.table.catalog.GenericInMemoryCatalog;
import org.apache.flink.table.client.resource.ClientResourceManager;
import org.apache.flink.table.delegation.Executor;
import org.apache.flink.table.delegation.ExecutorFactory;
import org.apache.flink.table.delegation.Planner;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.factories.PlannerFactoryUtil;
import org.apache.flink.table.module.ModuleManager;
import org.apache.flink.table.resource.ResourceManager;
import org.apache.flink.util.FlinkUserCodeClassLoaders;
import org.apache.flink.util.MutableURLClassLoader;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class Flink1162Shims extends FlinkShims {

  public Flink1162Shims(String flinkVersion) {
    super(flinkVersion);
  }

  public Object createTableEnvironment(
      Object flinkConfig, Object streamExecEnv, Object sessionState, ClassLoader classLoader) {
    Configuration flinkConfigConfiguration = (Configuration) flinkConfig;
    SessionState sessionStateByFlink = (SessionState) sessionState;
    if (sessionStateByFlink == null) {
      MutableURLClassLoader mutableURLClassLoader =
          FlinkUserCodeClassLoaders.create(new URL[0], classLoader, flinkConfigConfiguration);
      final ClientResourceManager resourceManager =
          new ClientResourceManager(flinkConfigConfiguration, mutableURLClassLoader);

      final ModuleManager moduleManager = new ModuleManager();

      final EnvironmentSettings settings =
          EnvironmentSettings.newInstance().withConfiguration(flinkConfigConfiguration).build();

      final CatalogManager catalogManager =
          CatalogManager.newBuilder()
              .classLoader(classLoader)
              .config(flinkConfigConfiguration)
              .defaultCatalog(
                  settings.getBuiltInCatalogName(),
                  new GenericInMemoryCatalog(
                      settings.getBuiltInCatalogName(), settings.getBuiltInDatabaseName()))
              .build();

      final FunctionCatalog functionCatalog =
          new FunctionCatalog(
              flinkConfigConfiguration, resourceManager, catalogManager, moduleManager);
      sessionStateByFlink =
          new SessionState(catalogManager, moduleManager, resourceManager, functionCatalog);
    }

    EnvironmentSettings settings =
        EnvironmentSettings.newInstance().withConfiguration(flinkConfigConfiguration).build();

    if (streamExecEnv == null) {
      streamExecEnv =
          new StreamExecutionEnvironment(new Configuration(flinkConfigConfiguration), classLoader);
    }

    final Executor executor =
        lookupExecutor((StreamExecutionEnvironment) streamExecEnv, classLoader);

    return createStreamTableEnvironment(
        (StreamExecutionEnvironment) streamExecEnv,
        settings,
        executor,
        sessionStateByFlink.catalogManager,
        sessionStateByFlink.moduleManager,
        sessionStateByFlink.resourceManager,
        sessionStateByFlink.functionCatalog,
        classLoader);
  }

  @Override
  public CompletableFuture<String> triggerSavepoint(
      Object clusterClientObject, Object jobIdObject, String savepoint) {
    ClusterClient clusterClient = (ClusterClient) clusterClientObject;
    return clusterClient.triggerSavepoint(
        (JobID) jobIdObject, savepoint, SavepointFormatType.CANONICAL);
  }

  @Override
  public CompletableFuture<String> cancelWithSavepoint(
      Object clusterClientObject, Object jobIdObject, String savepoint) {
    ClusterClient clusterClient = (ClusterClient) clusterClientObject;
    return clusterClient.cancelWithSavepoint(
        (JobID) jobIdObject, savepoint, SavepointFormatType.CANONICAL);
  }

  @Override
  public CompletableFuture<String> stopWithSavepoint(
      Object clusterClientObject,
      Object jobIdObject,
      boolean advanceToEndOfEventTime,
      String savepoint) {
    ClusterClient clusterClient = (ClusterClient) clusterClientObject;
    return clusterClient.stopWithSavepoint(
        (JobID) jobIdObject, advanceToEndOfEventTime, savepoint, SavepointFormatType.CANONICAL);
  }

  private static StreamTableEnvironment createStreamTableEnvironment(
      StreamExecutionEnvironment env,
      EnvironmentSettings settings,
      Executor executor,
      CatalogManager catalogManager,
      ModuleManager moduleManager,
      ResourceManager resourceManager,
      FunctionCatalog functionCatalog,
      ClassLoader userClassLoader) {

    TableConfig tableConfig = TableConfig.getDefault();
    tableConfig.setRootConfiguration(executor.getConfiguration());
    tableConfig.addConfiguration(settings.getConfiguration());

    final Planner planner =
        PlannerFactoryUtil.createPlanner(
            executor, tableConfig, userClassLoader, moduleManager, catalogManager, functionCatalog);

    return new StreamTableEnvironmentImpl(
        catalogManager,
        moduleManager,
        resourceManager,
        functionCatalog,
        tableConfig,
        env,
        planner,
        executor,
        settings.isStreamingMode());
  }

  private static Executor lookupExecutor(
      StreamExecutionEnvironment executionEnvironment, ClassLoader userClassLoader) {
    try {
      final ExecutorFactory executorFactory =
          FactoryUtil.discoverFactory(
              userClassLoader, ExecutorFactory.class, ExecutorFactory.DEFAULT_IDENTIFIER);
      final Method createMethod =
          executorFactory.getClass().getMethod("create", StreamExecutionEnvironment.class);

      return (Executor) createMethod.invoke(executorFactory, executionEnvironment);
    } catch (Exception e) {
      throw new TableException(
          "Could not instantiate the executor. Make sure a planner module is on the classpath", e);
    }
  }
}
