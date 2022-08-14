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

package org.apache.linkis.orchestrator.code.plans.ast

import org.apache.linkis.orchestrator.plans.ast.{AbstractStage, Job, Stage}
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit

class CodeStage(job: Job, private var parents: Array[Stage], private var children: Array[Stage])
    extends AbstractStage {

  private var codeLogicalUnit: CodeLogicalUnit = _

  override def verboseString: String = ???

  override def theSame(other: Stage): Boolean = {
    other match {
      case codeStage: CodeStage =>
        val childrenSame = sameElements(getChildren, codeStage.getChildren)
        val parentSame = sameElements(getParents, codeStage.getParents)
        childrenSame && parentSame
      case _ => false
    }
  }

  private def sameElements(stages1: Array[Stage], stages2: Array[Stage]): Boolean = {
    if (null == stages1 && null == stages2) {
      true
    } else if (null != stages1 && null != stages2 && stages1.length == stages2.length) {
      val size = stages1.length
      var flag = true
      var index = 0
      while (flag && index < size) {
        flag = stages1(index).theSame(stages2(index))
        index += 1
      }
      flag
    } else {
      false
    }
  }

  override def getJob: Job = job

  override def withNewChildren(children: Array[Stage]): Unit = {
    this.children = children
  }

  override def withNewParents(parents: Array[Stage]): Unit = {
    this.parents = parents
  }

  def getCodeLogicalUnit: CodeLogicalUnit = this.codeLogicalUnit

  def setCodeLogicalUnit(codeLogicalUnit: CodeLogicalUnit): Unit = this.codeLogicalUnit =
    codeLogicalUnit

  override def getParents: Array[Stage] = this.parents

  override def getChildren: Array[Stage] = this.children

  override protected def newNode(): Stage = {
    val stage = new CodeStage(this.job, null, null)
    stage.setCodeLogicalUnit(this.codeLogicalUnit)
    stage.setAstContext(this.getASTContext)
    stage
  }

}
