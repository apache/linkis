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

package org.apache.linkis.governance.common.utils

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf

import org.apache.commons.lang3.StringUtils

import java.io.File
import java.util
import java.util.{ArrayList, List}

object GovernanceUtils extends Logging {

  def killProcess(pid: String, desc: String, isSudo: Boolean): Unit = {
    val subProcessKillScriptPath = Configuration.getLinkisHome() + "/sbin/kill-process-by-pid.sh"
    if (
        StringUtils.isBlank(subProcessKillScriptPath) || !new File(subProcessKillScriptPath)
          .exists()
    ) {
      logger.error(s"Failed to locate kill-script, $subProcessKillScriptPath not exist")
    } else if (StringUtils.isNotBlank(pid)) {
      val cmd = if (isSudo) {
        Array("sudo", "sh", subProcessKillScriptPath, String.valueOf(pid))
      } else {
        Array("sh", subProcessKillScriptPath, String.valueOf(pid))
      }
      logger.info(
        s"Starting to kill sub-processes. desc: $desc  Kill Command: " + cmd.mkString(" ")
      )

      Utils.tryCatch {
        val output = Utils.exec(cmd, 600 * 1000L)
        logger.info(s"Kill Success! desc: $desc. msg:\n ${output}")
      } { t =>
        logger.error(s"Kill error! desc: $desc.", t)
      }
    }
  }

  def killYarnJobApp(appIds: util.List[String]): Unit = {
    if (appIds == null || appIds.isEmpty) return
    val cmdArr = new Array[String](appIds.size + 2)
    cmdArr(0) = "sh"
    cmdArr(1) = GovernanceCommonConf.ENGINE_CONN_YARN_APP_KILL_SCRIPTS_PATH.getValue
    for (i <- 0 until appIds.size) {
      cmdArr(i + 2) = appIds.get(i)
    }

    logger.info("Starting to kill yarn applications." + " Kill Command: " + cmdArr.mkString(" "))
    Utils.tryCatch {
      val output = Utils.exec(cmdArr, 600 * 1000L)
      logger.error(s"Kill yarn applications successfully! msg: $output.")
    } { t =>
      logger.error(s"Kill yarn applications failed!", t)
    }
  }

  def killECProcessByPort(port: String, desc: String, isSudo: Boolean): Unit = {
    val subProcessKillScriptPath =
      Configuration.getLinkisHome() + "/sbin/kill-ec-process-by-port.sh"
    if (
        StringUtils.isBlank(subProcessKillScriptPath) || !new File(subProcessKillScriptPath)
          .exists()
    ) {
      logger.error(s"Failed to locate kill-script, $subProcessKillScriptPath not exist")
    } else if (StringUtils.isNotBlank(port)) {
      val cmd = if (isSudo) {
        Array("sudo", "sh", subProcessKillScriptPath, port)
      } else {
        Array("sh", subProcessKillScriptPath, port)
      }
      logger.info(
        s"Starting to kill process and sub-processes. desc: $desc  Kill Command: " + cmd
          .mkString(" ")
      )

      Utils.tryCatch {
        val output = Utils.exec(cmd, 600 * 1000L)
        logger.info(s"Kill Success! desc: $desc. msg:\n ${output}")
      } { t =>
        logger.error(s"Kill error! desc: $desc.", t)
      }
    }
  }

  /**
   * find process id by port number
   * @param processPort
   * @return
   */
  def findProcessIdentifier(processPort: String): String = {
    val findCmd =
      "sudo netstat -tunlp | grep :" + processPort + " | awk '{print $7}' | awk -F/ '{print $1}'"
    val cmdList = new util.ArrayList[String]
    cmdList.add("bash")
    cmdList.add("-c")
    cmdList.add(findCmd)
    try Utils.exec(cmdList.toArray(new Array[String](0)), 5000L)
    catch {
      case e: Exception =>
        logger.warn("Method findPid failed, " + e.getMessage)
        null
    }
  }

}
