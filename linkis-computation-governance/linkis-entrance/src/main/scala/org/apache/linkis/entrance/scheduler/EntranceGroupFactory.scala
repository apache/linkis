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
 
package org.apache.linkis.entrance.scheduler

import java.util

import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.exception.{EntranceErrorCode, EntranceErrorException}
import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.governance.common.protocol.conf.{RequestQueryEngineConfig, RequestQueryEngineConfigWithGlobalConfig, ResponseQueryConfig}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{ConcurrentEngineConnLabel, EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.queue.parallelqueue.ParallelGroup
import org.apache.linkis.scheduler.queue.{Group, GroupFactory, SchedulerEvent}
import org.apache.linkis.server.JMap
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._


class EntranceGroupFactory extends GroupFactory with Logging {

  private val groupNameToGroups = new JMap[String, Group]
//  private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  private val GROUP_MAX_CAPACITY = CommonVars("wds.linkis.entrance.max.capacity", 2000)
  private val GROUP_INIT_CAPACITY = CommonVars("wds.linkis.entrance.init.capacity", 100)


  override def getOrCreateGroup(event: SchedulerEvent): Group = {
    val (labels, params) = event match {
      case job: EntranceJob =>
        (job.getJobRequest.getLabels, job.getJobRequest.getParams.asInstanceOf[util.Map[String, Any]])
    }
    val groupName = EntranceGroupFactory.getGroupNameByLabels(labels, params)
    if (!groupNameToGroups.containsKey(groupName)) synchronized {
      val maxAskExecutorTimes = EntranceConfiguration.MAX_ASK_EXECUTOR_TIME.getValue.toLong
      if (groupName.startsWith(EntranceGroupFactory.CONCURRENT)) {
        if (null == groupNameToGroups.get(groupName)) synchronized {
          if (null == groupNameToGroups.get(groupName)) {
            val group = new ParallelGroup(groupName, 100, EntranceConfiguration.CONCURRENT_FACTORY_MAX_CAPACITY.getValue)
            group.setMaxRunningJobs(EntranceConfiguration.CONCURRENT_MAX_RUNNING_JOBS.getValue)
            group.setMaxAskExecutorTimes(EntranceConfiguration.CONCURRENT_EXECUTOR_TIME.getValue)
            groupNameToGroups.put(groupName, group)
            return group
          }
        }
      }
      val sender: Sender = Sender.getSender(Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)
      var userCreatorLabel: UserCreatorLabel = null
      var engineTypeLabel: EngineTypeLabel = null
      labels.foreach {
        case label: UserCreatorLabel => userCreatorLabel = label
        case label: EngineTypeLabel => engineTypeLabel = label
        case _ =>
      }
      info(s"Getting user configurations for $groupName(正在为 $groupName 获取参数) userCreatorLabel: ${userCreatorLabel.getStringValue}, engineTypeLabel:${engineTypeLabel.getStringValue}.")
      val keyAndValue = Utils.tryAndWarnMsg {
        sender.ask(RequestQueryEngineConfigWithGlobalConfig(userCreatorLabel, engineTypeLabel)).asInstanceOf[ResponseQueryConfig].getKeyAndValue
      }("Get user configurations from configuration server failed! Next use the default value to continue.")
      val maxRunningJobs = EntranceConfiguration.WDS_LINKIS_INSTANCE.getValue(keyAndValue)
      val initCapacity = GROUP_INIT_CAPACITY.getValue(keyAndValue)
      val maxCapacity = GROUP_MAX_CAPACITY.getValue(keyAndValue)
      info(s"Got user configurations: groupName=$groupName, maxRunningJobs=$maxRunningJobs, initCapacity=$initCapacity, maxCapacity=$maxCapacity.")
      val group = new ParallelGroup(groupName, initCapacity, maxCapacity)
      group.setMaxRunningJobs(maxRunningJobs)
      group.setMaxAskExecutorTimes(maxAskExecutorTimes)
      if (!groupNameToGroups.containsKey(groupName)) groupNameToGroups.put(groupName, group)
    }
    groupNameToGroups.get(groupName)
  }

  override def getGroup(groupName: String): Group = {
    val group = groupNameToGroups.get(groupName)
    if(group == null){
      throw new EntranceErrorException(EntranceErrorCode.GROUP_NOT_FOUND.getErrCode, s"group not found: ${groupName}")
    }
    group
  }
}

object EntranceGroupFactory {

  val CACHE = "_Cache"

  val CONCURRENT = "Concurrent_"

  def getGroupName(creator: String, user: String, params: util.Map[String, Any] = new util.HashMap[String, Any]): String = {
    val runtime = TaskUtils.getRuntimeMap(params)
    val cache = if (runtime.get(TaskConstant.READ_FROM_CACHE) != null && runtime.get(TaskConstant.READ_FROM_CACHE).asInstanceOf[Boolean]) CACHE else ""
    if (StringUtils.isNotEmpty(creator)) creator + "_" + user + cache
    else EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getValue + "_" + user + cache
  }

  def getGroupNameByLabels(labels: java.util.List[Label[_]], params: util.Map[String, Any] = new util.HashMap[String, Any]): String = {

    val userCreator = labels.find(_.isInstanceOf[UserCreatorLabel])
    val engineType = labels.find(_.isInstanceOf[EngineTypeLabel])
    val concurrent = labels.find(_.isInstanceOf[ConcurrentEngineConnLabel])
    if (userCreator.isEmpty || engineType.isEmpty) {
      throw new EntranceErrorException(20001, "userCreator label or engineType label cannot null")
    }

    if (concurrent.isDefined) {

      val engineTypeLabel = engineType.get.asInstanceOf[EngineTypeLabel]
      val groupName = CONCURRENT + engineTypeLabel.getEngineType
      groupName

    } else {
      val userCreatorLabel = userCreator.get.asInstanceOf[UserCreatorLabel]

      val engineTypeLabel = engineType.get.asInstanceOf[EngineTypeLabel]

      val runtime = TaskUtils.getRuntimeMap(params)
      val cache = if (runtime.get(TaskConstant.READ_FROM_CACHE) != null && runtime.get(TaskConstant.READ_FROM_CACHE).asInstanceOf[Boolean]) CACHE else ""
      val groupName = userCreatorLabel.getCreator + "_" + userCreatorLabel.getUser + "_" + engineTypeLabel.getEngineType + cache
      groupName
    }
  }


}