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

package org.apache.linkis.engineconn.core.hook

import org.apache.linkis.common.utils.{ClassUtils, Logging, Utils}
import org.apache.linkis.engineconn.executor.entity.Executor

import scala.collection.JavaConverters.asScalaSetConverter
import scala.collection.mutable.ArrayBuffer

trait ExecutorHook extends Logging {

  def getOrder(): Int = 1

  def getHookName(): String = getClass.getName

  def isAccepted(codeType: String): Boolean

  /*
  Will be called before executor inited, and only be called once.
   */
  def beforeExecutorInit(executor: Executor): Unit = {}

  /*
  Will be called after executor inited, and only be called once.
   */
  def afterExecutorInit(executor: Executor): Unit = {}

}

object ExecutorHook extends Logging {

  private lazy val executorHooks: Array[ExecutorHook] = initExecutorHooks

  private def initExecutorHooks(): Array[ExecutorHook] = {
    val hooks = new ArrayBuffer[ExecutorHook]
    Utils.tryCatch {
      val reflections = ClassUtils.reflections
      val allSubClass = reflections.getSubTypesOf(classOf[ExecutorHook])
      allSubClass.asScala
        .filter(!ClassUtils.isInterfaceOrAbstract(_))
        .foreach(l => {
          hooks += l.newInstance()
        })
    } { t: Throwable =>
      logger.error(t.getMessage)
    }
    hooks.sortWith((a, b) => a.getOrder() <= b.getOrder()).toArray
  }

  def getAllExecutorHooks(): Array[ExecutorHook] = executorHooks

}
