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

package org.apache.linkis.manager.label.entity.engine

import org.apache.linkis.manager.label.entity.{EngineNodeLabel, Feature, GenericLabel}
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum

import java.util

class EngineConnModeLabel extends GenericLabel with EngineNodeLabel {

  setLabelKey("engineConnMode")

  override def getFeature: Feature = Feature.CORE

  @ValueSerialNum(0)
  def setEngineConnMode(engineConnMode: String): Unit = {
    if (null == getValue) setValue(new util.HashMap[String, String])
    getValue.put("engineConnMode", engineConnMode)
  }

  def getEngineConnMode: String = {
    if (null == getValue) return null
    getValue.get("engineConnMode")
  }

}

object EngineConnMode extends Enumeration {
  type EngineConnMode = Value
  val Computation = Value("computation")
  val Once = Value("once")
  val Cluster = Value("cluster")
  val Computation_With_Once = Value("computation_once")
  val Once_With_Cluster = Value("once_cluster")
  val Unknown = Value("unknown")

  implicit def toEngineConnMode(engineConnMode: String): EngineConnMode = engineConnMode match {
    case "computation" => Computation
    case "once" => Once
    case "cluster" => Cluster
    case "computation_once" => Computation_With_Once
    case "once_cluster" => Once_With_Cluster
    case _ => Unknown
  }

  val ONCE_MODES = Set(Once, Computation_With_Once, Once_With_Cluster)

  def isOnceMode(ecMode: String): Boolean = {
    ONCE_MODES.contains(toEngineConnMode(ecMode))
  }

}
