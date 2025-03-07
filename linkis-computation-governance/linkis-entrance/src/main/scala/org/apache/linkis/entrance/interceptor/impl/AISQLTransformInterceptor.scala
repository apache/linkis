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

package org.apache.linkis.entrance.interceptor.impl

import org.apache.linkis.common.utils.CodeAndRunTypeUtils.LANGUAGE_TYPE_AI_SQL
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.utils.TaskUtils

import java.{lang, util}
import scala.collection.JavaConverters._

class AISQLTransformInterceptor extends EntranceInterceptor {

  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    // TODO 修改为变量
    val aiSqlEnable = true
    // 转换为小写
    val supportAISQLCreator = "IDE"
    val sqlLanguage: String = LANGUAGE_TYPE_AI_SQL
    val sparkEngineType = "spark-3.4.4"
    val labels: util.List[Label[_]] = jobRequest.getLabels
    val codeType: String = LabelUtil.getCodeType(labels)
    // engineType and creator have been verified in LabelCheckInterceptor.
    val userCreatorOpt: Option[Label[_]] = labels.asScala.find(_.isInstanceOf[UserCreatorLabel])
    val creator: String = userCreatorOpt.get.asInstanceOf[UserCreatorLabel].getCreator
    val engineTypeLabelOpt: Option[Label[_]] = labels.asScala.find(_.isInstanceOf[EngineTypeLabel])
    // aiSql change to spark
    var currentEngineType: String =
      engineTypeLabelOpt.get.asInstanceOf[EngineTypeLabel].getEngineType
    if (
        aiSqlEnable && sqlLanguage
          .equals(codeType) && supportAISQLCreator.contains(creator.toLowerCase())
    ) {
      engineTypeLabelOpt.get.asInstanceOf[EngineTypeLabel].setEngineType(sparkEngineType)
      // TODO 添加 aisql 标识
      currentEngineType = sparkEngineType

      // TODO 将转换后的数据保存到数据库
    }
    // 开启 spark 动态资源规划, spark3.4.4
    if (sparkEngineType.equals(currentEngineType)) {
      val startMap: util.Map[String, AnyRef] = TaskUtils.getStartupMap(jobRequest.getParams)
      // add spark dynamic resource planning
      // TODO
      startMap.put("", "")
    }
    jobRequest
  }

}
