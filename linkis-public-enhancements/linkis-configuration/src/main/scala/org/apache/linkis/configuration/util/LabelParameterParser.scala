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

package org.apache.linkis.configuration.util

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.configuration.errorcode.LinkisConfigurationErrorCodeSummary._
import org.apache.linkis.configuration.errorcode.LinkisConfigurationErrorCodeSummary.THE_LABEL_PARAMETER_IS_EMPTY
import org.apache.linkis.configuration.exception.ConfigurationException
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineType, EngineTypeLabel, UserCreatorLabel}

import org.springframework.util.CollectionUtils

import java.text.MessageFormat
import java.util
import java.util.Locale

import scala.collection.JavaConverters._

object LabelParameterParser {
  private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  def getDefaultEngineVersion(engineType: String): String = {
    var returnType: CommonVars[String] = null
    EngineType.values.foreach(DefinedEngineType => {
      if (DefinedEngineType.toString.equals(engineType.toLowerCase(Locale.ROOT))) {
        returnType = engineType.toLowerCase(Locale.ROOT) match {
          case "spark" => GovernanceCommonConf.SPARK_ENGINE_VERSION
          case "hive" => GovernanceCommonConf.HIVE_ENGINE_VERSION
          case "python" => GovernanceCommonConf.PYTHON_ENGINE_VERSION
          case _ =>
            throw new ConfigurationException(
              MessageFormat.format(CONFIGURATION_NOT_TYPE.getErrorDesc(), engineType)
            )
        }
      }
    })
    if (returnType == null) {
      throw new ConfigurationException(
        MessageFormat.format(CORRESPONDING_ENGINE_TYPE.getErrorDesc(), engineType)
      )
    }

    returnType.getValue
  }

  def changeUserToDefault(
      labelList: java.util.List[Label[_]],
      withCreator: Boolean = true,
      withUser: Boolean = true
  ): java.util.List[Label[_]] = {
    val newList = new util.LinkedList[Label[_]]()
    if (labelList != null) {
      labelList.asScala.foreach(label => {
        if (label.isInstanceOf[UserCreatorLabel]) {
          val newLabel = labelBuilderFactory.createLabel(classOf[UserCreatorLabel])
          if (withUser) newLabel.setUser("*")
          else newLabel.setUser(label.asInstanceOf[UserCreatorLabel].getUser)
          if (withCreator) newLabel.setCreator("*")
          else newLabel.setCreator(label.asInstanceOf[UserCreatorLabel].getCreator)
          newList.addLast(newLabel)
        } else {
          newList.addLast(label)
        }
      })
    }
    newList
  }

  def labelCheck(labelList: java.util.List[Label[_]]): Boolean = {
    if (!CollectionUtils.isEmpty(labelList)) {
      labelList.asScala.foreach {
        case a: UserCreatorLabel => Unit
        case a: EngineTypeLabel => Unit
        case label =>
          throw new ConfigurationException(
            MessageFormat.format(TYPE_OF_LABEL_NOT_SUPPORTED.getErrorDesc(), label.getClass)
          )
      }
      true
    } else {
      throw new ConfigurationException(THE_LABEL_PARAMETER_IS_EMPTY.getErrorDesc())
    }
  }

}
