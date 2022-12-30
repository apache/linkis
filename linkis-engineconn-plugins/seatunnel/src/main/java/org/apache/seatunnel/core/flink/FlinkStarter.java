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
import org.apache.seatunnel.core.base.Starter;
import org.apache.seatunnel.core.flink.args.FlinkCommandArgs;
import org.apache.seatunnel.core.flink.config.FlinkJobType;
import org.apache.seatunnel.core.flink.utils.CommandLineUtils;

import java.util.List;

/**
 * The SeaTunnel flink starter. This class is responsible for generate the final flink job execute
 * command.
 */
public class FlinkStarter implements Starter {
  public static final Log logger = LogFactory.getLog(FlinkStarter.class.getName());
  private static final String APP_NAME = SeatunnelFlink.class.getName();
  private static final String APP_JAR_NAME = "seatunnel-core-flink.jar";

  /** SeaTunnel parameters, used by SeaTunnel application. e.g. `-c config.conf` */
  private final FlinkCommandArgs flinkCommandArgs;

  /** SeaTunnel flink job jar. */
  private final String appJar;

  FlinkStarter(String[] args) {
    this.flinkCommandArgs = CommandLineUtils.parseCommandArgs(args, FlinkJobType.JAR);
    // set the deployment mode, used to get the job jar path.
    Common.setDeployMode(flinkCommandArgs.getDeployMode().getName());
    this.appJar = Common.appLibDir().resolve(APP_JAR_NAME).toString();
  }

  @SuppressWarnings("checkstyle:RegexpSingleline")
  public static int main(String[] args) {
    logger.info("FlinkStarter start");
    int exitCode = 0;
    try {
      FlinkStarter flinkStarter = new FlinkStarter(args);
      String commandVal = String.join(" ", flinkStarter.buildCommands());
      logger.info("commandVal:" + commandVal);
      exitCode = SeatunnelUtils.executeLine(commandVal);
    } catch (Exception e) {
      exitCode = 1;
      logger.error("\n\n该任务最可能的错误原因是:\n" + e);
    }
    return exitCode;
  }

  @Override
  public List<String> buildCommands() {
    return CommandLineUtils.buildFlinkCommand(flinkCommandArgs, APP_NAME, appJar);
  }
}
