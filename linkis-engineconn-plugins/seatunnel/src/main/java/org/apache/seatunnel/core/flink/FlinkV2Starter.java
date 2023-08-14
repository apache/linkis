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

package org.apache.seatunnel.core.flink;

import org.apache.linkis.engineconnplugin.seatunnel.util.SeatunnelUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.seatunnel.common.config.Common;
import org.apache.seatunnel.core.starter.Starter;
import org.apache.seatunnel.core.starter.enums.EngineType;
import org.apache.seatunnel.core.starter.flink.SeaTunnelFlink;
import org.apache.seatunnel.core.starter.flink.args.FlinkCommandArgs;
import org.apache.seatunnel.core.starter.utils.CommandLineUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FlinkV2Starter implements Starter {
  public static final Log logger = LogFactory.getLog(FlinkV2Starter.class.getName());
  private static final String APP_NAME = SeaTunnelFlink.class.getName();
  private static final String APP_JAR_NAME = EngineType.FLINK13.getStarterJarName();
  private static final String SHELL_NAME = EngineType.FLINK13.getStarterShellName();

  /** SeaTunnel parameters, used by SeaTunnel application. e.g. `-c config.conf` */
  private final FlinkCommandArgs flinkCommandArgs;

  /** SeaTunnel flink job jar. */
  private final String appJar;

  FlinkV2Starter(String[] args) {
    this.flinkCommandArgs = CommandLineUtils.parse(args, new FlinkCommandArgs(), SHELL_NAME, true);
    logger.info("this.flinkCommandArgs = " + this.flinkCommandArgs);
    // set the deployment mode, used to get the job jar path.
    Common.setDeployMode(flinkCommandArgs.getDeployMode());
    Common.setStarter(true);
    this.appJar = Common.appStarterDir().resolve(APP_JAR_NAME).toString();
  }

  @SuppressWarnings("checkstyle:RegexpSingleline")
  public static int main(String[] args) {
    logger.info("FlinkStarter start:" + Arrays.toString(args));
    int exitCode = 0;
    try {
      FlinkV2Starter flinkStarter = new FlinkV2Starter(args);
      String commandVal = String.join(" ", flinkStarter.buildCommands());
      logger.info("FlinkV2Starter commandVal:" + commandVal);
      exitCode = SeatunnelUtils.executeLine(commandVal);
    } catch (Exception e) {
      exitCode = 1;
      logger.error("\n\nFlinkV2Starter error:\n" + e);
    }
    return exitCode;
  }

  @Override
  public List<String> buildCommands() {
    List<String> command = new ArrayList<>();
    command.add("${FLINK_HOME}/bin/flink");
    // set deploy mode, run or run-application
    command.add(flinkCommandArgs.getDeployMode().getDeployMode());
    // set submitted target master
    if (flinkCommandArgs.getMasterType() != null) {
      command.add("--target");
      command.add(flinkCommandArgs.getMasterType().getMaster());
    }
    logger.info("FlinkV2Starter OriginalParameters:" + flinkCommandArgs.getOriginalParameters());
    command.add("-c");
    command.add(APP_NAME);
    command.add(appJar);
    command.add("--config");
    command.add(flinkCommandArgs.getConfigFile());
    command.add("--name");
    command.add(flinkCommandArgs.getJobName());
    // set System properties
    flinkCommandArgs.getVariables().stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .forEach(variable -> command.add("-D" + variable));
    return command;
  }
}
