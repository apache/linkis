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

package org.apache.linkis.manager.label.utils

import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{
  CodeLanguageLabel,
  EngineConnModeLabel,
  EngineTypeLabel,
  UserCreatorLabel
}
import org.apache.linkis.manager.label.entity.entrance.{
  BindEngineLabel,
  ExecuteOnceLabel,
  LoadBalanceLabel
}
import org.apache.linkis.manager.label.entity.route.RouteLabel

import java.util

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.reflect.ClassTag

object LabelUtil {

  def getEngineTypeLabel(labels: util.List[Label[_]]): EngineTypeLabel = {
    getLabelFromList[EngineTypeLabel](labels)
  }

  def getEngineType(labels: util.List[Label[_]]): String = {
    if (null == labels) return null
    val engineTypeLabel = getEngineTypeLabel(labels)
    if (null != engineTypeLabel) {
      engineTypeLabel.getEngineType
    } else {
      null
    }
  }

  def getUserCreatorLabel(labels: util.List[Label[_]]): UserCreatorLabel = {
    getLabelFromList[UserCreatorLabel](labels)
  }

  def getUserCreator(labels: util.List[Label[_]]): (String, String) = {
    if (null == labels) return null
    val userCreatorLabel = getUserCreatorLabel(labels)
    if (null != userCreatorLabel) {
      (userCreatorLabel.getUser, userCreatorLabel.getCreator)
    } else {
      null
    }
  }

  def getCodeType(labels: util.List[Label[_]]): String = {
    if (null == labels) return null
    val codeTypeLabel = getCodeTypeLabel(labels)
    if (null != codeTypeLabel) {
      codeTypeLabel.getCodeType
    } else {
      null
    }
  }

  def getCodeTypeLabel(labels: util.List[Label[_]]): CodeLanguageLabel = {
    getLabelFromList[CodeLanguageLabel](labels)
  }

  def getEngineConnModeLabel(labels: util.List[Label[_]]): EngineConnModeLabel = {
    getLabelFromList[EngineConnModeLabel](labels)
  }

  def getEngineConnMode(labels: util.List[Label[_]]): String = {
    if (null == labels) return null
    val engineConnModeLabel = getEngineConnModeLabel(labels)
    if (null != engineConnModeLabel) {
      engineConnModeLabel.getEngineConnMode
    } else {
      null
    }
  }

  def getBindEngineLabel(labels: util.List[Label[_]]): BindEngineLabel = {
    getLabelFromList[BindEngineLabel](labels)
  }

  def getRouteLabel(labels: util.List[Label[_]]): RouteLabel = {
    getLabelFromList[RouteLabel](labels)
  }

  def getExecuteOnceLabel(labels: util.List[Label[_]]): ExecuteOnceLabel = {
    getLabelFromList[ExecuteOnceLabel](labels)
  }

  def getLoadBalanceLabel(labels: util.List[Label[_]]): LoadBalanceLabel = {
    getLabelFromList[LoadBalanceLabel](labels)
  }

  def getLabelFromList[A: ClassTag](labels: util.List[Label[_]]): A = {
    if (null == labels) return null.asInstanceOf[A]
    labels.asScala.foreach {
      case label: A =>
        return label
      case _ =>
    }
    null.asInstanceOf[A]
  }

  def getLabelFromArray[A: ClassTag](labels: Array[Label[_]]): A = {
    if (null == labels) return null.asInstanceOf[A]
    labels.foreach {
      case label: A =>
        return label
      case _ =>
    }
    null.asInstanceOf[A]
  }

}
