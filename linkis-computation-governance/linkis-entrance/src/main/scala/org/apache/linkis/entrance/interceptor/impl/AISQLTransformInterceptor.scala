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

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.common.utils.CodeAndRunTypeUtils.LANGUAGE_TYPE_AI_SQL
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.conf.EntranceConfiguration._
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.utils.EntranceUtils
import org.apache.linkis.governance.common.entity.job.{JobAiRequest, JobRequest}
import org.apache.linkis.governance.common.protocol.job.JobAiReqInsert
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.conf.LabelCommonConfig
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineType, EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.BeanUtils

import java.{lang, util}
import java.util.Date

import scala.collection.JavaConverters._

class AISQLTransformInterceptor extends EntranceInterceptor with Logging {

  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    val aiSqlEnable: Boolean = "true".equals(AI_SQL_KEY.getValue)
    val supportAISQLCreator: String = AI_SQL_CREATORS.toLowerCase()
    val sqlLanguage: String = LANGUAGE_TYPE_AI_SQL
    val sparkEngineType: String = AI_SQL_DEFAULT_SPARK_ENGINE_TYPE
    val hiveEngineType: String = AI_SQL_DEFAULT_HIVE_ENGINE_TYPE
    val labels: util.List[Label[_]] = jobRequest.getLabels
    val codeType: String = LabelUtil.getCodeType(labels)
    // engineType and creator have been verified in LabelCheckInterceptor.
    val userCreatorOpt: Option[Label[_]] = labels.asScala.find(_.isInstanceOf[UserCreatorLabel])
    val creator: String = userCreatorOpt.get.asInstanceOf[UserCreatorLabel].getCreator
    val engineTypeLabelOpt: Option[Label[_]] = labels.asScala.find(_.isInstanceOf[EngineTypeLabel])

    val startMap: util.Map[String, AnyRef] = TaskUtils.getStartupMap(jobRequest.getParams)

    val engineTypeLabel: EngineTypeLabel = engineTypeLabelOpt.get.asInstanceOf[EngineTypeLabel]

    /**
     * aiSql change to spark or hive
     *   1. Use the spark engine when configuring spark parameter templates 2. Use the hive engine
     *      when configuring hive parameter templates 3. Request doctor to get engine type 4. Use
     *      spark by default or exception
     */
    var currentEngineType: String = engineTypeLabel.getStringValue
    if (
        aiSqlEnable && sqlLanguage
          .equals(codeType) && supportAISQLCreator.contains(creator.toLowerCase())
    ) {

      logger.info(s"aisql enable for ${jobRequest.getId}")
      startMap.put(AI_SQL_KEY.key, AI_SQL_KEY.getValue.asInstanceOf[AnyRef])
      startMap.put(RETRY_NUM_KEY.key, RETRY_NUM_KEY.getValue.asInstanceOf[AnyRef])
      logAppender.append(LogUtils.generateInfo(s"current code is aiSql task.\n"))

      // 用户配置了模板参数
      if (startMap.containsKey("ec.resource.name")) {
        val hiveParamKeys: String = AI_SQL_HIVE_TEMPLATE_KEYS
        if (containsKeySubstring(startMap, hiveParamKeys)) {
          changeEngineLabel(hiveEngineType, labels)
          logAppender.append(
            LogUtils.generateInfo(
              s"use $hiveEngineType by set ${startMap.get("ec.resource.name")} template.\n"
            )
          )
          currentEngineType = hiveEngineType
        } else {
          changeEngineLabel(sparkEngineType, labels)
          logAppender.append(
            LogUtils.generateInfo(
              s"use $sparkEngineType by set ${startMap.get("ec.resource.name")} template.\n"
            )
          )
          currentEngineType = sparkEngineType
        }
        logger.info(
          s"use ${startMap.get("ec.resource.name")} conf, use $currentEngineType execute task."
        )
      } else {
        logger.info(s"start intelligent selection execution engine for ${jobRequest.getId}")
        val engineType: String =
          EntranceUtils.getDynamicEngineType(jobRequest.getExecutionCode, logAppender)
        if ("hive".equals(engineType)) {
          changeEngineLabel(hiveEngineType, labels)
          logAppender.append(
            LogUtils.generateInfo(s"use $hiveEngineType by intelligent selection.\n")
          )
          currentEngineType = hiveEngineType
        } else {
          changeEngineLabel(sparkEngineType, labels)
          logAppender.append(
            LogUtils.generateInfo(s"use $sparkEngineType by intelligent selection.\n")
          )
          currentEngineType = sparkEngineType
        }
        logger.info(
          s"end intelligent selection execution engine, and engineType is ${currentEngineType} for ${jobRequest.getId}."
        )
        EntranceUtils.dealsparkDynamicConf(jobRequest, logAppender, jobRequest.getParams)
      }

      persist(jobRequest);
    }

    TaskUtils.addStartupMap(jobRequest.getParams, startMap)
    jobRequest
  }

  private def persist(jobRequest: JobRequest) = {
    val sender: Sender =
      Sender.getSender(Configuration.JOBHISTORY_SPRING_APPLICATION_NAME.getValue)
    val jobAiRequest: JobAiRequest = new JobAiRequest
    BeanUtils.copyProperties(jobRequest, jobAiRequest)
    jobAiRequest.setId(null)
    jobAiRequest.setJobHistoryId(jobRequest.getId + "")
    jobAiRequest.setChangeTime(new Date())
    jobAiRequest.setEngineType(LabelUtil.getEngineType(jobRequest.getLabels))
    jobAiRequest.setSubmitCode(jobRequest.getExecutionCode)
    val jobAiReqInsert: JobAiReqInsert = JobAiReqInsert(jobAiRequest)
    logger.info(s"${jobRequest.getId} insert into ai_history: ${jobAiRequest}")
    sender.ask(jobAiReqInsert)
    logger.info(s"${jobRequest.getId} insert into ai_history end.")
  }

  private def containsKeySubstring(map: util.Map[String, AnyRef], keywords: String): Boolean = {
    if (StringUtils.isBlank(keywords) || map == null || map.isEmpty) {
      false
    } else {
      // 将关键词字符串按逗号分隔成数组
      val keywordArray: Array[String] = keywords.split(",").map(_.trim)

      // 遍历 Map 的键，检查是否包含任何一个关键词
      map.keySet().asScala.exists { key =>
        keywordArray.exists(key.contains)
      }
    }
  }

  private def changeEngineLabel(sparkEngineType: String, labels: util.List[Label[_]]): Unit = {
    val it: util.Iterator[Label[_]] = labels.iterator()
    // 移除引擎标签
    while (it.hasNext) {
      if (it.next().isInstanceOf[EngineTypeLabel]) {
        it.remove()
      }
    }
    // 添加正确的引擎标签
    val newEngineTypeLabel: EngineTypeLabel =
      LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EngineTypeLabel])
    newEngineTypeLabel.setEngineType(sparkEngineType.split("-")(0))
    newEngineTypeLabel.setVersion(sparkEngineType.split("-")(1))
    labels.add(newEngineTypeLabel)
  }

}
