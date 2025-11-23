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

package org.apache.seatunnel.core.spark;

import org.apache.linkis.engineconnplugin.seatunnel.util.SeatunnelUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.seatunnel.api.env.EnvCommonOptions;
import org.apache.seatunnel.common.Constants;
import org.apache.seatunnel.common.config.Common;
import org.apache.seatunnel.common.config.DeployMode;
import org.apache.seatunnel.core.starter.Starter;
import org.apache.seatunnel.core.starter.enums.EngineType;
import org.apache.seatunnel.core.starter.enums.PluginType;
import org.apache.seatunnel.core.starter.spark.SeaTunnelSpark;
import org.apache.seatunnel.core.starter.spark.args.SparkCommandArgs;
import org.apache.seatunnel.core.starter.utils.CommandLineUtils;
import org.apache.seatunnel.core.starter.utils.CompressionUtils;
import org.apache.seatunnel.core.starter.utils.ConfigBuilder;
import org.apache.seatunnel.plugin.discovery.PluginIdentifier;
import org.apache.seatunnel.plugin.discovery.seatunnel.SeaTunnelSinkPluginDiscovery;
import org.apache.seatunnel.plugin.discovery.seatunnel.SeaTunnelSourcePluginDiscovery;
import org.apache.seatunnel.shade.com.typesafe.config.Config;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigFactory;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigResolveOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.UnixStyleUsageFormatter;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;

public class SparkV2Starter implements Starter {
  public static final Log logger = LogFactory.getLog(SparkV2Starter.class.getName());

  private static final int USAGE_EXIT_CODE = 234;

  private static final int PLUGIN_LIB_DIR_DEPTH = 3;

  /** original commandline args */
  protected String[] args;

  /** args parsed from {@link #args} */
  protected SparkCommandArgs commandArgs;

  /** the spark application name */
  protected String appName;

  /** jars to include on the spark driver and executor classpaths */
  protected List<Path> jars = new ArrayList<>();

  /** files to be placed in the working directory of each spark executor */
  protected List<Path> files = new ArrayList<>();

  /** spark configuration properties */
  protected Map<String, String> sparkConf;

  private SparkV2Starter(String[] args, SparkCommandArgs commandArgs) {
    this.args = args;
    this.commandArgs = commandArgs;
  }

  @SuppressWarnings("checkstyle:RegexpSingleline")
  public static int main(String[] args) {
    int exitCode = 0;
    logger.info("starter start");
    try {
      SparkV2Starter starter = getInstance(args);
      List<String> command = starter.buildCommands();
      String commandVal = String.join(" ", command);
      logger.info("sparkV2starter commandVal:" + commandVal);
      exitCode = SeatunnelUtils.executeLine(commandVal);
    } catch (Exception e) {
      exitCode = 1;
      logger.error("\n\nsparkV2Starter error:\n" + e);
    }
    return exitCode;
  }

  /**
   * method to get SparkStarter instance, will return {@link ClusterModeSparkStarter} or {@link
   * ClientModeSparkStarter} depending on deploy mode.
   */
  static SparkV2Starter getInstance(String[] args) {
    SparkCommandArgs commandArgs =
        CommandLineUtils.parse(
            args, new SparkCommandArgs(), EngineType.SPARK2.getStarterShellName(), true);
    DeployMode deployMode = commandArgs.getDeployMode();
    switch (deployMode) {
      case CLUSTER:
        return new ClusterModeSparkStarter(args, commandArgs);
      case CLIENT:
        return new ClientModeSparkStarter(args, commandArgs);
      default:
        throw new IllegalArgumentException("Deploy mode " + deployMode + " not supported");
    }
  }

  /** parse commandline args */
  private static SparkCommandArgs parseCommandArgs(String[] args) {
    SparkCommandArgs commandArgs = new SparkCommandArgs();
    JCommander commander =
        JCommander.newBuilder()
            .programName("start-seatunnel-spark.sh")
            .addObject(commandArgs)
            .args(args)
            .build();
    if (commandArgs.isHelp()) {
      commander.setUsageFormatter(new UnixStyleUsageFormatter(commander));
      commander.usage();
      System.exit(USAGE_EXIT_CODE);
    }
    return commandArgs;
  }

  @Override
  public List<String> buildCommands() throws IOException {
    setSparkConf();
    logger.info("setSparkConf start");
    logger.info(commandArgs.getDeployMode().toString());
    Common.setDeployMode(commandArgs.getDeployMode());
    Common.setStarter(true);
    this.jars.addAll(Common.getPluginsJarDependencies());
    this.jars.addAll(Common.getLibJars());
    this.jars.addAll(getConnectorJarDependencies());
    this.jars.addAll(
        new ArrayList<>(
            Common.getThirdPartyJars(sparkConf.getOrDefault(EnvCommonOptions.JARS.key(), ""))));
    this.appName = this.sparkConf.getOrDefault("spark.app.name", Constants.LOGO);
    logger.info("buildFinal end");
    return buildFinal();
  }

