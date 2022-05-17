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
 
package org.apache.linkis.orchestrator.converter

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.exception.{OrchestratorErrorCodeSummary, OrchestratorErrorException}
import org.apache.linkis.orchestrator.extensions.catalyst.{ConverterCheckRuler, ConverterTransform}
import org.apache.linkis.orchestrator.plans.ast.{ASTContext, Job}

/**
  *
  */
trait AbstractConverter extends Converter with Logging{

  protected def converterTransforms: Array[ConverterTransform]

  protected def converterCheckRulers: Array[ConverterCheckRuler]

  protected def createASTContext(jobReq: JobReq): ASTContext

  override def convert(jobReq: JobReq): Job = {
    val context = createASTContext(jobReq)
    converterCheckRulers.foreach(_(jobReq, context))
    debug(s"Start to convert JobReq(${jobReq.getId}) to AstJob.")
    val job = converterTransforms.collectFirst { case transform => transform(jobReq, context) }
      .getOrElse(throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.CONVERTER_FOR_NOT_SUPPORT_ERROR_CODE, "Cannot convert jobReq " + jobReq))
    info(s"Finished to convert JobReq(${jobReq.getId}) to AstJob(${job.getId}).")
    job
  }

}