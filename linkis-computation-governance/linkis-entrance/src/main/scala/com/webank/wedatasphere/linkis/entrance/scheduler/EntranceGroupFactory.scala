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

package com.webank.wedatasphere.linkis.entrance.scheduler

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorException
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.entrance.persistence.HaPersistenceTask
import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask
import com.webank.wedatasphere.linkis.governance.common.protocol.conf.{RequestQueryEngineConfig, ResponseQueryConfig}
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{ConcurrentEngineConnLabel, EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.manager.label.utils.EngineTypeLabelCreator
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelGroup
import com.webank.wedatasphere.linkis.scheduler.queue.{Group, GroupFactory, SchedulerEvent}
import com.webank.wedatasphere.linkis.server.JMap
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._


class EntranceGroupFactory extends GroupFactory with Logging {

  private val groupNameToGroups = new JMap[String, Group]
  private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  override def getOrCreateGroup(groupName: String): Group = {
    if (!groupNameToGroups.containsKey(groupName)) synchronized {
      val initCapacity = 100
      val maxCapacity = 100
      var maxRunningJobs = EntranceConfiguration.WDS_LINKIS_INSTANCE.getValue
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
      val groupNameSplits = groupName.split("_")
      if (groupNameSplits.length < 3) {
        logger.warn(s"name style of group: $groupName is not correct, we will set default value for the group")
      } else {
        val sender: Sender = Sender.getSender(EntranceConfiguration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)
        val creator = groupNameSplits(0)
        val username = groupNameSplits(1)
        val engineType = groupNameSplits(2)

        logger.info(s"Getting parameters for $groupName(正在为 $groupName 获取参数) username: $username, creator:$creator, engineType: $engineType")
        val userCreatorLabel = labelBuilderFactory.createLabel(classOf[UserCreatorLabel])
        userCreatorLabel.setUser(username)
        userCreatorLabel.setCreator(creator)
        val engineTypeLabel = EngineTypeLabelCreator.createEngineTypeLabel(engineType)
        try {
          val keyAndValue = sender.ask(RequestQueryEngineConfig(userCreatorLabel, engineTypeLabel)).asInstanceOf[ResponseQueryConfig].getKeyAndValue

          maxRunningJobs = Integer.parseInt(keyAndValue.get(EntranceConfiguration.WDS_LINKIS_INSTANCE.key))
        } catch {
          case t: Throwable => logger.warn("Get maxRunningJobs from configuration server failed! Next use the default value to continue.")
        }
      }
      logger.info("groupName: {} =>  maxRunningJobs is {}", groupName, maxRunningJobs)
      val group = new ParallelGroup(groupName, initCapacity, maxCapacity)
      group.setMaxRunningJobs(maxRunningJobs)
      group.setMaxAskExecutorTimes(maxAskExecutorTimes)
      if (!groupNameToGroups.containsKey(groupName)) groupNameToGroups.put(groupName, group)
    }
    groupNameToGroups.get(groupName)
  }


  override def getGroupNameByEvent(event: SchedulerEvent): String = event match {
    case job: EntranceJob =>
      job.getTask match {
        case HaPersistenceTask(task) =>
          "HA"
        case requestPersistTask: RequestPersistTask => {
          val labels = requestPersistTask.getLabels
          EntranceGroupFactory.getGroupNameByLabels(labels, job.getParams)
        }
        case _ => EntranceGroupFactory.getGroupName(job.getCreator, job.getUser, job.getParams)
      }
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