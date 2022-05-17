/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.governance.common.utils

import org.apache.commons.lang.StringUtils
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, Utils}

import java.io.File

object GovernanceUtils extends Logging{


  def killProcess(pid: String, desc: String, isSudo: Boolean): Unit = {
    val subProcessKillScriptPath = Configuration.getLinkisHome() + "/sbin/kill-process-by-pid.sh"
    if (StringUtils.isBlank(subProcessKillScriptPath) || !new File(subProcessKillScriptPath).exists()) {
      logger.error(s"Failed to locate kill-script, $subProcessKillScriptPath not exist")
    } else if (StringUtils.isNotBlank(pid)) {
      val cmd = if (isSudo) {
        Array("sudo", "sh", subProcessKillScriptPath, String.valueOf(pid))
      } else {
        Array("sh", subProcessKillScriptPath, String.valueOf(pid))
      }
      logger.info(s"Starting to kill sub-processes. desc: $desc  Kill Command: " + cmd.mkString(" "))

      Utils.tryCatch {
        val output = Utils.exec(cmd, 600 * 1000L)
        logger.info(s"Kill Success! desc: $desc. msg:\n ${output}")
      } { t =>
        logger.error(s"Kill error! desc: $desc.", t)
      }
    }
  }

}
