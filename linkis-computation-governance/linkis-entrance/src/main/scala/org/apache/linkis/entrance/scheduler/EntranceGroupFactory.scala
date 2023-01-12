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

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary._
import org.apache.linkis.entrance.exception.{EntranceErrorCode, EntranceErrorException}
import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.governance.common.protocol.conf.{
  RequestQueryEngineConfigWithGlobalConfig,
  ResponseQueryConfig
}
import org.apache.linkis.instance.label.client.InstanceLabelClient
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.{LabelKeyConstant, LabelValueConstant}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{
  ConcurrentEngineConnLabel,
  EngineTypeLabel,
  UserCreatorLabel
}
import org.apache.linkis.manager.label.entity.route.RouteLabel
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.queue.{Group, GroupFactory, SchedulerEvent}
import org.apache.linkis.scheduler.queue.parallelqueue.ParallelGroup

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import scala.collection.JavaConverters._

import com.google.common.cache.{Cache, CacheBuilder}

class EntranceGroupFactory extends GroupFactory with Logging {

  private val groupNameToGroups: Cache[String, Group] = CacheBuilder
    .newBuilder()
    .expireAfterAccess(EntranceConfiguration.GROUP_CACHE_EXPIRE_TIME.getValue, TimeUnit.MINUTES)
    .maximumSize(EntranceConfiguration.GROUP_CACHE_MAX.getValue)
    .build()

  private val GROUP_MAX_CAPACITY = CommonVars("wds.linkis.entrance.max.capacity", 2000)

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
    val (labels, params) = event match {
      case job: EntranceJob =>
        (job.getJobRequest.getLabels, job.getJobRequest.getParams)
    }
    val groupName = EntranceGroupFactory.getGroupNameByLabels(labels, params)
    val cacheGroup = groupNameToGroups.getIfPresent(groupName)
    if (null == cacheGroup) synchronized {
      val maxAskExecutorTimes = EntranceConfiguration.MAX_ASK_EXECUTOR_TIME.getValue.toLong
      if (groupName.startsWith(EntranceGroupFactory.CONCURRENT)) {
        if (null == groupNameToGroups.getIfPresent(groupName)) synchronized {
          if (null == groupNameToGroups.getIfPresent(groupName)) {
            val group = new ParallelGroup(
              groupName,
              100,
              EntranceConfiguration.CONCURRENT_FACTORY_MAX_CAPACITY.getValue
            )
            group.setMaxRunningJobs(EntranceConfiguration.CONCURRENT_MAX_RUNNING_JOBS.getValue)
            group.setMaxAskExecutorTimes(EntranceConfiguration.CONCURRENT_EXECUTOR_TIME.getValue)
            groupNameToGroups.put(groupName, group)
            return group
          }
        }
      }
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
    }
    groupNameToGroups.getIfPresent(groupName)
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

  private def getUserMaxRunningJobs(keyAndValue: util.Map[String, String]): Int = {
    var userDefinedRunningJobs = EntranceConfiguration.WDS_LINKIS_INSTANCE.getValue(keyAndValue)
    var entranceNum = Sender.getInstances(Sender.getThisServiceInstance.getApplicationName).length
    val labelList = new util.ArrayList[Label[_]]()
    val offlineRouteLabel = LabelBuilderFactoryContext.getLabelBuilderFactory
      .createLabel[RouteLabel](LabelKeyConstant.ROUTE_KEY, LabelValueConstant.OFFLINE_VALUE)
    labelList.add(offlineRouteLabel)
    var offlineIns: Array[ServiceInstance] = null
    Utils.tryAndWarn {
      offlineIns = InstanceLabelClient.getInstance
        .getInstanceFromLabel(labelList)
        .asScala
        .filter(l =>
          null != l && l.getApplicationName
            .equalsIgnoreCase(Sender.getThisServiceInstance.getApplicationName)
        )
        .toArray
    }
    if (null != offlineIns) {
      logger.info(s"There are ${offlineIns.length} offlining instance.")
      entranceNum = entranceNum - offlineIns.length
    }
    /*
    Sender.getInstances may get 0 instances due to cache in Sender. So this instance is the one instance.
     */
    if (0 >= entranceNum) {
      logger.error(
        s"Got ${entranceNum} ${Sender.getThisServiceInstance.getApplicationName} instances."
      )
      entranceNum = 1
    }
    Math.max(
      EntranceConfiguration.ENTRANCE_INSTANCE_MIN.getValue,
      userDefinedRunningJobs / entranceNum
    );
  }

}

object EntranceGroupFactory {

  val CACHE = "_Cache"

  val CONCURRENT = "Concurrent_"

  def getGroupName(
      creator: String,
      user: String,
      params: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]
  ): String = {
    val runtime = TaskUtils.getRuntimeMap(params)
    val cache =
      if (
          runtime.get(TaskConstant.READ_FROM_CACHE) != null && runtime
            .get(TaskConstant.READ_FROM_CACHE)
            .asInstanceOf[Boolean]
      ) {
        CACHE
      } else ""
    if (StringUtils.isNotEmpty(creator)) creator + "_" + user + cache
    else EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getValue + "_" + user + cache
  }

  def getGroupNameByLabels(
      labels: java.util.List[Label[_]],
      params: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]
  ): String = {

    val userCreator = labels.asScala.find(_.isInstanceOf[UserCreatorLabel])
    val engineType = labels.asScala.find(_.isInstanceOf[EngineTypeLabel])
    val concurrent = labels.asScala.find(_.isInstanceOf[ConcurrentEngineConnLabel])
    if (userCreator.isEmpty || engineType.isEmpty) {
      throw new EntranceErrorException(LABEL_NOT_NULL.getErrorCode, LABEL_NOT_NULL.getErrorDesc)
    }

    if (concurrent.isDefined) {

      val engineTypeLabel = engineType.get.asInstanceOf[EngineTypeLabel]
      val groupName = CONCURRENT + engineTypeLabel.getEngineType
      groupName

    } else {
      val userCreatorLabel = userCreator.get.asInstanceOf[UserCreatorLabel]

      val engineTypeLabel = engineType.get.asInstanceOf[EngineTypeLabel]

      val runtime = TaskUtils.getRuntimeMap(params)
      val cache =
        if (
            runtime.get(TaskConstant.READ_FROM_CACHE) != null && runtime
              .get(TaskConstant.READ_FROM_CACHE)
              .asInstanceOf[Boolean]
        ) {
          CACHE
        } else ""
      val groupName =
        userCreatorLabel.getCreator + "_" + userCreatorLabel.getUser + "_" + engineTypeLabel.getEngineType + cache
      groupName
    }
  }

}
