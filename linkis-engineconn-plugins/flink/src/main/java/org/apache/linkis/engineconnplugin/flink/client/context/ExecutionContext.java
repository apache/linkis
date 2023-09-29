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
import org.apache.linkis.engineconnplugin.flink.exception.SqlExecutionException;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.client.ClientUtils;
import org.apache.flink.client.program.StreamContextEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.execution.DefaultExecutorServiceLoader;
import org.apache.flink.kubernetes.KubernetesClusterDescriptor;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableConfig;
import org.apache.flink.table.api.TableException;
import org.apache.flink.table.api.bridge.java.BatchTableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.bridge.java.internal.BatchTableEnvironmentImpl;
import org.apache.flink.table.api.bridge.java.internal.StreamTableEnvironmentImpl;
import org.apache.flink.table.api.internal.TableEnvironmentInternal;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.CatalogManager;
import org.apache.flink.table.catalog.FunctionCatalog;
import org.apache.flink.table.catalog.GenericInMemoryCatalog;
import org.apache.flink.table.client.config.entries.*;
import org.apache.flink.table.delegation.Executor;
import org.apache.flink.table.delegation.ExecutorFactory;
import org.apache.flink.table.delegation.Planner;
import org.apache.flink.table.delegation.PlannerFactory;
import org.apache.flink.table.descriptors.CoreModuleDescriptorValidator;
import org.apache.flink.table.factories.*;
import org.apache.flink.table.functions.*;
import org.apache.flink.table.module.Module;
import org.apache.flink.table.module.ModuleManager;
import org.apache.flink.table.sinks.TableSink;
import org.apache.flink.table.sources.TableSource;
import org.apache.flink.util.TemporaryClassLoaderContext;
import org.apache.flink.yarn.YarnClusterDescriptor;

import javax.annotation.Nullable;