  /** parse spark configurations from SeaTunnel config file */
  private void setSparkConf() throws FileNotFoundException {
    commandArgs.getVariables().stream()
        .filter(Objects::nonNull)
        .map(variable -> variable.split("=", 2))
        .filter(pair -> pair.length == 2)
        .forEach(pair -> System.setProperty(pair[0], pair[1]));
    this.sparkConf = getSparkConf(commandArgs.getConfigFile());
    String driverJavaOpts = this.sparkConf.getOrDefault("spark.driver.extraJavaOptions", "");
    String executorJavaOpts = this.sparkConf.getOrDefault("spark.executor.extraJavaOptions", "");
    if (!commandArgs.getVariables().isEmpty()) {
      String properties =
          commandArgs.getVariables().stream().map(v -> "-D" + v).collect(Collectors.joining(" "));
      driverJavaOpts += " " + properties;
      executorJavaOpts += " " + properties;
      this.sparkConf.put("spark.driver.extraJavaOptions", driverJavaOpts.trim());
      this.sparkConf.put("spark.executor.extraJavaOptions", executorJavaOpts.trim());
    }
  }

  /** Get spark configurations from SeaTunnel job config file. */
  static Map<String, String> getSparkConf(String configFile) throws FileNotFoundException {
    File file = new File(configFile);
    if (!file.exists()) {
      throw new FileNotFoundException("config file '" + file + "' does not exists!");
    }
    Config appConfig =
        ConfigFactory.parseFile(file)
            .resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true))
            .resolveWith(
                ConfigFactory.systemProperties(),
                ConfigResolveOptions.defaults().setAllowUnresolved(true));

    return appConfig.getConfig("env").entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().unwrapped().toString()));
  }

  /** return plugin's dependent jars, which located in 'plugins/${pluginName}/lib/*'. */
  private List<Path> getPluginsJarDependencies() throws IOException {
    Path pluginRootDir = Common.pluginRootDir();
    if (!Files.exists(pluginRootDir) || !Files.isDirectory(pluginRootDir)) {
      return Collections.emptyList();
    }
    try (Stream<Path> stream = Files.walk(pluginRootDir, PLUGIN_LIB_DIR_DEPTH, FOLLOW_LINKS)) {
      return stream
          .filter(it -> pluginRootDir.relativize(it).getNameCount() == PLUGIN_LIB_DIR_DEPTH)
          .filter(it -> it.getParent().endsWith("lib"))
          .filter(it -> it.getFileName().toString().endsWith("jar"))
          .collect(Collectors.toList());
    }
  }

  /** return connector's jars, which located in 'connectors/spark/*'. 2.3.0改为链接seatunnel中 */
  private List<Path> getConnectorJarDependencies() {
    Path pluginRootDir = Common.connectorJarDir("seatunnel");
    if (!Files.exists(pluginRootDir) || !Files.isDirectory(pluginRootDir)) {
      return Collections.emptyList();
    }
    Config config = ConfigBuilder.of(commandArgs.getConfigFile());
    Set<URL> pluginJars = new HashSet<>();
    SeaTunnelSourcePluginDiscovery seaTunnelSourcePluginDiscovery =
        new SeaTunnelSourcePluginDiscovery();
    SeaTunnelSinkPluginDiscovery seaTunnelSinkPluginDiscovery = new SeaTunnelSinkPluginDiscovery();
    pluginJars.addAll(
        seaTunnelSourcePluginDiscovery.getPluginJarPaths(
            getPluginIdentifiers(config, PluginType.SOURCE)));
    pluginJars.addAll(
        seaTunnelSinkPluginDiscovery.getPluginJarPaths(
            getPluginIdentifiers(config, PluginType.SINK)));
    List<Path> connectPaths =
        pluginJars.stream()
            .map(url -> new File(url.getPath()).toPath())
            .collect(Collectors.toList());
    logger.info("getConnector jar paths:" + connectPaths.toString());
    return connectPaths;
  }

  private List<PluginIdentifier> getPluginIdentifiers(Config config, PluginType... pluginTypes) {
    return Arrays.stream(pluginTypes)
        .flatMap(
            (Function<PluginType, Stream<PluginIdentifier>>)
                pluginType -> {
                  List<? extends Config> configList = config.getConfigList(pluginType.getType());
                  return configList.stream()
                      .map(
                          pluginConfig ->
                              PluginIdentifier.of(
                                  "seatunnel",
                                  pluginType.getType(),
                                  pluginConfig.getString("plugin_name")));
                })
        .collect(Collectors.toList());
  }

  /** list jars in given directory */
  private List<Path> listJars(Path dir) throws IOException {
    try (Stream<Path> stream = Files.list(dir)) {
      return stream
          .filter(it -> !Files.isDirectory(it))
          .filter(it -> it.getFileName().endsWith("jar"))
          .collect(Collectors.toList());
    }
  }

  /** build final spark-submit commands */
  protected List<String> buildFinal() {
    List<String> commands = new ArrayList<>();
    commands.add(System.getenv("SPARK_HOME") + "/bin/spark-submit");
    appendOption(commands, "--class", SeaTunnelSpark.class.getName());
    appendOption(commands, "--name", this.appName);
    appendOption(commands, "--master", this.commandArgs.getMaster());
    appendOption(commands, "--deploy-mode", this.commandArgs.getDeployMode().getDeployMode());
    appendJars(commands, this.jars);
    appendFiles(commands, this.files);
    appendSparkConf(commands, this.sparkConf);
    appendAppJar(commands);
    appendArgs(commands, args);
    if (this.commandArgs.isCheckConfig()) {
      commands.add("--check");
    }
    logger.info("build command:" + commands);
    return commands;
  }

  /** append option to StringBuilder */
  protected void appendOption(List<String> commands, String option, String value) {
    commands.add(option);
    commands.add("\"" + value.replace("\"", "\\\"") + "\"");
  }

  /** append jars option to StringBuilder */
  protected void appendJars(List<String> commands, List<Path> paths) {
    appendPaths(commands, "--jars", paths);
  }

  /** append files option to StringBuilder */
  protected void appendFiles(List<String> commands, List<Path> paths) {
    appendPaths(commands, "--files", paths);
  }

  /** append comma-split paths option to StringBuilder */
  protected void appendPaths(List<String> commands, String option, List<Path> paths) {
    if (!paths.isEmpty()) {
      String values = paths.stream().map(Path::toString).collect(Collectors.joining(","));
      appendOption(commands, option, values);
    }
  }

  /** append spark configurations to StringBuilder */
  protected void appendSparkConf(List<String> commands, Map<String, String> sparkConf) {
    for (Map.Entry<String, String> entry : sparkConf.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      appendOption(commands, "--conf", key + "=" + value);
    }
  }

  /** append original commandline args to StringBuilder */
  protected void appendArgs(List<String> commands, String[] args) {
    commands.addAll(Arrays.asList(args));
  }

  /** append appJar to StringBuilder */
  protected void appendAppJar(List<String> commands) {
    //    commands.add(Common.appLibDir().resolve("seatunnel-spark-starter.jar").toString());
    String appJarPath =
        Common.appStarterDir().resolve(EngineType.SPARK2.getStarterJarName()).toString();
    logger.info("spark appJarPath:" + appJarPath);
    commands.add(appJarPath);
  }

  /** a Starter for building spark-submit commands with client mode options */
  private static class ClientModeSparkStarter extends SparkV2Starter {

    /** client mode specified spark options */
    private enum ClientModeSparkConfigs {

      /** Memory for driver in client mode */
      DriverMemory("--driver-memory", "spark.driver.memory"),

      /** Extra Java options to pass to the driver in client mode */
      DriverJavaOptions("--driver-java-options", "spark.driver.extraJavaOptions"),

      /** Extra library path entries to pass to the driver in client mode */
      DriverLibraryPath(" --driver-library-path", "spark.driver.extraLibraryPath"),

      /** Extra class path entries to pass to the driver in client mode */
      DriverClassPath("--driver-class-path", "spark.driver.extraClassPath");

      private final String optionName;

      private final String propertyName;

      private static final Map<String, ClientModeSparkConfigs> PROPERTY_NAME_MAP = new HashMap<>();

      static {
        for (ClientModeSparkConfigs config : values()) {
          PROPERTY_NAME_MAP.put(config.propertyName, config);
        }
      }

      ClientModeSparkConfigs(String optionName, String propertyName) {
        this.optionName = optionName;
        this.propertyName = propertyName;
      }
    }

    private ClientModeSparkStarter(String[] args, SparkCommandArgs commandArgs) {
      super(args, commandArgs);
    }

    @Override
    protected void appendSparkConf(List<String> commands, Map<String, String> sparkConf) {
      for (ClientModeSparkConfigs config : ClientModeSparkConfigs.values()) {
        String driverJavaOptions = this.sparkConf.get(config.propertyName);
        if (StringUtils.isNotBlank(driverJavaOptions)) {
          appendOption(commands, config.optionName, driverJavaOptions);
        }
      }
      for (Map.Entry<String, String> entry : sparkConf.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
        if (ClientModeSparkConfigs.PROPERTY_NAME_MAP.containsKey(key)) {
          continue;
        }
        appendOption(commands, "--conf", key + "=" + value);
      }
    }
  }

  /** a Starter for building spark-submit commands with cluster mode options */
  private static class ClusterModeSparkStarter extends SparkV2Starter {

    private ClusterModeSparkStarter(String[] args, SparkCommandArgs commandArgs) {
      super(args, commandArgs);
    }

    @Override
    public List<String> buildCommands() throws IOException {
      Common.setDeployMode(commandArgs.getDeployMode());
      Common.setStarter(true);
      Path pluginTarball = Common.pluginTarball();
      CompressionUtils.tarGzip(Common.pluginRootDir(), pluginTarball);
      this.files.add(pluginTarball);
      this.files.add(Paths.get(commandArgs.getConfigFile()));
      return super.buildCommands();
    }
  }
}
