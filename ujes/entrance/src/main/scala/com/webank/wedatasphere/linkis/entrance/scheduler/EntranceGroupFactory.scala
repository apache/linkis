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

import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.entrance.persistence.HaPersistenceTask
import com.webank.wedatasphere.linkis.protocol.config.{RequestQueryAppConfig, ResponseQueryConfig}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelGroup
import com.webank.wedatasphere.linkis.scheduler.queue.{Group, GroupFactory, SchedulerEvent}
import com.webank.wedatasphere.linkis.server.JMap
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by enjoyyin on 2019/1/22.
  */
class EntranceGroupFactory extends GroupFactory {

  private val groupNameToGroups = new JMap[String, Group]
  private val logger:Logger = LoggerFactory.getLogger(classOf[EntranceGroupFactory])
  override def getOrCreateGroup(groupName: String): Group = {
    if(!groupNameToGroups.containsKey(groupName)) synchronized{
      //TODO Query the database and get initCapacity, maxCapacity, maxRunningJobs, maxAskExecutorTimes(查询数据库，拿到initCapacity、maxCapacity、maxRunningJobs、maxAskExecutorTimes)
      val initCapacity = 100
      val maxCapacity = 100
      var maxRunningJobs =  EntranceConfiguration.WDS_LINKIS_INSTANCE.getValue
      val maxAskExecutorTimes = EntranceConfiguration.MAX_ASK_EXECUTOR_TIME.getValue.toLong
      if (groupName.split("_").length < 2){
        logger.warn(s"name style of group: $groupName is not correct, we will set default value for the group")
      }else{
        val sender:Sender = Sender.getSender(EntranceConfiguration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)
        val creator = groupName.split("_")(0)
        val username = groupName.split("_")(1)
        val engineName = EntranceConfiguration.ENGINE_SPRING_APPLICATION_NAME.getValue
        val engineType = if (engineName.trim().toLowerCase().contains("engine")) engineName.substring(0, engineName.length - "engine".length) else "spark"
        logger.info(s"Getting parameters for $groupName(正在为 $groupName 获取参数) username: $username, creator:$creator, engineType: $engineType")
        val keyAndValue = sender.ask(RequestQueryAppConfig(username, creator, engineType)).asInstanceOf[ResponseQueryConfig].getKeyAndValue
        try{
          maxRunningJobs = Integer.parseInt(keyAndValue.get(EntranceConfiguration.WDS_LINKIS_INSTANCE.key))
        }catch{
          case t:Throwable => logger.warn("Get maxRunningJobs from configuration server failed! Next use the default value to continue.",t)
        }
      }
      logger.info("groupName: {} =>  maxRunningJobs is {}", groupName, maxRunningJobs)
      val group = new ParallelGroup(groupName, initCapacity, maxCapacity)
      group.setMaxRunningJobs(maxRunningJobs)
      group.setMaxAskExecutorTimes(maxAskExecutorTimes)
      if(!groupNameToGroups.containsKey(groupName)) groupNameToGroups.put(groupName, group)
    }
    groupNameToGroups.get(groupName)
  }


  override def getGroupNameByEvent(event: SchedulerEvent): String = event match {
    case job: EntranceJob =>
      job.getTask match {
        case HaPersistenceTask(task) =>
          "HA"
        case _ =>EntranceGroupFactory.getGroupName(job.getCreator, job.getUser)
      }
  }
}
object EntranceGroupFactory {
  def getGroupName(creator: String, user: String): String = {
    if (StringUtils.isNotEmpty(creator)) creator + "_" + user
    else EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getValue + "_" + user
  }
}