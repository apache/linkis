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

package com.webank.wedatasphere.linkis.orchestrator.execution.impl

import scala.collection.mutable

class UserRunningNumber {

  private val runningNumber:  mutable.Map[String, Int] = new mutable.HashMap[String, Int]()

  def this(sourceRunningNumber:  mutable.Map[String, Int]) = {
    this()
    this.runningNumber ++= sourceRunningNumber
  }

  def addNumber(user: String, number: Int = 1): Int = synchronized {
    val oldNumber = runningNumber.getOrElse(user, 0)
    runningNumber.put(user, oldNumber + number)
    oldNumber
  }

  def minusNumber(user: String, number: Int = 1): Int = synchronized {
    val oldNumber = runningNumber.getOrElse(user, 0)
    val running = oldNumber - number
    if (running > 0) {
      runningNumber.put(user, running)
    } else {
      runningNumber.remove(user)
    }
    oldNumber
  }

  def getRunningNumber(user: String): Int = {
    runningNumber.getOrElse(user, 0)
  }

  def copy(): UserRunningNumber = {
    val newUserRunningNumber = new UserRunningNumber(runningNumber)
    newUserRunningNumber
  }

}
