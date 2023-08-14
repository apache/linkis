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

package org.apache.linkis.ecm.server.service.impl;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.ecm.server.conf.ECMConfiguration;
import org.apache.linkis.ecm.server.service.EngineConnKillService;
import org.apache.linkis.engineconn.common.conf.EngineConnConf;
import org.apache.linkis.governance.common.utils.GovernanceUtils;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineStopResponse;
import org.apache.linkis.manager.common.protocol.engine.EngineSuicideRequest;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEngineConnKillService implements EngineConnKillService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultEngineConnKillService.class);

  private static final ThreadPoolExecutor ecYarnAppKillService =
      Utils.newCachedThreadPool(10, "ECM-Kill-EC-Yarn-App", true);

  @Override
  @Receiver
  public EngineStopResponse dealEngineConnStop(EngineStopRequest engineStopRequest) {
    logger.info("received EngineStopRequest " + engineStopRequest);
    String pid = null;
    if (AMConstant.PROCESS_MARK.equals(engineStopRequest.getIdentifierType())
        && StringUtils.isNotBlank(engineStopRequest.getIdentifier())) {
      pid = engineStopRequest.getIdentifier();
    }
    logger.info("dealEngineConnStop return pid: {}", pid);
    EngineStopResponse response = new EngineStopResponse();
    if (StringUtils.isNotBlank(pid)) {
      if (!killEngineConnByPid(pid, engineStopRequest.getServiceInstance())) {
        response.setStopStatus(false);
        response.setMsg(
            "Kill engine " + engineStopRequest.getServiceInstance().toString() + " failed.");
      } else {
        response.setStopStatus(true);
        response.setMsg(
            "Kill engine " + engineStopRequest.getServiceInstance().toString() + " succeed.");
      }
    } else {
      String processPort = engineStopRequest.getServiceInstance().getInstance().split(":")[1];
      logger.warn("Kill EC {} by port {}", engineStopRequest.getServiceInstance(), processPort);
      if (!killEngineConnByPort(processPort, engineStopRequest.getServiceInstance())) {
        response.setStopStatus(false);
        response.setMsg(
            "Kill engine " + engineStopRequest.getServiceInstance().toString() + " failed.");
      } else {
        response.setStopStatus(true);
        response.setMsg(
            "Kill engine " + engineStopRequest.getServiceInstance().toString() + " succeed.");
      }
    }

    // Requires default kill yarn appid
    if (AMConstant.PROCESS_MARK.equals(engineStopRequest.getIdentifierType())) {
      killYarnAppIdOfOneEc(engineStopRequest);
    }

    if (AMConstant.CLUSTER_PROCESS_MARK.equals(engineStopRequest.getIdentifierType())
        && engineStopRequest.getIdentifier() != null) {
      List<String> appIds = new ArrayList<>();
      appIds.add(engineStopRequest.getIdentifier());
      GovernanceUtils.killYarnJobApp(appIds);
    }

    if (!response.getStopStatus()) {
      EngineSuicideRequest request =
          new EngineSuicideRequest(
              engineStopRequest.getServiceInstance(), engineStopRequest.getUser());
      try {
        Sender.getSender(engineStopRequest.getServiceInstance()).send(request);
        response.setStopStatus(true);
        response.setMsg(response.getMsg() + " Now send suicide request to engine.");
      } catch (Exception e) {
        response.setMsg(
            response.getMsg() + " Sended suicide request to engine error, " + e.getMessage());
      }
    }
    return response;
  }

  public void killYarnAppIdOfOneEc(EngineStopRequest engineStopRequest) {
    String logDirSuffix = engineStopRequest.getLogDirSuffix();
    ServiceInstance serviceInstance = engineStopRequest.getServiceInstance();
    String engineType = engineStopRequest.getEngineType();
    String engineConnInstance = serviceInstance.toString();
    String engineLogDir;
    if (logDirSuffix.startsWith(ECMConfiguration.ENGINECONN_ROOT_DIR())) {
      engineLogDir = logDirSuffix;
    } else {
      engineLogDir = ECMConfiguration.ENGINECONN_ROOT_DIR() + File.separator + logDirSuffix;
    }
    logger.info(
        "try to kill yarn app ids in the engine of: [{}] engineLogDir: [{}]",
        engineConnInstance,
        engineLogDir);
    final String errEngineLogPath = engineLogDir.concat(File.separator).concat("yarnApp");
    logger.info(
        "try to parse the yarn app id from the engine err log file path: [{}]", errEngineLogPath);
    File file = new File(errEngineLogPath);
    if (file.exists()) {
      ecYarnAppKillService.execute(
          () -> {
            BufferedReader in = null;
            try {
              in = new BufferedReader(new FileReader(errEngineLogPath));
              String line;
              String regex = getYarnAppRegexByEngineType(engineType);
              if (StringUtils.isBlank(regex)) {
                return;
              }
              Pattern pattern = Pattern.compile(regex);
              List<String> appIds = new ArrayList<>();
              while ((line = in.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                  Matcher mApp = pattern.matcher(line);
                  if (mApp.find()) {
                    String candidate1 = mApp.group(mApp.groupCount());
                    if (!appIds.contains(candidate1)) {
                      appIds.add(candidate1);
                    }
                  }
                }
              }
              GovernanceUtils.killYarnJobApp(appIds);
              logger.info("finished kill yarn app ids in the engine of ({}).", engineConnInstance);
            } catch (IOException ioEx) {
              if (ioEx instanceof FileNotFoundException) {
                logger.error("the engine log file {} not found.", errEngineLogPath);
              } else {
                logger.error(
                    "the engine log file parse failed. the reason is {}", ioEx.getMessage());
              }
            } finally {
              IOUtils.closeQuietly(in);
            }
          });
    }
  }

  private String getYarnAppRegexByEngineType(String engineType) {
    if (StringUtils.isBlank(engineType)) {
      return "";
    }
    String regex;
    switch (engineType) {
      case "spark":
      case "shell":
        regex = EngineConnConf.SPARK_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX().getValue();
        break;
      case "sqoop":
        regex = EngineConnConf.SQOOP_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX().getValue();
        break;
      case "hive":
        regex = EngineConnConf.HIVE_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX().getValue();
        break;
      case "seatunnel":
        regex = EngineConnConf.SEATUNNEL_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX().getValue();
        break;
      default:
        regex = "";
    }
    return regex;
  }

  private boolean killEngineConnByPid(String processId, ServiceInstance serviceInstance) {
    logger.info("try to kill {} toString with pid({}).", serviceInstance.toString(), processId);
    if (StringUtils.isNotBlank(processId)) {
      if (ECMConfiguration.ECM_PROCESS_SCRIPT_KILL()) {
        GovernanceUtils.killProcess(processId, serviceInstance.toString(), true);
      } else {
        killProcessByKillCmd(processId, serviceInstance.toString());
      }
      return !isProcessAlive(processId);
    } else {
      logger.warn("cannot kill {} with empty pid.", serviceInstance);
      return false;
    }
  }

  private boolean killEngineConnByPort(String port, ServiceInstance serviceInstance) {
    logger.info("try to kill {} toString with port({}).", serviceInstance.toString(), port);
    if (StringUtils.isNotBlank(port)) {
      GovernanceUtils.killECProcessByPort(port, serviceInstance.toString(), true);
      return !isProcessAliveByPort(port);
    } else {
      logger.warn("cannot kill {} with empty port.", serviceInstance);
      return false;
    }
  }

  private boolean isProcessAlive(String pid) {
    String findCmd =
        "ps -ef | grep "
            + pid
            + " | grep EngineConnServer | awk '{print \"exists_\"$2}' | grep "
            + pid
            + "|| true";
    List<String> cmdList = new ArrayList<>();
    cmdList.add("bash");
    cmdList.add("-c");
    cmdList.add(findCmd);
    try {
      String rs = Utils.exec(cmdList.toArray(new String[0]), 5000L);
      return null != rs && rs.contains("exists_" + pid);
    } catch (Exception e) {
      logger.warn("Method isProcessAlive failed", e);
      return false;
    }
  }

  private boolean isProcessAliveByPort(String port) {
    String findCmd =
        "ps -ef | grep server.port= "
            + port
            + " | grep EngineConnServer | awk -F \"server.port=\" '{print \"exists_\"$2}'";
    List<String> cmdList = new ArrayList<>();
    cmdList.add("bash");
    cmdList.add("-c");
    cmdList.add(findCmd);
    try {
      String rs = Utils.exec(cmdList.toArray(new String[0]), 5000L);
      return null != rs && rs.contains("exists_" + port);
    } catch (Exception e) {
      logger.warn("Method isProcessAlive failed", e);
      return false;
    }
  }

  private void killProcessByKillCmd(String pid, String desc) {
    String k15cmd = "sudo kill " + pid;
    String k9cmd = "sudo kill -9 " + pid;
    int tryNum = 0;
    try {
      while (isProcessAlive(pid) && tryNum <= 3) {
        logger.info(
            "{} still alive with pid({}), use shell command to kill it. try {}++",
            desc,
            pid,
            tryNum++);
        if (tryNum <= 3) {
          Utils.exec(k15cmd.split(" "), 3000L);
        } else {
          logger.info(
              "{} still alive with pid({}). try {}, use shell command to kill -9 it",
              desc,
              pid,
              tryNum);
          Utils.exec(k9cmd.split(" "), 3000L);
        }
        Thread.sleep(5000);
      }
    } catch (InterruptedException e) {
      logger.error("Interrupted while killing engine {} with pid({})." + desc, pid);
    }
  }
}
