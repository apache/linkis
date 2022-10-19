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

package org.apache.linkis.manager.am.conf

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.server.JMap

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}

import java.util

import scala.collection.JavaConverters._

trait EngineConnConfigurationService {

  def getConsoleConfiguration(label: util.List[Label[_]]): util.Map[String, String]

}

class DefaultEngineConnConfigurationService extends EngineConnConfigurationService with Logging {

  override def getConsoleConfiguration(label: util.List[Label[_]]): util.Map[String, String] = {
    val properties = new JMap[String, String]
    val userCreatorLabelOption = label.asScala.find(_.isInstanceOf[UserCreatorLabel])
    val engineTypeLabelOption = label.asScala.find(_.isInstanceOf[EngineTypeLabel])
    if (userCreatorLabelOption.isDefined) {
      val userCreatorLabel = userCreatorLabelOption.get.asInstanceOf[UserCreatorLabel]
      if (engineTypeLabelOption.isDefined) {
        val engineTypeLabel = engineTypeLabelOption.get.asInstanceOf[EngineTypeLabel]
        val engineConfig = Utils.tryAndWarn(
          ConfigurationMapCache.engineMapCache.getCacheMap((userCreatorLabel, engineTypeLabel))
        )
        if (null != engineConfig) {
          properties.putAll(engineConfig)
        }
      }
    }
    properties
  }

}

@Configuration
class ApplicationManagerSpringConfiguration {

  @ConditionalOnMissingBean
  @Bean
  def getDefaultEngineConnConfigurationService: EngineConnConfigurationService = {
    new DefaultEngineConnConfigurationService
  }

}
