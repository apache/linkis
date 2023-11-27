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

package org.apache.linkis.entrance.scheduler

import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary._
import org.apache.linkis.entrance.exception.{EntranceErrorCode, EntranceErrorException}
import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.entrance.utils.EntranceUtils
import org.apache.linkis.governance.common.protocol.conf.{
  RequestQueryEngineConfigWithGlobalConfig,
  ResponseQueryConfig
}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.queue.{Group, GroupFactory, SchedulerEvent}
import org.apache.linkis.scheduler.queue.parallelqueue.ParallelGroup

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import com.google.common.cache.{Cache, CacheBuilder}

class EntranceGroupFactory extends GroupFactory with Logging {

  private val groupNameToGroups: Cache[String, Group] = CacheBuilder
    .newBuilder()
    .expireAfterAccess(EntranceConfiguration.GROUP_CACHE_EXPIRE_TIME.getValue, TimeUnit.MINUTES)
    .maximumSize(EntranceConfiguration.GROUP_CACHE_MAX.getValue)
    .build()

  private val GROUP_MAX_CAPACITY = CommonVars("wds.linkis.entrance.max.capacity", 1000)

  private val SPECIFIED_USERNAME_REGEX =
    CommonVars("wds.linkis.entrance.specified.username.regex", "hduser.*")

  private val GROUP_SPECIFIED_USER_MAX_CAPACITY =
    CommonVars("wds.linkis.entrance.specified.max.capacity", 5000)

  private val GROUP_INIT_CAPACITY = CommonVars("wds.linkis.entrance.init.capacity", 100)

  private val specifiedUsernameRegexPattern: Pattern =
    if (StringUtils.isNotBlank(SPECIFIED_USERNAME_REGEX.getValue)) {
      Pattern.compile(SPECIFIED_USERNAME_REGEX.getValue)
    } else {
      null
    }

  override def getOrCreateGroup(event: SchedulerEvent): Group = {
    val labels = event match {
      case job: EntranceJob =>
        job.getJobRequest.getLabels
      case _ =>
        throw new EntranceErrorException(LABEL_NOT_NULL.getErrorCode, LABEL_NOT_NULL.getErrorDesc)
    }
    val groupName = EntranceGroupFactory.getGroupNameByLabels(labels)
    val cacheGroup = groupNameToGroups.getIfPresent(groupName)
    if (null == cacheGroup) synchronized {
      val maxAskExecutorTimes = EntranceConfiguration.MAX_ASK_EXECUTOR_TIME.getValue.toLong
      val sender: Sender =
        Sender.getSender(Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)
      val userCreatorLabel: UserCreatorLabel = LabelUtil.getUserCreatorLabel(labels)
      val engineTypeLabel: EngineTypeLabel = LabelUtil.getEngineTypeLabel(labels)
      logger.info(
        s"Getting user configurations for $groupName userCreatorLabel: ${userCreatorLabel.getStringValue}, engineTypeLabel:${engineTypeLabel.getStringValue}."
      )
      val keyAndValue = Utils.tryAndWarnMsg {
        sender
          .ask(RequestQueryEngineConfigWithGlobalConfig(userCreatorLabel, engineTypeLabel))
          .asInstanceOf[ResponseQueryConfig]
          .getKeyAndValue
      }(
        "Get user configurations from configuration server failed! Next use the default value to continue."
      )
      val maxRunningJobs = getUserMaxRunningJobs(keyAndValue)
      val initCapacity = GROUP_INIT_CAPACITY.getValue(keyAndValue)
      val maxCapacity = if (null != specifiedUsernameRegexPattern) {
        if (specifiedUsernameRegexPattern.matcher(userCreatorLabel.getUser).find()) {
          logger.info(
            s"Set maxCapacity of user ${userCreatorLabel.getUser} to specifiedMaxCapacity : ${GROUP_SPECIFIED_USER_MAX_CAPACITY
              .getValue(keyAndValue)}"
          )
          GROUP_SPECIFIED_USER_MAX_CAPACITY.getValue(keyAndValue)
        } else {
          GROUP_MAX_CAPACITY.getValue(keyAndValue)
        }
      } else {
        GROUP_MAX_CAPACITY.getValue(keyAndValue)
      }
      logger.info(
        s"Got user configurations: groupName=$groupName, maxRunningJobs=$maxRunningJobs, initCapacity=$initCapacity, maxCapacity=$maxCapacity."
      )
      val group = new ParallelGroup(groupName, initCapacity, maxCapacity)
      group.setMaxRunningJobs(maxRunningJobs)
      group.setMaxAskExecutorTimes(maxAskExecutorTimes)
      groupNameToGroups.put(groupName, group)
      group
    }
    else {
      cacheGroup
    }
  }

  override def getGroup(groupName: String): Group = {
    val group = groupNameToGroups.getIfPresent(groupName)
    if (group == null) {
      throw new EntranceErrorException(
        EntranceErrorCode.GROUP_NOT_FOUND.getErrCode,
        s"group not found: ${groupName}"
      )
    }
    group
  }

  /**
   * User task concurrency control is controlled for multiple Entrances, which will be evenly
   * distributed based on the number of existing Entrances
   * @param keyAndValue
   * @return
   */
  private def getUserMaxRunningJobs(keyAndValue: util.Map[String, String]): Int = {
    val userDefinedRunningJobs = EntranceConfiguration.WDS_LINKIS_INSTANCE.getValue(keyAndValue)
    val entranceNum = EntranceUtils.getRunningEntranceNumber()
    Math.max(
      EntranceConfiguration.ENTRANCE_INSTANCE_MIN.getValue,
      userDefinedRunningJobs / entranceNum
    )
  }

}

object EntranceGroupFactory {

  /**
   * Entrance group rule creator_username_engineType eg:IDE_PEACEWONG_SPARK
   * @param labels
   * @param params
   * @return
   */
  def getGroupNameByLabels(labels: java.util.List[Label[_]]): String = {
    val userCreatorLabel = LabelUtil.getUserCreatorLabel(labels)
    val engineTypeLabel = LabelUtil.getEngineTypeLabel(labels)
    if (null == userCreatorLabel || null == engineTypeLabel) {
      throw new EntranceErrorException(LABEL_NOT_NULL.getErrorCode, LABEL_NOT_NULL.getErrorDesc)
    }
    val groupName =
      userCreatorLabel.getCreator + "_" + userCreatorLabel.getUser + "_" + engineTypeLabel.getEngineType
    groupName
  }

}
