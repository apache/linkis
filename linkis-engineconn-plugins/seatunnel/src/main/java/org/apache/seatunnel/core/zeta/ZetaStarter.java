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

package org.apache.seatunnel.core.zeta;

import org.apache.linkis.engineconnplugin.seatunnel.util.SeatunnelUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.seatunnel.common.config.Common;
import org.apache.seatunnel.core.starter.Starter;
import org.apache.seatunnel.core.starter.enums.EngineType;
import org.apache.seatunnel.core.starter.seatunnel.args.ClientCommandArgs;
import org.apache.seatunnel.core.starter.utils.CommandLineUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZetaStarter implements Starter {
  public static final Log logger = LogFactory.getLog(ZetaStarter.class.getName());

  private static final String APP_JAR_NAME = EngineType.SEATUNNEL.getStarterJarName();
  private static final String SHELL_NAME = EngineType.SEATUNNEL.getStarterShellName();
  private static final String namePrefix = "seaTunnel";
  private final ClientCommandArgs commandArgs;
  private final String appJar;

  ZetaStarter(String[] args) {
    this.commandArgs = CommandLineUtils.parse(args, new ClientCommandArgs(), SHELL_NAME, true);
    logger.info("this.commandArgs = " + this.commandArgs);
    // set the deployment mode, used to get the job jar path.
    Common.setDeployMode(commandArgs.getDeployMode());
    Common.setStarter(true);
    this.appJar = Common.appStarterDir().resolve(APP_JAR_NAME).toString();
  }

  public static int main(String[] args) {
    int exitCode = 0;
    try {
      logger.info("seaTunnel Zeta process..");
      ZetaStarter zetaStarter = new ZetaStarter(args);
      String commandVal = String.join(" ", zetaStarter.buildCommands());
      logger.info("ZetaStarter commandVal:" + commandVal);
      exitCode = SeatunnelUtils.executeLine(commandVal);
    } catch (Exception e) {
      exitCode = 1;
      logger.error("\n\nZetaStarter error:\n" + e);
    }
    return exitCode;
  }

  @Override
  public List<String> buildCommands() {
    List<String> command = new ArrayList<>();
    command.add("${SEATUNNEL_HOME}/bin/" + SHELL_NAME);
    command.add("--master");
    command.add(this.commandArgs.getMasterType().name());
    command.add("--cluster");
    command.add(
        StringUtils.isNotBlank(this.commandArgs.getClusterName())
            ? this.commandArgs.getClusterName()
            : randomClusterName());
    command.add("--config");
    command.add(this.commandArgs.getConfigFile());
    command.add("--name");
    command.add(this.commandArgs.getJobName());
    return command;
  }

  public String randomClusterName() {
    Random random = new Random();
    return namePrefix + "-" + random.nextInt(1000000);
  }
}
