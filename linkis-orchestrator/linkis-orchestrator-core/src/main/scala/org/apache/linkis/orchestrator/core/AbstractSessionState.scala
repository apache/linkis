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

package org.apache.linkis.orchestrator.core

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.orchestrator.OrchestratorSession
import org.apache.linkis.orchestrator.extensions.Extensions
import org.apache.linkis.orchestrator.extensions.catalyst._
import org.apache.linkis.orchestrator.extensions.operation.Operation
import org.apache.linkis.orchestrator.listener.{
  OrchestratorAsyncListenerBus,
  OrchestratorListenerBusContext,
  OrchestratorSyncListenerBus
}

import java.util

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/**
 */
abstract class AbstractSessionState(
    orchestratorSession: OrchestratorSession,
    transforms: Array[Transform[_, _, _]],
    checkRulers: Array[CheckRuler[_, _]],
    operations: Array[Operation[_]],
    extractExtensions: Array[Extensions[_]]
) extends SessionState {

  private val configMap: java.util.Map[String, String] = new util.HashMap[String, String]

  private[core] override def setStringConf(key: String, value: String): Unit =
    configMap.put(key, value)

  private val busContext = OrchestratorListenerBusContext.createBusContext

  override def getValue[T](commonVars: CommonVars[T]): T = commonVars.getValue(configMap)

  override def getValue[T](key: String): T = CommonVars(key).getValue(configMap)

  override def getOperations: Array[Operation[_]] = operations

  protected def getConverterCheckRulers: Array[ConverterCheckRuler] = getExtensions(checkRulers)

  protected def getValidatorCheckRulers: Array[ValidatorCheckRuler] = getExtensions(checkRulers)

  protected def getConverterTransforms: Array[ConverterTransform] = getExtensions(transforms)

  private def getExtensions[R: ClassTag](extensions: Array[_]): Array[R] = {
    val resExtensions = new ArrayBuffer[R]()
    extensions.foreach {
      case extension: R =>
        resExtensions += extension
      case _ => false
    }
    resExtensions.toArray
  }

  protected def getParserTransforms: Array[ParserTransform] = getExtensions(transforms)

  protected def getPlannerTransforms: Array[PlannerTransform] = getExtensions(transforms)

  protected def getOptimizerTransforms: Array[OptimizerTransform] = getExtensions(transforms)

  protected def getPhysicalTransforms: Array[PhysicalTransform] = getExtensions(transforms)

  protected def getReheaterTransforms: Array[ReheaterTransform] = getExtensions(transforms)

  override def getOrchestratorAsyncListenerBus: OrchestratorAsyncListenerBus = {
    busContext.getOrchestratorAsyncListenerBus
  }

  override def getOrchestratorSyncListenerBus: OrchestratorSyncListenerBus = {
    busContext.getOrchestratorSyncListenerBus
  }

}
