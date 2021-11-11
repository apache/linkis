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
 
package org.apache.linkis.orchestrator.plans

/**
  *
  */
trait PlanContext {

  def get(key: String): Any

  def getOption(key: String): Option[Any]

  def exists(key: String): Boolean

  def getOrElse(key: String, defaultValue: Any): Any

  def getOrElsePut(key: String, defaultValue: Any): Any

  def orElse(key: String, defaultValue: Any): Option[Any]

  def orElsePut(key: String, defaultValue: Any): Option[Any]

  def set(key: String, value: Any): Unit

}

abstract class AbstractPlanContext extends PlanContext {

  override def getOption(key: String): Option[Any] = Option(get(key))

  override def getOrElse(key: String, defaultValue: Any): Any = {
    val value = get(key)
    if(value != null) value else defaultValue
  }

  override def getOrElsePut(key: String, defaultValue: Any): Any = synchronized {
    val value = get(key)
    if(value != null) value else {
      set(key, defaultValue)
      defaultValue
    }
  }

  override def orElse(key: String, defaultValue: Any): Option[Any] = {
    val value = get(key)
    if(value != null) Some(value) else Option(defaultValue)
  }

  override def orElsePut(key: String, defaultValue: Any): Option[Any] = getOption(key).orElse {
    set(key, defaultValue)
    Option(defaultValue)
  }
}

class SimplifyPlanContext extends AbstractPlanContext {

  private val contextMap = new java.util.HashMap[String, Any]

  override def get(key: String): Any = contextMap.get(key)

  override def set(key: String, value: Any): Unit = contextMap.put(key, value)

  override def exists(key: String): Boolean = contextMap.containsKey(key)

}