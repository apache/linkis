/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.entranceclient.conf

import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entranceclient
import org.springframework.context.annotation.{Condition, ConditionContext}
import org.springframework.core.`type`.AnnotatedTypeMetadata

class MultiEntranceCondition extends Condition {
  override def matches(conditionContext: ConditionContext, annotatedTypeMetadata: AnnotatedTypeMetadata): Boolean =
    entranceclient.conf.MultiEntranceCondition.isMultiEntranceApplication
}
class SingleEntranceCondition extends Condition {
  override def matches(conditionContext: ConditionContext, annotatedTypeMetadata: AnnotatedTypeMetadata): Boolean =
    !entranceclient.conf.MultiEntranceCondition.isMultiEntranceApplication
}
object MultiEntranceCondition {
  def isMultiEntranceApplication: Boolean = EntranceConfiguration.MULTI_ENTRANCE_CONDITION.getValue
}