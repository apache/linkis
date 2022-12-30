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

package org.apache.linkis.entrance.interceptor.impl

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary._
import org.apache.linkis.entrance.exception.DangerousGramsCheckException
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.paser.CodeType
import org.apache.linkis.manager.label.entity.engine.EngineType
import org.apache.linkis.manager.label.utils.LabelUtil

import java.lang

class ShellDangerousGrammerInterceptor extends EntranceInterceptor with Logging {

  private val shellDangerousGrammerCheckSwitch =
    EntranceConfiguration.SHELL_DANGER_CHECK_SWITCH.getValue

  private val shellDangerCode = EntranceConfiguration.SHELL_DANGER_USAGE.getValue
  private val shellWhiteCodes = EntranceConfiguration.SHELL_WHITE_USAGE.getValue

  logger.info(s"shellDangerousGrammerCheckSwitch : ${shellDangerousGrammerCheckSwitch}")

  if (shellDangerousGrammerCheckSwitch) {
    logger.info(s"SHELL DANGER USAGE ${shellDangerCode}")
    logger.info(s"SHELL White USAGE ${shellWhiteCodes}")
  }

  def shellWhiteUsage(shellContent: String): Boolean = {
    val shellLines = shellContent.split("\n")
    var signature: Boolean = false
    shellLines foreach { shellLine =>
      shellLine
        .split(";")
        .foreach(inner => {
          val shellCommand: String = inner.trim.split(" ")(0)
          if (shellWhiteCodes.split(",").contains(shellCommand)) {
            signature = true
          }
        })
    }
    signature
  }

  /**
   * check danger shell usage
   *
   * @param shellContent
   * @return
   */
  def shellContainDangerUsage(shellContent: String): Boolean = {
    val shellLines = shellContent.split("\n")
    var signature: Boolean = false
    shellLines foreach { shellLine =>
      if (shellLine.trim.endsWith(".sh")) {
        signature = true
      } else {
        val shellCommands = shellLine.trim.split(" ")
        shellCommands foreach { shellCommand =>
          if (shellDangerCode.split(",").contains(shellCommand)) {
            signature = true
          }
        }
      }
    }
    signature
  }

  /**
   * The apply function is to supplement the information of the incoming parameter task, making the
   * content of this task more complete.   * Additional information includes: database information
   * supplement, custom variable substitution, code check, limit limit, etc.
   * apply函数是对传入参数task进行信息的补充，使得这个task的内容更加完整。 补充的信息包括: 数据库信息补充、自定义变量替换、代码检查、limit限制等
   *
   * @param jobRequest
   * @param logAppender
   *   Used to cache the necessary reminder logs and pass them to the upper layer(用于缓存必要的提醒日志，传给上层)
   * @return
   */
  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    if (!shellDangerousGrammerCheckSwitch) return jobRequest

    val codeType = LabelUtil.getCodeType(jobRequest.getLabels)
    val engineType = LabelUtil.getEngineType(jobRequest.getLabels)
    if (
        CodeType.Shell.equals(CodeType.getType(codeType)) || EngineType.SHELL.equals(
          EngineType.mapStringToEngineType(engineType)
        )
    ) {
      logger.info(s"GET REQUEST CODE_TYPE ${codeType} and ENGINE_TYPE ${EngineType}")
      if (shellContainDangerUsage(jobRequest.getExecutionCode)) {
        throw DangerousGramsCheckException(SHELL_BLACKLISTED_CODE.getErrorDesc)
      }
      jobRequest
    } else {
      jobRequest
    }
  }

}
