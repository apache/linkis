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
 
package org.apache.linkis.orchestrator.ecm

import java.util

import org.apache.linkis.common.utils.{ClassUtils, Logging, Utils}
import org.apache.linkis.orchestrator.ecm.conf.ECMPluginConf
import org.apache.linkis.orchestrator.ecm.entity.Policy


/**
  *
  *
  */
trait EngineConnManagerBuilder {

  def setPolicy(policy: Policy): EngineConnManagerBuilder

  def setParallelism(parallelism: Int): EngineConnManagerBuilder

  def setMarkApplyTime(applyTime: Long): EngineConnManagerBuilder

  def setMarkApplyAttempts(attemptNumber: Int): EngineConnManagerBuilder

  def build(): EngineConnManager

}

class DefaultEngineConnManagerBuilder extends EngineConnManagerBuilder with Logging {


  private var policy: Policy = _

  private var parallelism: Int = ECMPluginConf.ECM_ENGINE_PARALLELISM.getValue

  private var applyTime: Long = ECMPluginConf.ECM_MARK_APPLY_TIME.getValue.toLong

  private var attemptNumber: Int = ECMPluginConf.ECM_MARK_ATTEMPTS.getValue


  override def setPolicy(policy: Policy): EngineConnManagerBuilder = {
    this.policy = policy
    this
  }

  override def setParallelism(parallelism: Int): EngineConnManagerBuilder = {
    this.parallelism = parallelism
    this
  }

  override def setMarkApplyTime(applyTime: Long): EngineConnManagerBuilder = {
    this.applyTime = applyTime
    this
  }

  override def setMarkApplyAttempts(attemptNumber: Int): EngineConnManagerBuilder = {
    this.attemptNumber = attemptNumber
    this
  }

  override def build(): EngineConnManager = {
    if (null == policy) this.policy = Policy.Process
    val engineManagerClazz = EngineConnManagerBuilder.getEngineManagerClazzByPolicy(this.policy)
    val manager = engineManagerClazz.newInstance()
    manager.setEngineConnApplyAttempts(this.attemptNumber)
    manager.setEngineConnApplyTime(applyTime)
    manager.setParallelism(parallelism)
    manager
  }


}

object EngineConnManagerBuilder extends Logging {

  private val engineConnManagerClazzCache = new util.HashMap[String, Class[_ <: EngineConnManager]]()

  private def init(): Unit = {

    val reflections = ClassUtils.reflections

    val allSubClass = reflections.getSubTypesOf(classOf[EngineConnManager])

    if (null != allSubClass) {
      val iterator = allSubClass.iterator()
      while (iterator.hasNext) {
        val clazz = iterator.next()
        Utils.tryCatch {
          if (! ClassUtils.isInterfaceOrAbstract(clazz)) {
            val manager = clazz.newInstance()
            if (engineConnManagerClazzCache.containsKey(manager.getPolicy().name())) {
              throw new RuntimeException(s"EngineConnManager Type cannot be duplicated ${manager.getPolicy()} ")
            }
            engineConnManagerClazzCache.put(manager.getPolicy().name(), clazz)
          }
        } { t: Throwable =>
          warn(s"Failed to Instantiation: ${clazz.getName}, reason ${t.getMessage}")
          null
        }
      }
    }

  }

  init()

  def getEngineManagerClazzByPolicy(policy: Policy): Class[_ <: EngineConnManager] = {
    engineConnManagerClazzCache.get(policy.name())
  }

  def builder: EngineConnManagerBuilder = new DefaultEngineConnManagerBuilder

}