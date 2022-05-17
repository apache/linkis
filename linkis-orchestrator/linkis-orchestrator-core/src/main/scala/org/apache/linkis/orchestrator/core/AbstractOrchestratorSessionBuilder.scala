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
 
package org.apache.linkis.orchestrator.core

import org.apache.linkis.orchestrator.extensions.CatalystExtensions.CatalystExtensionsBuilder
import org.apache.linkis.orchestrator.extensions.CheckRulerExtensions.CheckRulerExtensionsBuilder
import org.apache.linkis.orchestrator.extensions.OperationExtensions.OperationExtensionsBuilder
import org.apache.linkis.orchestrator.extensions.catalyst.{CatalystExtensionsImpl, CheckRuler, CheckRulerExtensionsImpl, Transform}
import org.apache.linkis.orchestrator.extensions.operation.{Operation, OperationExtensionsImpl}
import org.apache.linkis.orchestrator.extensions.{Extensions, _}
import org.apache.linkis.orchestrator.{Orchestrator, OrchestratorSession}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  *
  */
abstract class AbstractOrchestratorSessionBuilder extends OrchestratorSessionBuilder {

  protected var orchestrator: Orchestrator = _
  protected val configMap = new mutable.HashMap[String, Any]
  protected val extensions = new ArrayBuffer[Extensions[_]]
  protected val catalystExtensionsBuilders = new ArrayBuffer[CatalystExtensionsBuilder]
  protected val checkRulerExtensionsBuilders = new ArrayBuffer[CheckRulerExtensionsBuilder]
  protected val operationExtensionsBuilders = new ArrayBuffer[OperationExtensionsBuilder]
  protected var id: String = _

  override def setOrchestrator(orchestrator: Orchestrator): OrchestratorSessionBuilder = {
    this.orchestrator = orchestrator
    this
  }

  override def setId(id: String): OrchestratorSessionBuilder = {
    this.id = id
    this
  }

  override def getId(): String = id

  override def config(key: String, value: Any): OrchestratorSessionBuilder = {
    configMap += key -> value
    this
  }

  override def withExtensions(extensions: Extensions[_]): OrchestratorSessionBuilder = {
    this.extensions += extensions
    this
  }

  override def withCatalystExtensions(catalystExtensionsBuilder: CatalystExtensionsBuilder): OrchestratorSessionBuilder = {
    catalystExtensionsBuilders += catalystExtensionsBuilder
    this
  }

  override def withCheckRulerExtensions(CheckRulerExtensionsBuilder: CheckRulerExtensionsBuilder): OrchestratorSessionBuilder = {
    checkRulerExtensionsBuilders += CheckRulerExtensionsBuilder
    this
  }

  override def withOperationExtensions(operationExtensionsBuilder: OperationExtensionsBuilder): OrchestratorSessionBuilder = {
    operationExtensionsBuilders += operationExtensionsBuilder
    this
  }

  protected def createCatalystExtensions(): CatalystExtensions = new CatalystExtensionsImpl
  protected def createCheckRulerExtensions(): CheckRulerExtensions = new CheckRulerExtensionsImpl
  protected def createOperationExtensions(): OperationExtensions = new OperationExtensionsImpl

  override def getOrCreate(): OrchestratorSession = if(orchestrator.getActiveOrchestratorSession != null) orchestrator.getActiveOrchestratorSession
  else {
    initBuilders[CatalystExtensions](catalystExtensionsBuilders, createCatalystExtensions(), classOf[CatalystExtensions])
    initBuilders[CheckRulerExtensions](checkRulerExtensionsBuilders, createCheckRulerExtensions(), classOf[CheckRulerExtensions])
    initBuilders[OperationExtensions](operationExtensionsBuilders, createOperationExtensions(), classOf[OperationExtensions])
    //extensions.find(_.isInstanceOf[CatalystExtensions]).foreach( extensions => catalystExtensionsBuilders.foreach( builder => builder(extensions.asInstanceOf[CatalystExtensions])))
    val orchestratorSession = createOrchestratorSession { implicit orchestratorSession =>
      val transforms = getTransforms
      val checkRulers = getCheckRulers
      val operations = getOperations
      val extractExtensions = extensions.filterNot(_.isInstanceOf[CatalystExtensions])
        .filterNot(_.isInstanceOf[OperationExtensions]).filterNot(_.isInstanceOf[CheckRulerExtensions]).toArray
      createSessionState(orchestratorSession, transforms, checkRulers, operations, extractExtensions)
    }
    orchestratorSession.initialize(configMap.toMap)
    orchestrator.setActiveOrchestratorSession(orchestratorSession)
    orchestratorSession
  }

  private def initBuilders[E](builders: ArrayBuffer[E => Unit], create: => E, clazz: Class[E]): Unit = if(builders.nonEmpty) extensions.find{ extension =>
    extension.getClass == clazz
  }.orElse {
    val extension = create
    extension match {
      case extension: Extensions[_] =>
        extensions += extension
      case _=>
    }
    Option(extension)
  }.foreach { case extensions: E => builders.foreach( _(extensions)) }

  private def getTransforms(implicit orchestratorSession: OrchestratorSession): Array[Transform[_, _, _]] = {
    val extensionsBuffer: ArrayBuffer[CatalystExtensions]  = extensions.filter(_.isInstanceOf[CatalystExtensions]).map(_.asInstanceOf[CatalystExtensions])
    val resBuffer = new ArrayBuffer[Transform[_, _, _]]()
    extensionsBuffer.foreach { extension =>
      resBuffer ++= extension.build(orchestratorSession)
    }
    resBuffer.toArray
  }

  private def getCheckRulers(implicit orchestratorSession: OrchestratorSession): Array[CheckRuler[_, _]] = {
    val extensionsBuffer: ArrayBuffer[CheckRulerExtensions]  = extensions.filter(_.isInstanceOf[CheckRulerExtensions]).map(_.asInstanceOf[CheckRulerExtensions])
    val resBuffer = new ArrayBuffer[CheckRuler[_, _]]()
    extensionsBuffer.foreach { extension =>
      resBuffer ++= extension.build(orchestratorSession)
    }
    resBuffer.toArray
  }

  private def getOperations(implicit orchestratorSession: OrchestratorSession): Array[Operation[_]] = {
    val extensionsBuffer: ArrayBuffer[OperationExtensions]  = extensions.filter(_.isInstanceOf[OperationExtensions]).map(_.asInstanceOf[OperationExtensions])
    val resBuffer = new ArrayBuffer[Operation[_]]()
    extensionsBuffer.foreach { extension =>
      resBuffer ++= extension.build(orchestratorSession)
    }
    resBuffer.toArray
  }

  protected def createSessionState(orchestratorSession: OrchestratorSession,
                                   transforms: Array[Transform[_, _, _]],
                                   checkRulers: Array[CheckRuler[_, _]],
                                   operations: Array[Operation[_]],
                                   extractExtensions: Array[Extensions[_]]): SessionState

  protected def createOrchestratorSession(createSessionState: OrchestratorSession => SessionState): OrchestratorSession

}