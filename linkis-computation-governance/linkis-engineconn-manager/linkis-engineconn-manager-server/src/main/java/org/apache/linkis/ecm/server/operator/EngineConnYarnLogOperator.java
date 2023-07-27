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

package org.apache.linkis.ecm.server.operator;

import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.ecm.errorcode.EngineconnServerErrorCodeSummary;
import org.apache.linkis.ecm.server.exception.ECMErrorException;
import org.apache.linkis.ecm.server.exception.ECMWarnException;

import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.ecm.errorcode.EngineconnServerErrorCodeSummary.LOG_IS_NOT_EXISTS;

public class EngineConnYarnLogOperator extends EngineConnLogOperator {
  private static final Logger logger = LoggerFactory.getLogger(EngineConnYarnLogOperator.class);

  private static final String YARN_LOG_OPERATOR_NAME = "engineConnYarnLog";

  @Override
  public String[] getNames() {
    return new String[] {EngineConnYarnLogOperator.YARN_LOG_OPERATOR_NAME};
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> parameters) {
    Map<String, Object> result = new HashMap<>();
    try {
      result = super.apply(parameters);
    } finally {
      Object logPath = result.get("logPath");
      if (logPath instanceof String) {
        File logFile = new File((String) logPath);
        if (logFile.exists() && logFile.getName().startsWith(".")) {
          // If is a temporary file, drop it
          logger.info(String.format("Delete the temporary yarn log file: [%s]", logPath));
          if (!logFile.delete()) {
            logger.warn(String.format("Fail to delete the temporary yarn log file: [%s]", logPath));
          }
        }
      }
    }
    return result;
  }

  @Override
  public File getLogPath(Map<String, Object> parameters) {
    String ticketId, engineConnInstance, engineConnLogDir;
    Triple<String, String, String> engineConnInfo = getEngineConnInfo(parameters);
    ticketId = engineConnInfo.getRight();
    engineConnInstance = engineConnInfo.getMiddle();
    engineConnLogDir = engineConnInfo.getLeft();

    File rootLogDir = new File(engineConnLogDir);
    if (!rootLogDir.exists() || !rootLogDir.isDirectory()) {
      throw new ECMWarnException(
          LOG_IS_NOT_EXISTS.getErrorCode(),
          MessageFormat.format(LOG_IS_NOT_EXISTS.getErrorDesc(), rootLogDir));
    }

    String creator = getAsThrow(parameters, "creator");
    String applicationId = getAsThrow(parameters, "yarnApplicationId");
    File logPath = new File(engineConnLogDir, "yarn_" + applicationId);
    if (!logPath.exists()) {
      String tempLogFile =
          String.format(
              ".yarn_%s_%d_%d",
              applicationId, System.currentTimeMillis(), Thread.currentThread().getId());
      try {
        String command =
            String.format(
                "yarn logs -applicationId %s >> %s/%s", applicationId, rootLogDir, tempLogFile);
        logger.info(String.format("Fetch yarn logs to temporary file: [%s]", command));

        ProcessBuilder processBuilder = new ProcessBuilder(sudoCommands(creator, command));
        processBuilder.environment().putAll(System.getenv());
        processBuilder.redirectErrorStream(false);
        Process process = processBuilder.start();
        boolean waitFor = process.waitFor(5, TimeUnit.SECONDS);
        logger.trace(String.format("waitFor: %b, result: %d", waitFor, process.exitValue()));
        if (waitFor && process.waitFor() == 0) {
          command =
              String.format(
                  "mv %s/%s %s/yarn_%s", rootLogDir, tempLogFile, rootLogDir, applicationId);
          logger.info(String.format("Move and save yarn logs: [%s]", command));
          Utils.exec(sudoCommands(creator, command));
        } else {
          logPath = new File(engineConnLogDir, tempLogFile);
          if (!logPath.exists()) {
            throw new WarnException(
                -1,
                String.format(
                    "Fetch yarn logs timeout, log aggregation has not completed or is not enabled"));
          }
        }
      } catch (Exception e) {
        throw new WarnException(
            -1,
            String.format(
                "Fail to fetch yarn logs application: %s, message: %s",
                applicationId, e.getMessage()));
      }
    }
    if (!logPath.exists() || !logPath.isFile()) {
      throw new ECMErrorException(
          EngineconnServerErrorCodeSummary.LOGFILE_IS_NOT_EXISTS.getErrorCode(),
          MessageFormat.format(
              EngineconnServerErrorCodeSummary.LOGFILE_IS_NOT_EXISTS.getErrorDesc(), logPath));
    }
    logger.info(
        String.format(
            "Try to fetch EngineConn(id: %s, instance: %s) yarn logs from %s in application id: %s",
            ticketId, engineConnInstance, logPath.getPath(), applicationId));

    return logPath;
  }

  private String[] sudoCommands(String creator, String command) {
    return new String[] {
      "/bin/bash",
      "-c",
      "sudo su " + creator + " -c \"source ~/.bashrc 2>/dev/null; " + command + "\""
    };
  }
}
