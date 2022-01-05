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

import java.util

import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import scala.collection.JavaConverters._

class UserRunningNumber {

  private val runningNumber:  java.util.Map[String, Int] = new java.util.HashMap[String, Int]()

  private val SPLIT = ","

  def this(sourceRunningNumber:  java.util.Map[String, Int]) = {
    this()
    this.runningNumber.putAll(sourceRunningNumber)
  }

  def addNumber(user: String, labels: util.List[Label[_]], number: Int = 1): Int = synchronized {
    val key = getKey(labels, user)
    val oldNumber = runningNumber.getOrDefault(key, 0)
    runningNumber.put(key, oldNumber + number)
    oldNumber
  }

  def minusNumber(user: String, labels: util.List[Label[_]], number: Int = 1): Int = synchronized {
    val key = getKey(labels, user)
    val oldNumber = runningNumber.getOrDefault(key, 0)
    val running = oldNumber - number
    if (running > 0) {
      runningNumber.put(key, running)
    } else {
      runningNumber.remove(key)
    }
    oldNumber
  }

  def getRunningNumber(user: String, labels: util.List[Label[_]]): Int = {
    val key = getKey(labels, user)
    runningNumber.getOrDefault(key, 0)
  }

  /**
   * Copy the current running task situation
   * @return
   */
  def copy(): UserRunningNumber = synchronized {
    val newUserRunningNumber = new UserRunningNumber(runningNumber)
    newUserRunningNumber
  }

  /**
   * TODO 统一getKey方法
   * @param labels
   * @return
   */
  def getKey(labels: util.List[Label[_]], user: String): String = {
    var userCreatorLabel: UserCreatorLabel = null
    var engineTypeLabel: EngineTypeLabel = null
    labels.asScala.foreach {
      case label: UserCreatorLabel => userCreatorLabel = label
      case label: EngineTypeLabel => engineTypeLabel = label
      case _ =>
    }
    if (null != userCreatorLabel && null != engineTypeLabel) {
      userCreatorLabel.getStringValue + SPLIT + engineTypeLabel.getStringValue
    } else {
      user
    }
  }
}
