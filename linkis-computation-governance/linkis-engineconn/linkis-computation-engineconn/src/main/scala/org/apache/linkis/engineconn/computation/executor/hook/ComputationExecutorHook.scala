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

package org.apache.linkis.engineconn.computation.executor.hook

import org.apache.linkis.common.utils.{ClassUtils, Logging, Utils}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext

import scala.collection.JavaConverters.asScalaSetConverter
import scala.collection.mutable.ArrayBuffer

trait ComputationExecutorHook {

  def getOrder(): Int = 1

  def getHookName(): String

  def beforeExecutorExecute(
      engineExecutionContext: EngineExecutionContext,
      engineCreationContext: EngineCreationContext,
      codeBeforeHook: String
  ): String = codeBeforeHook

}

object ComputationExecutorHook extends Logging {

  private lazy val computationExecutorHooks: Array[ComputationExecutorHook] =
    initComputationExecutorHook

  private def initComputationExecutorHook: Array[ComputationExecutorHook] = {
    val hooks = new ArrayBuffer[ComputationExecutorHook]
    Utils.tryCatch {
      val reflections = ClassUtils.reflections
      val allSubClass = reflections.getSubTypesOf(classOf[ComputationExecutorHook])
      allSubClass.asScala
        .filter(!ClassUtils.isInterfaceOrAbstract(_))
        .foreach(l => {
          hooks += l.newInstance
        })
    } { t: Throwable =>
      logger.error(t.getMessage)
    }
    hooks.sortWith((a, b) => a.getOrder() <= b.getOrder()).toArray
  }

  def getComputationExecutorHooks: Array[ComputationExecutorHook] = computationExecutorHooks
}
