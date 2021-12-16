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
 
package org.apache.linkis.governance.common.protocol.conf

import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.protocol.message.RequestProtocol
import org.apache.linkis.protocol.{CacheableProtocol, RetryableProtocol}

trait ConfigProtocol

case class RequestQueryGlobalConfig(username: String) extends CacheableProtocol with RetryableProtocol with ConfigProtocol with RequestProtocol {
  override def toString: String = {
    RequestQueryGlobalConfig.getClass.getName + "," + username
  }
}

case class RequestQueryEngineConfig(userCreatorLabel: UserCreatorLabel, engineTypeLabel: EngineTypeLabel, filter: String = null) extends CacheableProtocol with RetryableProtocol with ConfigProtocol{
  override def toString: String = {
    RequestQueryEngineConfig.getClass.getName + "," + userCreatorLabel.getStringValue + "," + engineTypeLabel.getStringValue
  }
}

case class RequestQueryEngineConfigWithGlobalConfig(userCreatorLabel: UserCreatorLabel, engineTypeLabel: EngineTypeLabel, filter: String = null) extends CacheableProtocol with RetryableProtocol with ConfigProtocol{
  override def toString: String = {
    RequestQueryEngineConfigWithGlobalConfig.getClass.getName + "," + userCreatorLabel.getStringValue + "," + engineTypeLabel.getStringValue
  }
}


case class RequestQueryEngineTypeDefault(engineTypeLabel: EngineTypeLabel) extends CacheableProtocol with RetryableProtocol with ConfigProtocol

case class RequestConfigByLabel(labels: java.util.List[Label[_]], isMerge: Boolean = true) extends CacheableProtocol with RetryableProtocol with ConfigProtocol

