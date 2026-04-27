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
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.conf.{
  SecondaryYarnRequest,
  SecondaryYarnResponse
}
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.StringUtils

import java.{lang, util}

import scala.concurrent.duration.Duration

/**
 * QueueSelectionInterceptor: Intelligent queue selection interceptor at Entrance layer.
 *
 * This interceptor performs the following functions:
 *   1. Check if the queue selection feature is enabled 2. Check if the engine type is in the
 *      supported list 3. Check if the creator is in the supported list 4. Call LinkisManager RPC to
 *      get the optimal queue 5. Replace the queue configuration in runtimeParams
 */
class QueueSelectionInterceptor extends EntranceInterceptor with Logging {

  private val RPC_TIMEOUT = Duration(5, "seconds") // RPC call timeout: 5 seconds

  /**
   * Apply queue selection logic to the job request.
   *
   * @param jobRequest
   *   The job request to process
   * @param logAppender
   *   Used to cache necessary reminder logs and pass them to the upper layer
   * @return
   *   The processed job request
   */
  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    val taskId = jobRequest.getId.toString
    Utils.tryAndWarn {
      // 1. Check if queue selection is enabled
      if (!Configuration.SECONDARY_QUEUE_ENABLED.getValue) {
        logger.info(s"Queue selection is not enabled at Entrance layer")
        return jobRequest
      }

      // 2. Get labels from job request
      val labels = jobRequest.getLabels
      if (labels == null || labels.isEmpty) {
        logger.info(s"No labels found in job request, skip queue selection")
        return jobRequest
      }

      // 3. Extract engine type and creator (with null safety)
      val userCreatorLabel = LabelUtil.getUserCreatorLabel(labels)
      val creator = if (userCreatorLabel != null) userCreatorLabel.getCreator else null

      val engineTypeLabel = LabelUtil.getEngineTypeLabel(labels)
      val engineType = if (engineTypeLabel != null) engineTypeLabel.getEngineType else null

      val supportedEngines = Configuration.SECONDARY_QUEUE_ENGINES.getValue
        .split(",")
        .map(_.trim.toLowerCase())
        .toSet
      val supportedCreators = Configuration.SECONDARY_QUEUE_CREATORS.getValue
        .split(",")
        .map(_.trim.toUpperCase())
        .toSet

      val engineMatched =
        engineType == null || supportedEngines.contains(engineType.toLowerCase())
      val creatorMatched = creator == null || supportedCreators.contains(creator.toUpperCase())

      if (!engineMatched || !creatorMatched) {
        logger.info(
          s"Engine type or creator not in supported list - engineType: $engineType (matched: $engineMatched), creator: $creator (matched: $creatorMatched)"
        )
        return jobRequest
      }
      val startMap: util.Map[String, AnyRef] = TaskUtils.getStartupMap(jobRequest.getParams)
      // 4. Call LinkisManager RPC with timeout and error handling
      val sender = Sender.getSender(Configuration.MANAGER_SPRING_APPLICATION_NAME.getValue)
      val response = sender.ask(SecondaryYarnRequest(taskId, startMap, labels)) match {
        case r: SecondaryYarnResponse =>
          logger.info(s"Received RPC response: ${r.selectQueue}")
          r
        case other =>
          logger.warn(s"Unexpected response type: ${other.getClass.getName}, using primary queue")
          null
      }

      if (response == null) {
        logger.info(s"RPC returned null or timed out, using primary queue")
        logAppender.append(
          LogUtils.generateInfo(
            "Queue selection failed: unable to get response from LinkisManager, using primary queue\n"
          )
        )
        return jobRequest
      }

      val yarnQueue = response.selectQueue
      val primaryQueue = response.primaryQueue
      val secondaryQueue = response.secondaryQueue

      // 5. Update queue configuration in startupMap
      if (StringUtils.isNotBlank(yarnQueue)) {
        startMap.put(ECConstants.YARN_QUEUE_NAME_CONFIG_KEY, yarnQueue)
        logger.info(
          s"Queue selection completed - primary: $primaryQueue, secondary: $secondaryQueue, selected: $yarnQueue"
        )
        logAppender.append(
          LogUtils.generateInfo(
            s"Queue selection completed - primary: $primaryQueue, secondary: $secondaryQueue, selected: $yarnQueue\n"
          )
        )
        TaskUtils.addStartupMap(jobRequest.getParams, startMap)
      } else {
        logger.info(s"Selected queue is blank, using primary queue")
        logAppender.append(
          LogUtils
            .generateInfo(s"Queue selection error - selected queue is blank, using primary queue\n")
        )
      }
    }
    jobRequest
  }

}
