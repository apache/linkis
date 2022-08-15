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

package org.apache.linkis.orchestrator.parser

import org.apache.linkis.orchestrator.extensions.catalyst.{
  AnalyzeFactory,
  ParserTransform,
  Transform
}
import org.apache.linkis.orchestrator.plans.ast.{ASTContext, ASTOrchestration, Job}

/**
 */
abstract class AbstractParser extends Parser with AnalyzeFactory[Job, ASTContext] {

  override def parse(astPlan: ASTOrchestration[_]): ASTOrchestration[_] = {
    // parse
    Option(parserTransforms)
      .map { transforms =>
        logger.debug(s"Start to parse AstJob(${astPlan.getId}) to AstTree.")
        val newAstPlan = astPlan match {
          case job: Job =>
            apply(
              job,
              astPlan.getASTContext,
              transforms.map { transform: Transform[Job, Job, ASTContext] =>
                transform
              }
            )
        }
        logger.debug(s"Finished to parse AstJob(${astPlan.getId}) to AstTree.")
        newAstPlan
      }
      .getOrElse {
        logger.info(s"AstJob(${astPlan.getId}) do not need to parse, ignore it.")
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
