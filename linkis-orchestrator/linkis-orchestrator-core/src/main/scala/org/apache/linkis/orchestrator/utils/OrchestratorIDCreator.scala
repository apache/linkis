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
 
package org.apache.linkis.orchestrator.utils

import java.util.concurrent.atomic.AtomicInteger

/**
  *
  *
  */
trait OrchestratorIDCreator {

  def nextID(): String

  def nextID(prefix: String, splitToken: String = "_"): String

}

object OrchestratorIDCreator {

  private val astJobIDCreator = new  OrchestratorIDCreatorImpl

  private val astStageIDCreator = new  OrchestratorIDCreatorImpl

  private val logicalJobIDCreator = new OrchestratorIDCreatorImpl

  private val logicalStageIDCreator = new OrchestratorIDCreatorImpl

  private val logicalTaskIDCreator = new OrchestratorIDCreatorImpl

  private val physicalJobIDCreator = new OrchestratorIDCreatorImpl

  private val physicalStageIDCreator = new OrchestratorIDCreatorImpl

  private val physicalTaskIDCreator = new OrchestratorIDCreatorImpl

  private val retryTaskIDCreator = new OrchestratorIDCreatorImpl

  private val executionIDCreator = new OrchestratorIDCreatorImpl

  def getAstJobIDCreator: OrchestratorIDCreator = astJobIDCreator

  def getAstStageIDCreator: OrchestratorIDCreator  = astStageIDCreator

  def getLogicalJobIDCreator: OrchestratorIDCreator  = logicalJobIDCreator

  def getLogicalStageIDCreator: OrchestratorIDCreator  = logicalStageIDCreator

  def getLogicalTaskIDCreator: OrchestratorIDCreator  = logicalTaskIDCreator

  def getPhysicalJobIDCreator: OrchestratorIDCreator  = physicalJobIDCreator

  def getPhysicalStageIDCreator: OrchestratorIDCreator  = physicalStageIDCreator

  def getPhysicalTaskIDCreator: OrchestratorIDCreator  = physicalTaskIDCreator

  def getRetryTaskIDCreator: OrchestratorIDCreator  = retryTaskIDCreator

  def getExecutionIDCreator: OrchestratorIDCreator = executionIDCreator

}

class OrchestratorIDCreatorImpl extends OrchestratorIDCreator {

  private val idCreator = new AtomicInteger()

  override def nextID(prefix: String, splitToken: String = "_"): String = {
    prefix + splitToken + nextID()
  }

  override def nextID(): String = {
    idCreator.getAndIncrement().toString
  }

  private def rest(): Unit = {
    idCreator.set(0)
  }
}

