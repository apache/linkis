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
 
package org.apache.linkis.orchestrator.execution.impl

import java.util.Comparator

import org.apache.linkis.orchestrator.execution.ExecTaskRunner
import org.apache.linkis.orchestrator.plans.logical.{EndJobTaskDesc, EndStageTaskDesc}

case class UserTaskRunner(user: String, maxRunningNumber: Int, runningNumber: Int, taskRunner: ExecTaskRunner) {

  private val BASE_SCORE = 50

  def getScore(): Int = {
    val initScore = taskRunner.task.getTaskDesc match {
      case taskDesc: EndJobTaskDesc =>
        BASE_SCORE
      case taskDesc: EndStageTaskDesc =>
        BASE_SCORE
      case _ => 0
    }
    val extraScore = if(runningNumber > maxRunningNumber) {
      -BASE_SCORE
    } else if (maxRunningNumber > 0) {
      BASE_SCORE * (maxRunningNumber - runningNumber) / maxRunningNumber
    } else {
      0
    }
    initScore + extraScore
  }
}

class UserTaskRunnerComparator extends Comparator[UserTaskRunner] {

  override def compare(o1: UserTaskRunner, o2: UserTaskRunner): Int = {
    o2.getScore() - o1.getScore()
  }

}