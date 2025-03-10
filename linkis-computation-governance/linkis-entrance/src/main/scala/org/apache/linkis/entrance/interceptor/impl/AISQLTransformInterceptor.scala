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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.common.utils.CodeAndRunTypeUtils.LANGUAGE_TYPE_AI_SQL
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.conf.EntranceConfiguration._
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.governance.common.entity.job.{JobAiRequest, JobRequest}
import org.apache.linkis.governance.common.protocol.job.JobAiReqInsert
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
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
    val aiSqlEnable: Boolean = AI_SQL_ENABLED
    val supportAISQLCreator: String = AI_SQL_CREATORS.toLowerCase()
    val sqlLanguage: String = LANGUAGE_TYPE_AI_SQL
    val sparkEngineType: String = AI_SQL_DEFAULT_SPARK_ENGINE_TYPE
    val labels: util.List[Label[_]] = jobRequest.getLabels
    val codeType: String = LabelUtil.getCodeType(labels)
    // engineType and creator have been verified in LabelCheckInterceptor.
    val userCreatorOpt: Option[Label[_]] = labels.asScala.find(_.isInstanceOf[UserCreatorLabel])
    val creator: String = userCreatorOpt.get.asInstanceOf[UserCreatorLabel].getCreator
    val engineTypeLabelOpt: Option[Label[_]] = labels.asScala.find(_.isInstanceOf[EngineTypeLabel])

    val startMap: util.Map[String, AnyRef] = TaskUtils.getStartupMap(jobRequest.getParams)

    val engineTypeLabel: EngineTypeLabel = engineTypeLabelOpt.get.asInstanceOf[EngineTypeLabel]
    // aiSql change to spark
    var currentEngineType: String = engineTypeLabel.toString
    if (
        aiSqlEnable && sqlLanguage
          .equals(codeType) && supportAISQLCreator.contains(creator.toLowerCase())
    ) {
      val it: util.Iterator[Label[_]] = labels.iterator()
      while (it.hasNext) {
        if (it.next().isInstanceOf[EngineTypeLabel]) {
          it.remove()
        }
      }
      val newEngineTypeLabel: EngineTypeLabel =
        LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EngineTypeLabel])
      newEngineTypeLabel.setEngineType(sparkEngineType.split("-")(0))
      newEngineTypeLabel.setVersion(sparkEngineType.split("-")(1))
      newEngineTypeLabel.setStringValue(sparkEngineType)
      labels.add(newEngineTypeLabel)
      startMap.put(AI_SQL_KEY.key, AI_SQL_KEY.getValue.asInstanceOf[AnyRef])
      startMap.put(RETRY_NUM_KEY.key, RETRY_NUM_KEY.getValue.asInstanceOf[AnyRef])

      currentEngineType = sparkEngineType

      persist(jobRequest);

    }
    // 开启 spark 动态资源规划, spark3.4.4
    if (sparkEngineType.equals(currentEngineType)) {
      logger.info("spark3 add dynamic resource.")

      // add spark dynamic resource planning
      startMap.put(
        "spark.shuffle.service.enabled",
        SPARK_SHUFFLE_SERVICE_ENABLED.asInstanceOf[AnyRef]
      )
      startMap.put(
        "spark.dynamicAllocation.enabled",
        SPARK_DYNAMIC_ALLOCATION_ENABLED.asInstanceOf[AnyRef]
      )
      startMap.put(
        "spark.dynamicAllocation.minExecutors",
        SPARK_DYNAMIC_ALLOCATION_MIN_EXECUTORS.asInstanceOf[AnyRef]
      )
      startMap.put(
        "spark.dynamicAllocation.maxExecutors",
        SPARK_DYNAMIC_ALLOCATION_MAX_EXECUTORS.asInstanceOf[AnyRef]
      )
      startMap.put("spark.executor.cores", SPARK_EXECUTOR_CORES.asInstanceOf[AnyRef])
      startMap.put("spark.executor.memory", SPARK_EXECUTOR_MEMORY.asInstanceOf[AnyRef])
      startMap.put("spark.executor.instances", SPARK_EXECUTOR_INSTANCES.asInstanceOf[AnyRef])

      Utils.tryAndWarn {
        val extraConfs: String = SPARK_DYNAMIC_ALLOCATION_ADDITIONAL_CONFS
        if (StringUtils.isNotBlank(extraConfs)) {
          val confs: Array[String] = extraConfs.split(",")
          for (conf <- confs) {
            val confKey: String = conf.split("=")(0)
            val confValue: String = conf.split("=")(1)
            startMap.put(confKey, confValue)
          }
        }
      }

    }
    TaskUtils.addStartupMap(jobRequest.getParams, startMap)
    jobRequest
  }

  private def persist(jobRequest: JobRequest) = {
    val sender: Sender =
      Sender.getSender(EntranceConfiguration.JOBHISTORY_SPRING_APPLICATION_NAME.getValue)
    val jobAiRequest: JobAiRequest = new JobAiRequest
    BeanUtils.copyProperties(jobRequest, jobAiRequest)
    jobAiRequest.setId(null)
    jobAiRequest.setJobHistoryId(jobRequest.getId + "")
    jobAiRequest.setChangeTime(new Date())
    val jobAiReqInsert: JobAiReqInsert = JobAiReqInsert(jobAiRequest)
    logger.info(s"${jobRequest.getId} insert into ai_history: ${jobAiRequest}")
    sender.ask(jobAiReqInsert)
    logger.info(s"${jobRequest.getId} insert into ai_history end.")
  }

}
