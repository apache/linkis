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

package com.webank.wedatasphere.linkis.manager.am.label

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

/**
  * @date 2021/1/13 10:46
  */
@Component
class MultiUserEngineReuseLabelChooser extends EngineReuseLabelChooser with Logging {

  private val multiUserEngine = AMConfiguration.MULTI_USER_ENGINE_TYPES.getValue.split(",")

  /**
    * 过滤掉支持多用户引擎的UserCreator Label
    *
    * @param labelList
    * @return
    */
  override def chooseLabels(labelList: util.List[Label[_]]): util.List[Label[_]] = {
    val labels = labelList.asScala
    val engineTypeLabelOption = labels.find(_.isInstanceOf[EngineTypeLabel])
    if (engineTypeLabelOption.isDefined) {
      val engineTypeLabel = engineTypeLabelOption.get.asInstanceOf[EngineTypeLabel]
      val maybeString = multiUserEngine.find(_.equalsIgnoreCase(engineTypeLabel.getEngineType))
      if (maybeString.isDefined) {
        info("For multi user engine remove userCreatorLabel")
        return labels.filterNot(_.isInstanceOf[UserCreatorLabel]).asJava
      }
    }
    labelList
  }

}
