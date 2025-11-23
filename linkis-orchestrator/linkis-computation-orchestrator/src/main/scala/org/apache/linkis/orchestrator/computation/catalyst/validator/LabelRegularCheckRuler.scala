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

package org.apache.linkis.orchestrator.computation.catalyst.validator

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.orchestrator.exception.{
  OrchestratorErrorCodeSummary,
  OrchestratorErrorException
}
import org.apache.linkis.orchestrator.extensions.catalyst.ValidatorCheckRuler
import org.apache.linkis.orchestrator.plans.ast.{ASTContext, ASTOrchestration, Job, Stage}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 */
trait LabelRegularCheckRuler extends ValidatorCheckRuler with Logging {

  /** default label array for check, cannot be override* */
  final val defaultLabel = Array[Label[_]](new UserCreatorLabel, new EngineTypeLabel)

  /** use to add label to the check list, extends this trait and override this value* */
  val customLabel = Array[Label[_]]()

  override def apply(in: ASTOrchestration[_], context: ASTContext): Unit = {
    in match {
      case stage: Stage =>
        val stageId = stage.getId
        val jobId = stage.getJob.getId
        logger.debug(s"${getName}:start to check labels,jobId:${jobId}---stageId:${stageId} ")
      case jobId: Job =>
        logger.debug(s"${getName}:start to check labels,jobId:${jobId}")
      case _ =>
    }
    val queryLabels = context.getLabels
    var checkResult = true
    val missingLabel = new ArrayBuffer[Label[_]]
    val userDefinedLabel = defaultLabel ++ customLabel
    userDefinedLabel.foreach(needLabel => {
      if (!queryLabels.asScala.exists(_.getClass == needLabel.getClass)) {
        logger.warn(
          s"label:${needLabel.getClass.getName} is needed, but there is no definition in the requested labels!" +
            s"(请求的标签列表中缺少标签:${needLabel.getLabelKey})！"
        )
        missingLabel += needLabel
        checkResult = false
      }
    })
    if (!checkResult) {
      val labelStr = missingLabel.map(_.getLabelKey).mkString(";")
      throw new OrchestratorErrorException(
        OrchestratorErrorCodeSummary.LABEL_NOT_EXISTS_ERROR_CODE,
        s"Label verification failed, Please check the integrity of the requested labels, missing labels:${labelStr}"
      )
    }
  }

  override def getName: String = {
    val className = getClass.getName
    if (className endsWith "$") className.dropRight(1) else className
  }

}
