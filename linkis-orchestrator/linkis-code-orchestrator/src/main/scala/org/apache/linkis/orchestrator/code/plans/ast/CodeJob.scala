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
 
package org.apache.linkis.orchestrator.code.plans.ast

import org.apache.linkis.orchestrator.plans.ast.{ASTContext, AbstractJob, Job, Stage}
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit

class CodeJob(private var parents: Array[Job],
              private var children: Array[Job]) extends AbstractJob {

  private var name: String = _

  private var submitUser: String = _

  private var stages: Array[Stage] = _

  private var astContext: ASTContext = _

  private var codeLogicalUnit: CodeLogicalUnit = _

  def getSubmitUser: String = submitUser

  def setSubmitUser(submitUser: String): Unit = this.submitUser = submitUser

  override def copyWithNewStages(stages: Array[Stage]): Job = {
    this.stages = stages
    val job = copy()
    job
  }

  override def verboseString: String = toString

  override def withNewChildren(children: Array[Job]): Unit = {
    this.children = children
  }

  override def withNewParents(parents: Array[Job]): Unit = {
    this.parents = parents
  }

  override def getASTContext: ASTContext = this.astContext

  def setAstContext(astContext: ASTContext): Unit = this.astContext = astContext

  def getCodeLogicalUnit: CodeLogicalUnit = this.codeLogicalUnit

  def setCodeLogicalUnit(codeLogicalUnit: CodeLogicalUnit): Unit = this.codeLogicalUnit = codeLogicalUnit

  def setName(name: String): Unit = this.name = name

  override def getName: String = name

  override def theSame(other: Job): Boolean = {
    other match {
      case codeJob: CodeJob =>
        sameElements(getAllStages, codeJob.getAllStages)
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

  override def getAllStages: Array[Stage] = stages

  def setAllStages(stages: Array[Stage]): Unit = this.stages = stages

  override def getParents: Array[Job] = this.parents

  override def getChildren: Array[Job] = this.children

  override protected def newNode(): Job = {
    val job = new CodeJob(null, null)
    job.setAstContext(getASTContext)
    job.setCodeLogicalUnit(getCodeLogicalUnit)
    job.setAllStages(this.stages)
    job.setExecuteUser(getExecuteUser)
    job.setSubmitUser(this.submitUser)
    job.setLabels(this.getLabels)
    job.setParams(this.getParams)
    job.setPriority(this.getPriority)
    job
  }

  override def toString = s"CodeJob($name, $submitUser, $codeLogicalUnit)"
}
