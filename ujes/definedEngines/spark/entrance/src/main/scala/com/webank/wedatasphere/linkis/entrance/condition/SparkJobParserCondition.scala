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

package com.webank.wedatasphere.linkis.entrance.condition

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import org.springframework.context.annotation.{Condition, ConditionContext}
import org.springframework.core.`type`.AnnotatedTypeMetadata

/**
  * Created by allenlliu on 2019/4/22.
  */
class SparkJobParserCondition extends Condition{
  val condition = CommonVars("wds.linkis.ujes.spark.tuning.switch",false).getValue
  override def matches(conditionContext: ConditionContext, annotatedTypeMetadata: AnnotatedTypeMetadata): Boolean = {
     condition
  }
}
