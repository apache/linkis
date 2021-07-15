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
 */

package com.webank.wedatasphere.linkis.orchestrator.parser

import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.{AnalyzeFactory, ParserTransform, Transform}
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, ASTOrchestration, Job}

/**
  *
  *
  */
abstract class AbstractParser extends Parser with AnalyzeFactory[Job, ASTContext] {

  override def parse(astPlan: ASTOrchestration[_]): ASTOrchestration[_] = {
    //parse
    Option(parserTransforms).map { transforms =>
      debug(s"Start to parse AstJob(${astPlan.getId}) to AstTree.")
      val newAstPlan = astPlan match {
        case job: Job =>
          apply(job, astPlan.getASTContext, transforms.map{
            transform: Transform[Job, Job, ASTContext] => transform
          })
      }
      debug(s"Finished to parse AstJob(${astPlan.getId}) to AstTree.")
      newAstPlan
    }.getOrElse{
      info(s"AstJob(${astPlan.getId}) do not need to parse, ignore it.")
      astPlan
    }
  }

//  protected def doWhileParser(astPlan: ASTOrchestration[_]): ASTOrchestration[_] = {
//    var oldAstPlan: ASTOrchestration[_] = astPlan
//    var newAstPlan: ASTOrchestration[_] = null
//    var count = 0
//    while (null == newAstPlan || !oldAstPlan.asInstanceOf[Job].theSame(newAstPlan.asInstanceOf[Job])) {
//      info(s"oldAstPlan != newAstPlan, retry to parser count($count)")
//      count += 1
//      val tmpPlan: ASTOrchestration[_] = oldAstPlan
//      if (null != newAstPlan) {
//        oldAstPlan = newAstPlan
//      }
//      newAstPlan = tmpPlan
//      parserTransforms.foreach(of => newAstPlan = of.apply(newAstPlan, astPlan.getASTContext))
//    }
//    newAstPlan
//  }

  protected def parserTransforms: Array[ParserTransform]
}
