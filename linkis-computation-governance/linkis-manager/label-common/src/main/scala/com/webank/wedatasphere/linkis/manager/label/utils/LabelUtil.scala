/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.manager.label.utils

import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{CodeLanguageLabel, EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.manager.label.entity.entrance.{BindEngineLabel, ExecuteOnceLabel, LoadBalanceLabel}
import com.webank.wedatasphere.linkis.manager.label.entity.route.RouteLabel

import java.util
import scala.collection.JavaConverters.asScalaBufferConverter


object LabelUtil {

  def getEngineTypeLabel(labels: util.List[Label[_]]): EngineTypeLabel = {
    if (null == labels) return null
    var labelRs: EngineTypeLabel = null
    labels.asScala.foreach(l => l match {
      case label: EngineTypeLabel =>
        labelRs = label
        return label
      case _ =>
    })
    labelRs
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
    if (null == labels) return null
    labels.asScala.foreach(l => l match {
      case label: UserCreatorLabel =>
        return label
      case _ =>
    })
    null
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
    if (null == labels) return null
    labels.asScala.foreach(l => l match {
      case label: CodeLanguageLabel =>
        return label
      case _ =>
    })
    null
  }

  def getBindEngineLabel(labels: util.List[Label[_]]): BindEngineLabel = {
    if (null == labels) return null
    labels.asScala.foreach(l => l match {
      case label: BindEngineLabel =>
        return label
      case _ =>
    })
    null
  }

  def getRouteLabel(labels: util.List[Label[_]]): RouteLabel = {
    if (null == labels) return null
    labels.asScala.foreach(l => l match {
      case label: RouteLabel =>
        return label
      case _ =>
    })
    null
  }

  def getExecuteOnceLabel(labels: util.List[Label[_]]) : ExecuteOnceLabel = {
    if (null == labels) return null
    labels.asScala.foreach(l => l match {
      case label: ExecuteOnceLabel =>
        return label
      case _ =>
    })
    null
  }

  def getLoadBalanceLabel(labels: util.List[Label[_]]) : LoadBalanceLabel = {
    if (null == labels) return null
    labels.asScala.foreach(l => l match {
      case label: LoadBalanceLabel =>
        return label
      case _ =>
    })
    null
  }


  /*
  // todo
  def getLabelFromList[A](labels: util.List[Label[_]]): A = {
    if (null == labels) return null.asInstanceOf[A]
    labels.asScala.foreach(l => if (l.isInstanceOf[A]) return l.asInstanceOf[A])
    null.asInstanceOf[A]
  }*/

  def main(args: Array[String]): Unit = {
    /*val labels = new util.ArrayList[Label[_]]()
    val engineTypeLabel = new EngineTypeLabel
    engineTypeLabel.setEngineType("1")
    engineTypeLabel.setVersion("1")
    labels.add(engineTypeLabel)
    val label = getEngineTypeLabel(labels)
    if (null == label) {
      println("Got null EngineTypeLabel.")
    } else {
      println("Got engineTypeLabel : " + label.getStringValue)
    }
    val l1 = getBindEngineLabel(null)
    if (null == l1) {
      println("Got null jobGroupLabel")
    }
    labels.add(new BindEngineLabel().setJobGroupId("1").setIsJobGroupHead(true).setIsJobGroupEnd(true))
    val l2 = getBindEngineLabel(labels)
    if (null == l2) {
      println("Got null jobGroupLabel")
    } else {
      println(s"Got jobGroupLabel : ${l2.getStringValue}")
    }*/
  }

}
