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

package org.apache.linkis.manager.am.selector

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineConnMode, EngineConnModeLabel}
import org.apache.linkis.manager.label.utils.LabelUtil

import java.util

trait ECAvailableRule {

  def isNeedAvailable(labels: util.List[Label[_]]): Boolean
}

class DefaultECAvailableRule extends ECAvailableRule with Logging {

  override def isNeedAvailable(labels: util.List[Label[_]]): Boolean = {
    val engineConnModeLabel = LabelUtil.getLabelFromList[EngineConnModeLabel](labels)
    if (
        null != engineConnModeLabel && EngineConnMode.Once.toString.equalsIgnoreCase(
          engineConnModeLabel.getEngineConnMode
        )
    ) {
      false
    } else {
      true
    }
  }

}

object ECAvailableRule {

  private val ecAvailableRule: ECAvailableRule = new DefaultECAvailableRule

  def getInstance: ECAvailableRule = this.ecAvailableRule

}
