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
 
package org.apache.linkis.orchestrator.computation.catalyst.converter.ruler

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.orchestrator.computation.catalyst.converter.exception.SensitiveTablesCheckException
import org.apache.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import org.apache.linkis.orchestrator.computation.entity.ComputationJobReq
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import org.apache.linkis.orchestrator.plans.ast.ASTContext


class ShellDangerousGrammarConverterCheckRuler extends ConverterCheckRuler  with Logging {

  private val shellDangerUsage = ComputationOrchestratorConf.SHELL_DANGER_USAGE.getValue
  info(s"SHELL DANGER USAGE ${shellDangerUsage}")

  private val shellWhiteUsage = ComputationOrchestratorConf.SHELL_WHITE_USAGE.getValue
  info(s"SHELL White USAGE ${shellWhiteUsage}")

  private val shellWhiteUsageEnabled = ComputationOrchestratorConf.SHELL_WHITE_USAGE_ENABLED.getValue
  info(s"Only Allow SHELL White USAGE? ${shellWhiteUsage}")


  def shellWhiteUsage(shellContent:String): Boolean = {
    if(!shellWhiteUsageEnabled) return true
    val shellLines = shellContent.split("\n")
    var signature: Boolean = false
    shellLines foreach {
      shellLine =>
        val shellCommand: String = shellLine.trim.split(" ")(0)
        if (shellWhiteUsage.split(",").contains(shellCommand)){
          signature = true
        }
    }
    signature
  }

  def shellContainDangerUsage(shellContent:String): Boolean ={
    val shellLines = shellContent.split("\n")
    var signature:Boolean = false
    shellLines synchronized{
      shellLines foreach{
        shellLine =>
          if (shellLine.trim.endsWith(".sh")){//禁止执行shell命令
            signature=true
          }else{
            val shellCommands = shellLine.trim.split(" ")
            shellCommands foreach{
              shellCommand =>  shellDangerUsage.split(",").contains(shellCommand) match {
                case true => signature=true
                case _ =>
              }
            }
          }
      }
    }
    signature
  }

  /**
    * The apply function is to supplement the information of the incoming parameter task, making the content of this task more complete.
    *   * Additional information includes: database information supplement, custom variable substitution, code check, limit limit, etc.
    * apply函数是对传入参数task进行信息的补充，使得这个task的内容更加完整。
    * 补充的信息包括: 数据库信息补充、自定义变量替换、代码检查、limit限制等
    *
    * @param in
    * @return
    */
  override def apply(in: JobReq, context: ASTContext): Unit = {
    in match {
      case computationJobReq: ComputationJobReq =>
        if ("shell".equals(computationJobReq.getCodeLanguageLabel.getCodeType)
          || "sh".equals(computationJobReq.getCodeLanguageLabel.getCodeType)){
          info(s"GET REQUEST RUNTYPE ${computationJobReq.getCodeLanguageLabel.getCodeType}")
          if (/**PythonMySqlUtils.checkSensitiveTables(entranceJobReq.getExecutionContent) && **/!shellContainDangerUsage(computationJobReq.getCodeLogicalUnit.toStringCode) && shellWhiteUsage(computationJobReq.getCodeLogicalUnit.toStringCode)) {
            //info(s"CHECK SENSITIVE table ${PythonMySqlUtils.checkSensitiveTables(entranceJobReq.getExecutionContent)}")
            info(s"CHECK SENSITIVE code ${!shellContainDangerUsage(computationJobReq.getCodeLogicalUnit.toStringCode)}")
          }
          else throw SensitiveTablesCheckException("代码中含有敏感表信息，不能进行提交")
        }
      case _ =>
    }
  }

  override def getName: String = "ShellDangerousGrammarConverterCheckRuler"
}