import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
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
  private LinkisYarnClusterClientFactory clusterClientFactory;

  private LinkisKubernetesClusterClientFactory kubernetesClusterClientFactory;

  private TableEnvironmentInternal tableEnv;
  private ExecutionEnvironment execEnv;
  private StreamExecutionEnvironment streamExecEnv;
  private Executor executor;

  // Members that should be reused in the same session.
  private SessionState sessionState;

  private ExecutionContext(
      Environment environment,
      @Nullable SessionState sessionState,
      List<URL> dependencies,
      Configuration flinkConfig) {
    this(
        environment,
        sessionState,
        dependencies,
        flinkConfig,
        new LinkisYarnClusterClientFactory(),
        new LinkisKubernetesClusterClientFactory());
  }

  private ExecutionContext(
      Environment environment,
      @Nullable SessionState sessionState,
      List<URL> dependencies,
      Configuration flinkConfig,
      LinkisYarnClusterClientFactory clusterClientFactory) {
    this(
        environment,
        sessionState,
        dependencies,
        flinkConfig,
        clusterClientFactory,
        new LinkisKubernetesClusterClientFactory());
  }

  private ExecutionContext(
      Environment environment,
      @Nullable SessionState sessionState,
      List<URL> dependencies,
      Configuration flinkConfig,
      LinkisYarnClusterClientFactory clusterClientFactory,
      LinkisKubernetesClusterClientFactory linkisKubernetesClusterClientFactory) {
    this.environment = environment;
    this.flinkConfig = flinkConfig;
    this.sessionState = sessionState;
    // create class loader
    if (dependencies == null) {
      dependencies = Collections.emptyList();
    }
    classLoader =
        ClientUtils.buildUserCodeClassLoader(
            dependencies, Collections.emptyList(), this.getClass().getClassLoader(), flinkConfig);
    LOG.debug("Deployment descriptor: {}", environment.getDeployment());
    LOG.info("flinkConfig config: {}", flinkConfig);
    this.clusterClientFactory = clusterClientFactory;
    this.kubernetesClusterClientFactory = linkisKubernetesClusterClientFactory;
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
      return function.apply(getTableEnvironment());
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

  public TableEnvironmentInternal getTableEnvironment() throws SqlExecutionException {
    if (tableEnv == null) {
      synchronized (this) {
        if (tableEnv == null) {
          // Initialize the TableEnvironment.
          initializeTableEnvironment(sessionState);
        }
      }
    }
    return tableEnv;
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
      Configuration configuration) {
    return new Builder(defaultEnv, sessionEnv, dependencies, configuration);
  }

  private Module createModule(Map<String, String> moduleProperties, ClassLoader classLoader) {
    final ModuleFactory factory =
        TableFactoryService.find(ModuleFactory.class, moduleProperties, classLoader);
    return factory.createModule(moduleProperties);
  }

  private Catalog createCatalog(
      String name, Map<String, String> catalogProperties, ClassLoader classLoader) {
    final CatalogFactory factory =
        TableFactoryService.find(CatalogFactory.class, catalogProperties, classLoader);
    return factory.createCatalog(name, catalogProperties);
  }

  private static TableSource<?> createTableSource(
      ExecutionEntry execution, Map<String, String> sourceProperties, ClassLoader classLoader)
      throws SqlExecutionException {
    if (execution.isStreamingPlanner()) {
      final TableSourceFactory<?> factory =
          (TableSourceFactory<?>)
              TableFactoryService.find(TableSourceFactory.class, sourceProperties, classLoader);
      return factory.createTableSource(sourceProperties);
    } else if (execution.isBatchPlanner()) {
      final BatchTableSourceFactory<?> factory =
          (BatchTableSourceFactory<?>)
              TableFactoryService.find(
                  BatchTableSourceFactory.class, sourceProperties, classLoader);
      return factory.createBatchTableSource(sourceProperties);
    }
    throw new SqlExecutionException(SUPPORTED_SOURCES.getErrorDesc());
  }

  private static TableSink<?> createTableSink(
      ExecutionEntry execution, Map<String, String> sinkProperties, ClassLoader classLoader)
      throws SqlExecutionException {
    if (execution.isStreamingPlanner()) {
      final TableSinkFactory<?> factory =
          (TableSinkFactory<?>)
              TableFactoryService.find(TableSinkFactory.class, sinkProperties, classLoader);
      return factory.createTableSink(sinkProperties);
    } else if (execution.isBatchPlanner()) {
      final BatchTableSinkFactory<?> factory =
          (BatchTableSinkFactory<?>)
              TableFactoryService.find(BatchTableSinkFactory.class, sinkProperties, classLoader);
      return factory.createBatchTableSink(sinkProperties);
    }
    throw new SqlExecutionException(SUPPORTED_SINKS.getErrorDesc());
  }

  private TableEnvironmentInternal createStreamTableEnvironment(
      StreamExecutionEnvironment env,
      EnvironmentSettings settings,
      TableConfig config,
      Executor executor,
      CatalogManager catalogManager,
      ModuleManager moduleManager,
      FunctionCatalog functionCatalog) {
    final Map<String, String> plannerProperties = settings.toPlannerProperties();
    final Planner planner =
        ComponentFactoryService.find(PlannerFactory.class, plannerProperties)
            .create(plannerProperties, executor, config, functionCatalog, catalogManager);

    return new StreamTableEnvironmentImpl(
        catalogManager,
        moduleManager,
        functionCatalog,
        config,
        env,
        planner,
        executor,
        settings.isStreamingMode(),
        classLoader);
  }

  private static Executor lookupExecutor(
      Map<String, String> executorProperties, StreamExecutionEnvironment executionEnvironment) {
    try {
      ExecutorFactory executorFactory =
          ComponentFactoryService.find(ExecutorFactory.class, executorProperties);
      Method createMethod =
          executorFactory
              .getClass()
              .getMethod("create", Map.class, StreamExecutionEnvironment.class);

      return (Executor)
          createMethod.invoke(executorFactory, executorProperties, executionEnvironment);
    } catch (Exception e) {
      throw new TableException(
          "Could not instantiate the executor. Make sure a planner module is on the classpath", e);
    }
  }

  private void initializeTableEnvironment(@Nullable SessionState sessionState)
      throws SqlExecutionException {
    final EnvironmentSettings settings = environment.getExecution().getEnvironmentSettings();
    // Step 0.0 Initialize the table configuration.
    final TableConfig config = new TableConfig();
    environment
        .getConfiguration()
        .asMap()
        .forEach((k, v) -> config.getConfiguration().setString(k, v));
    final boolean noInheritedState = sessionState == null;
    if (noInheritedState) {
      // --------------------------------------------------------------------------------------------------------------
      // Step.1 Create environments
      // --------------------------------------------------------------------------------------------------------------
      // Step 1.0 Initialize the ModuleManager if required.
      final ModuleManager moduleManager = new ModuleManager();
      // Step 1.1 Initialize the CatalogManager if required.
      final CatalogManager catalogManager =
          CatalogManager.newBuilder()
              .classLoader(classLoader)
              .config(config.getConfiguration())
              .defaultCatalog(
                  settings.getBuiltInCatalogName(),
                  new GenericInMemoryCatalog(
                      settings.getBuiltInCatalogName(), settings.getBuiltInDatabaseName()))
              .build();
      // Step 1.2 Initialize the FunctionCatalog if required.
      final FunctionCatalog functionCatalog =
          new FunctionCatalog(config, catalogManager, moduleManager);
      // Step 1.4 Set up session state.
      this.sessionState = SessionState.of(catalogManager, moduleManager, functionCatalog);

      // Must initialize the table environment before actually the
      createTableEnvironment(settings, config, catalogManager, moduleManager, functionCatalog);

      // --------------------------------------------------------------------------------------------------------------
      // Step.2 Create modules and load them into the TableEnvironment.
      // --------------------------------------------------------------------------------------------------------------
      // No need to register the modules info if already inherit from the same session.
      Map<String, Module> modules = new LinkedHashMap<>();
      environment
          .getModules()
          .forEach((name, entry) -> modules.put(name, createModule(entry.asMap(), classLoader)));
      if (!modules.isEmpty()) {
        // unload core module first to respect whatever users configure
        tableEnv.unloadModule(CoreModuleDescriptorValidator.MODULE_TYPE_CORE);
        modules.forEach(tableEnv::loadModule);
      }

      // --------------------------------------------------------------------------------------------------------------
      // Step.3 create user-defined functions and temporal tables then register them.
      // --------------------------------------------------------------------------------------------------------------
      // No need to register the functions if already inherit from the same session.
      registerFunctions();

      // --------------------------------------------------------------------------------------------------------------
      // Step.4 Create catalogs and register them.
      // --------------------------------------------------------------------------------------------------------------
      // No need to register the catalogs if already inherit from the same session.
      initializeCatalogs();
    } else {
      // Set up session state.
      this.sessionState = sessionState;
      createTableEnvironment(
          settings,
          config,
          sessionState.catalogManager,
          sessionState.moduleManager,
          sessionState.functionCatalog);
    }
  }

  private void createTableEnvironment(
      EnvironmentSettings settings,
      TableConfig config,
      CatalogManager catalogManager,
      ModuleManager moduleManager,
      FunctionCatalog functionCatalog) {

    // for streaming(流式)
    if (environment.getExecution().isStreamingPlanner()) {
      streamExecEnv = createStreamExecutionEnvironment();
      execEnv = null;
      final Map<String, String> executorProperties = settings.toExecutorProperties();
      executor = lookupExecutor(executorProperties, streamExecEnv);
      tableEnv =
          createStreamTableEnvironment(
              streamExecEnv,
              settings,
              config,
              executor,
              catalogManager,
              moduleManager,
              functionCatalog);
      return;
    }
    // default batch(默认批)
    streamExecEnv = null;
    execEnv = createExecutionEnvironment();
    executor = null;
    tableEnv = new BatchTableEnvironmentImpl(execEnv, config, catalogManager, moduleManager);
  }

  private void initializeCatalogs() throws SqlExecutionException {
    // --------------------------------------------------------------------------------------------------------------
    // Step.1 Create catalogs and register them.
    // --------------------------------------------------------------------------------------------------------------
    wrapClassLoader(
        () ->
            environment
                .getCatalogs()
                .forEach(
                    (name, entry) -> {
                      Catalog catalog = createCatalog(name, entry.asMap(), classLoader);
                      tableEnv.registerCatalog(name, catalog);
                    }));

    // --------------------------------------------------------------------------------------------------------------
    // Step.2 create table sources & sinks, and register them.
    // --------------------------------------------------------------------------------------------------------------
    Map<String, TableSource<?>> tableSources = new HashMap<>();
    Map<String, TableSink<?>> tableSinks = new HashMap<>();
    for (Entry<String, TableEntry> keyValue : environment.getTables().entrySet()) {
      String name = keyValue.getKey();
      TableEntry entry = keyValue.getValue();
      if (entry instanceof SourceTableEntry || entry instanceof SourceSinkTableEntry) {
        tableSources.put(
            name, createTableSource(environment.getExecution(), entry.asMap(), classLoader));
      }
      if (entry instanceof SinkTableEntry || entry instanceof SourceSinkTableEntry) {
        tableSinks.put(
            name, createTableSink(environment.getExecution(), entry.asMap(), classLoader));
      }
    }
    // register table sources
    tableSources.forEach(tableEnv::registerTableSourceInternal);
    // register table sinks
    tableSinks.forEach(tableEnv::registerTableSinkInternal);

    // --------------------------------------------------------------------------------------------------------------
    // Step.4 Register temporal tables.
    // --------------------------------------------------------------------------------------------------------------
    for (Entry<String, TableEntry> keyValue : environment.getTables().entrySet()) {
      TableEntry entry = keyValue.getValue();
      if (entry instanceof TemporalTableEntry) {
        final TemporalTableEntry temporalTableEntry = (TemporalTableEntry) entry;
        registerTemporalTable(temporalTableEntry);
      }
    }

    // --------------------------------------------------------------------------------------------------------------
    // Step.5 Register views in specified order.
    // --------------------------------------------------------------------------------------------------------------
    for (Entry<String, TableEntry> keyValue : environment.getTables().entrySet()) {
      // if registering a view fails at this point,
      // it means that it accesses tables that are not available anymore
      TableEntry entry = keyValue.getValue();
      if (entry instanceof ViewEntry) {
        final ViewEntry viewEntry = (ViewEntry) entry;
        registerView(viewEntry);
      }
    }

    // --------------------------------------------------------------------------------------------------------------
    // Step.6 Set current catalog and database.
    // --------------------------------------------------------------------------------------------------------------
    // Switch to the current catalog.
    Optional<String> catalog = environment.getExecution().getCurrentCatalog();
    catalog.ifPresent(tableEnv::useCatalog);

    // Switch to the current database.
    Optional<String> database = environment.getExecution().getCurrentDatabase();
    database.ifPresent(tableEnv::useDatabase);
  }

  private ExecutionEnvironment createExecutionEnvironment() {
    final ExecutionEnvironment execEnv = ExecutionEnvironment.getExecutionEnvironment();
    execEnv.setRestartStrategy(environment.getExecution().getRestartStrategy());
    execEnv.setParallelism(environment.getExecution().getParallelism().orElse(1));
    return execEnv;
  }

  private StreamExecutionEnvironment createStreamExecutionEnvironment() {
    StreamContextEnvironment.setAsContext(
        new DefaultExecutorServiceLoader(), flinkConfig, classLoader, false, false);
    final StreamExecutionEnvironment env =
        StreamExecutionEnvironment.getExecutionEnvironment(flinkConfig);
    env.setRestartStrategy(environment.getExecution().getRestartStrategy());
    env.setParallelism(environment.getExecution().getParallelism().orElse(1));
    env.setMaxParallelism(environment.getExecution().getMaxParallelism());
    env.setStreamTimeCharacteristic(environment.getExecution().getTimeCharacteristic());
    if (env.getStreamTimeCharacteristic() == TimeCharacteristic.EventTime) {
      env.getConfig()
          .setAutoWatermarkInterval(environment.getExecution().getPeriodicWatermarksInterval());
    }
    return env;
  }

  private void registerFunctions() throws SqlExecutionException {
    Map<String, FunctionDefinition> functions = new LinkedHashMap<>();
    environment
        .getFunctions()
        .forEach(
            (name, entry) -> {
              final UserDefinedFunction function =
                  FunctionService.createFunction(entry.getDescriptor(), classLoader, false);
              functions.put(name, function);
            });
    registerFunctions(functions);
  }

  private void registerFunctions(Map<String, FunctionDefinition> functions)
      throws SqlExecutionException {
    if (tableEnv instanceof StreamTableEnvironment) {
      StreamTableEnvironment streamTableEnvironment = (StreamTableEnvironment) tableEnv;
      for (Entry<String, FunctionDefinition> keyValue : functions.entrySet()) {
        String k = keyValue.getKey();
        FunctionDefinition v = keyValue.getValue();
        if (v instanceof ScalarFunction) {
          streamTableEnvironment.registerFunction(k, (ScalarFunction) v);
        } else if (v instanceof AggregateFunction) {
          streamTableEnvironment.registerFunction(k, (AggregateFunction<?, ?>) v);
        } else if (v instanceof TableFunction) {
          streamTableEnvironment.registerFunction(k, (TableFunction<?>) v);
        } else {
          throw new SqlExecutionException(
              MessageFormat.format(SUPPORTED_FUNCTION_TYPE.getErrorDesc(), v.getClass().getName()));
        }
      }
    } else {
      BatchTableEnvironment batchTableEnvironment = (BatchTableEnvironment) tableEnv;
      for (Entry<String, FunctionDefinition> keyValue : functions.entrySet()) {
        String k = keyValue.getKey();
        FunctionDefinition v = keyValue.getValue();
        if (v instanceof ScalarFunction) {
          batchTableEnvironment.registerFunction(k, (ScalarFunction) v);
        } else if (v instanceof AggregateFunction) {
          batchTableEnvironment.registerFunction(k, (AggregateFunction<?, ?>) v);
        } else if (v instanceof TableFunction) {
          batchTableEnvironment.registerFunction(k, (TableFunction<?>) v);
        } else {
          throw new SqlExecutionException(
              MessageFormat.format(SUPPORTED_FUNCTION_TYPE.getErrorDesc(), v.getClass().getName()));
        }
      }
    }
  }

  private void registerView(ViewEntry viewEntry) throws SqlExecutionException {
    try {
      tableEnv.registerTable(viewEntry.getName(), tableEnv.sqlQuery(viewEntry.getQuery()));
    } catch (Exception e) {
      throw new SqlExecutionException(
          "Invalid view '"
              + viewEntry.getName()
              + "' with query:\n"
              + viewEntry.getQuery()
              + "\nCause: "
              + e.getMessage());
    }
  }

  private void registerTemporalTable(TemporalTableEntry temporalTableEntry)
      throws SqlExecutionException {
    try {
      final Table table = tableEnv.scan(temporalTableEntry.getHistoryTable());
      final TableFunction<?> function =
          table.createTemporalTableFunction(
              temporalTableEntry.getTimeAttribute(),
              String.join(",", temporalTableEntry.getPrimaryKeyFields()));
      if (tableEnv instanceof StreamTableEnvironment) {
        StreamTableEnvironment streamTableEnvironment = (StreamTableEnvironment) tableEnv;
        streamTableEnvironment.registerFunction(temporalTableEntry.getName(), function);
      } else {
        BatchTableEnvironment batchTableEnvironment = (BatchTableEnvironment) tableEnv;
        batchTableEnvironment.registerFunction(temporalTableEntry.getName(), function);
      }
    } catch (Exception e) {
      throw new SqlExecutionException(
          "Invalid temporal table '"
              + temporalTableEntry.getName()
              + "' over table '"
              + temporalTableEntry.getHistoryTable()
              + ".\nCause: "
              + e.getMessage());
    }
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
    private LinkisYarnClusterClientFactory clusterClientFactory;

    // Optional members.
    @Nullable private SessionState sessionState;

    private Builder(
        Environment defaultEnv,
        @Nullable Environment sessionEnv,
        List<URL> dependencies,
        Configuration configuration) {
      this.defaultEnv = defaultEnv;
      this.sessionEnv = sessionEnv;
      this.dependencies = dependencies;
      this.configuration = configuration;
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
            this.configuration);
      } else {
        return new ExecutionContext(
            this.currentEnv == null ? Environment.merge(defaultEnv, sessionEnv) : this.currentEnv,
            this.sessionState,
            this.dependencies,
            this.configuration,
            this.clusterClientFactory);
      }
    }
  }

  /** Represents the state that should be reused in one session. * */
  public static class SessionState {
    public final CatalogManager catalogManager;
    public final ModuleManager moduleManager;
    public final FunctionCatalog functionCatalog;

    private SessionState(
        CatalogManager catalogManager,
        ModuleManager moduleManager,
        FunctionCatalog functionCatalog) {
      this.catalogManager = catalogManager;
      this.moduleManager = moduleManager;
      this.functionCatalog = functionCatalog;
    }

    public static SessionState of(
        CatalogManager catalogManager,
        ModuleManager moduleManager,
        FunctionCatalog functionCatalog) {
      return new SessionState(catalogManager, moduleManager, functionCatalog);
    }
  }
}
