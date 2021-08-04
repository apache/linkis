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

package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.parser

import com.google.common.collect.Lists
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.code.plans.ast.{CodeJob, CodeStage}
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ParserTransform
import com.webank.wedatasphere.linkis.orchestrator.parser.Parser
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, Job}
import com.webank.wedatasphere.linkis.orchestrator.plans.unit.CodeLogicalUnit

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class DefaultCodeJobParserTransform extends ParserTransform with Logging {

  /**
    * This Transform should be the last transform for CodeJob.
    * If no other CodeJobTransform parsed stages for it, then this Transform is used.
    * So, please be noticed, when we build a new [[Parser]], this Transform must be the end one of all CodeJobTransforms.
    * @param in a AstJob
    * @param context ASTContext for this Orchestration
    * @return
    */
  override def apply(in: Job, context: ASTContext): Job = in match {
    case codeJob: CodeJob if codeJob.getAllStages == null || codeJob.getAllStages.isEmpty =>
      val codeStages = new ArrayBuffer[CodeStage]
      codeStages.append(createStage(codeJob.getCodeLogicalUnit, codeJob, context))
      codeJob.setAllStages(codeStages.toArray)
      codeJob
    case _ => in
  }

  def createStage(codeLogicalUnit: CodeLogicalUnit, codeJob: CodeJob, context: ASTContext): CodeStage = {
    val codeStage = new CodeStage(codeJob, null, null)
    codeStage.setAstContext(context)
    codeStage.setCodeLogicalUnit(codeJob.getCodeLogicalUnit)
    codeStage
  }

  def splitCode(codeJob: CodeJob) : Array[CodeLogicalUnit] = {
    val codeLogicalUnits = new ArrayBuffer[CodeLogicalUnit]
    codeJob.getCodeLogicalUnit.getCodes.foreach{ code =>
      code.split(codeJob.getCodeLogicalUnit.getSeparator).foreach { line =>
        codeLogicalUnits.append(new CodeLogicalUnit(Lists.newArrayList(line), codeJob.getCodeLogicalUnit.getLabel, codeJob.getCodeLogicalUnit.getSeparator))
      }
    }
    codeLogicalUnits.toArray
  }


  override def getName: String = "CodeStageParserTransform"
}
