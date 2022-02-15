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

import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.orchestrator.computation.catalyst.converter.exception.VarSubstitutionException
import org.apache.linkis.orchestrator.computation.entity.ComputationJobReq
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import org.apache.linkis.orchestrator.log.LogManager
import org.apache.linkis.orchestrator.plans.ast.ASTContext
import org.apache.commons.lang.exception.ExceptionUtils

class VarSubstitutionConverterCheckRuler extends ConverterCheckRuler {

  override def apply(in: JobReq, context: ASTContext): Unit = {
    in match {
      case computationJobReq: ComputationJobReq =>
        Utils.tryThrow {
          LogManager.getLogManager.cacheLog(computationJobReq.getId, LogUtils.generateInfo("Program is substituting variables for you") + "\n")
          CustomVariableUtils.replaceCustomVar(computationJobReq, computationJobReq.getCodeLanguageLabel.getCodeType)
          LogManager.getLogManager.cacheLog(computationJobReq.getId, LogUtils.generateInfo( "Variables substitution ended successfully") + "\n")
        }{
          case e:VarSubstitutionException =>
            val exception = VarSubstitutionException(20050, "Variable replacement failed!(变量替换失败！)" + ExceptionUtils.getRootCauseMessage(e))
            exception.initCause(e)
            exception
          case t: Throwable =>
            val exception = VarSubstitutionException(20050, "Variable replacement failed!(变量替换失败！)" + ExceptionUtils.getRootCauseMessage(t))
            exception.initCause(t)
            exception
        }
      case _ =>
    }
  }

  override def getName: String = "VarSubstitutionConverterCheckRuler"
}
