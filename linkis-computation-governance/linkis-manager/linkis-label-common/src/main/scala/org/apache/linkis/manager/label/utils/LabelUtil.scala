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

import org.apache.linkis.manager.label.entity.{Label, TenantLabel}
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

import org.apache.commons.lang3.StringUtils

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

  def getTenantValue(labels: util.List[Label[_]]): String = {
    if (null == labels) return ""
    val tentantLabel = getTenantLabel(labels)
    if (null != tentantLabel) {
      tentantLabel.getTenant
    } else {
      ""
    }
  }

  def getTenantLabel(labels: util.List[Label[_]]): TenantLabel = {
    getLabelFromList[TenantLabel](labels)
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

  def getFromLabelStr(labelStr: String, key: String): String = {
    //  hadoop-IDE,hive-2.3.3  or hadoop-IDE  or hive-2.3.3
    if (StringUtils.isNotBlank(labelStr)) {
      val labelArray = labelStr.split(",")
      (labelArray.length, key.toLowerCase()) match {
        case (1, "user") => labelStr.split("-")(0)
        case (1, "creator") => labelStr.split("-")(1)
        case (1, "engine") => labelStr.split("-")(0)
        case (1, "version") => labelStr.split("-")(1)
        case (2, "user") => labelArray(0).split("-")(0)
        case (2, "creator") => labelArray(0).split("-")(1)
        case (2, "engine") => labelArray(1).split("-")(0)
        case (2, "version") => labelArray(1).split("-")(1)
      }
    } else {
      ""
    }
  }

}
