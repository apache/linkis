/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.ruler

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.exception.ScalaCodeCheckException
import com.webank.wedatasphere.linkis.orchestrator.computation.entity.ComputationJobReq
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.ASTContext

class ScalaCodeConverterCheckRuler extends ConverterCheckRuler {

  override def apply(in: JobReq, context: ASTContext): Unit = in match {
    case computationJobReq:ComputationJobReq =>
      val error = new StringBuilder
      computationJobReq.getCodeLanguageLabel.getCodeType match {
        case "scala" => Utils.tryThrow(ScalaExplain.authPass(computationJobReq.getCodeLogicalUnit.toStringCode, error)){
          case ScalaCodeCheckException(errorCode, errDesc) =>
            computationJobReq.setErrorCode(errorCode)
            computationJobReq.setErrorDesc(errDesc)
            ScalaCodeCheckException(errorCode, errDesc)
          case t:Throwable => val exception = ScalaCodeCheckException(20074, "Scala code check failed(scala代码检查失败)")
            exception.initCause(t)
            exception
        }
        case _ =>
      }
  }

  override def getName: String = "ScalaCodeConverterCheckRuler"
}
