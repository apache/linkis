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

package org.apache.seatunnel.common.config;

import org.apache.linkis.engineconnplugin.seatunnel.client.exception.JobExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;

public class Common {
  public static final Log logger = LogFactory.getLog(Common.class.getName());

  private Common() {
    throw new IllegalStateException("Utility class");
  }

  public static final int COLLECTION_SIZE = 16;

  private static final int APP_LIB_DIR_DEPTH = 2;

  private static final int PLUGIN_LIB_DIR_DEPTH = 3;

  private static DeployMode MODE;

  private static String SEATUNNEL_HOME;

  private static boolean STARTER = false;

  public static void setDeployMode(DeployMode mode) {
    MODE = mode;
  }

  public static void setStarter(boolean inStarter) {
    STARTER = inStarter;
  }

  public static DeployMode getDeployMode() {
    return MODE;
  }

  public static Path appStarterDir() {
    return appRootDir().resolve("starter");
  }

  private static String getSeaTunnelHome() {

    if (StringUtils.isNotEmpty(SEATUNNEL_HOME)) {
      return SEATUNNEL_HOME;
    }
    String seatunnelHome = System.getProperty("SEATUNNEL_HOME");
    if (StringUtils.isBlank(seatunnelHome)) {
      seatunnelHome = System.getenv("SEATUNNEL_HOME");
    }
    if (StringUtils.isBlank(seatunnelHome)) {
      seatunnelHome = appRootDir().toString();
    }
    SEATUNNEL_HOME = seatunnelHome;
    return SEATUNNEL_HOME;
  }

  public static Path appRootDir() {
    logger.info("Mode:" + MODE + ",Starter:" + STARTER);
    if (DeployMode.CLIENT == MODE || DeployMode.RUN == MODE || STARTER) {
      try {
        String path = System.getProperty("SEATUNNEL_HOME") + "/seatunnel";
        path = new File(path).getPath();
        logger.info("appRootDir:" + path);
        return Paths.get(path);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (DeployMode.CLUSTER == MODE || DeployMode.RUN_APPLICATION == MODE) {
      return Paths.get("");
    } else {
      throw new IllegalStateException("deploy mode not support : " + MODE);
    }
  }

  public static Path appLibDir() {
    return appRootDir().resolve("lib");
  }

  /** Plugin Root Dir */
  public static Path pluginRootDir() {
    return Paths.get(appRootDir().toString(), "plugins");
  }

  /** Plugin Root Dir */
  public static Path connectorRootDir(String engine) {
    return Paths.get(appRootDir().toString(), "connectors", engine.toLowerCase());
  }

  /** Plugin Connector Jar Dir */
  public static Path connectorJarDir(String engine) {
    return Paths.get(appRootDir().toString(), "connectors", engine.toLowerCase());
  }

  public static Path connectorDir() {
    return Paths.get(appRootDir().toString(), "connectors");
  }

  public static Path pluginTarball() {
    return appRootDir().resolve("plugins.tar.gz");
  }

  public static List<Path> getPluginsJarDependencies() {
    Path pluginRootDir = Common.pluginRootDir();
    if (!Files.exists(pluginRootDir) || !Files.isDirectory(pluginRootDir)) {
      return Collections.emptyList();
    }
    try (Stream<Path> stream = Files.walk(pluginRootDir, PLUGIN_LIB_DIR_DEPTH, FOLLOW_LINKS)) {
      return stream
          .filter(it -> pluginRootDir.relativize(it).getNameCount() == PLUGIN_LIB_DIR_DEPTH)
          .filter(it -> it.getParent().endsWith("lib"))
          .filter(it -> it.getFileName().toString().endsWith(".jar"))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new JobExecutionException(e.getMessage(), e);
    }
  }

  public static Path libDir() {
    return appRootDir().resolve("lib");
  }

  public static List<Path> getLibJars() {
    Path libRootDir = Common.libDir();
    if (!Files.exists(libRootDir) || !Files.isDirectory(libRootDir)) {
      return Collections.emptyList();
    }
    try (Stream<Path> stream = Files.walk(libRootDir, APP_LIB_DIR_DEPTH, FOLLOW_LINKS)) {
      return stream
          .filter(it -> !it.toFile().isDirectory())
          .filter(it -> it.getFileName().toString().endsWith(".jar"))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new JobExecutionException(e.getMessage(), e);
    }
  }

  public static Set<Path> getThirdPartyJars(String paths) {
    logger.info("getThirdPartyJars path:" + paths);
    return Arrays.stream(paths.split(";"))
        .filter(s -> !"".equals(s))
        .filter(it -> it.endsWith(".jar"))
        .map(path -> Paths.get(URI.create(path)))
        .collect(Collectors.toSet());
  }
}
