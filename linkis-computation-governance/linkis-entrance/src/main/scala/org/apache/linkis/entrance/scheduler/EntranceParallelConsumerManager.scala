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
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.instance.label.client.InstanceLabelClient
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.{LabelKeyConstant, LabelValueConstant}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.route.RouteLabel
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer
import org.apache.linkis.scheduler.queue.parallelqueue.{ParallelConsumerManager, ParallelGroup}

import java.util
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._

class EntranceParallelConsumerManager(maxParallelismUsers: Int, schedulerName: String)
    extends ParallelConsumerManager(maxParallelismUsers, schedulerName) {

  override protected def createConsumer(groupName: String): FIFOUserConsumer = {
    val group = getSchedulerContext.getOrCreateGroupFactory.getGroup(groupName)
    new EntranceFIFOUserConsumer(getSchedulerContext, getOrCreateExecutorService, group)
  }

  if (EntranceConfiguration.ENTRANCE_GROUP_SCAN_ENABLED.getValue) {
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = {
          Utils.tryAndError {
            logger.info("start refresh consumer group maxAllowRunningJobs")
            // get all entrance server from eureka
            val serviceInstances =
              Sender.getInstances(Sender.getThisServiceInstance.getApplicationName)
            if (null == serviceInstances || serviceInstances.isEmpty) return

            // get all offline label server
            val routeLabel = LabelBuilderFactoryContext.getLabelBuilderFactory
              .createLabel[RouteLabel](LabelKeyConstant.ROUTE_KEY, LabelValueConstant.OFFLINE_VALUE)
            val labels = new util.ArrayList[Label[_]]
            labels.add(routeLabel)
            val labelInstances = InstanceLabelClient.getInstance.getInstanceFromLabel(labels)

            // get active entrance server
            val allInstances = new util.ArrayList[ServiceInstance]()
            allInstances.addAll(serviceInstances.toList.asJava)
            allInstances.removeAll(labelInstances)
            // refresh all group maxAllowRunningJobs
            refreshAllGroupMaxAllowRunningJobs(allInstances.size())
            logger.info("Finished to refresh consumer group maxAllowRunningJobs")
          }
        }
      },
      EntranceConfiguration.ENTRANCE_GROUP_SCAN_INIT_TIME.getValue,
      EntranceConfiguration.ENTRANCE_GROUP_SCAN_INTERVAL.getValue,
      TimeUnit.MILLISECONDS
    )
  }

  def refreshAllGroupMaxAllowRunningJobs(validInsCount: Int): Unit = {
    listConsumers()
      .foreach(item => {
        item.getGroup match {
          case group: ParallelGroup =>
            val maxAllowRunningJobs = Math.round(group.getMaxRunningJobs / validInsCount)
            group.setMaxAllowRunningJobs(maxAllowRunningJobs)
            logger
              .info(
                "group {} refresh maxAllowRunningJobs => {}/{}={}",
                Array(
                  group.getGroupName,
                  group.getMaxRunningJobs.toString,
                  validInsCount.toString,
                  maxAllowRunningJobs.toString
                ): _*
              )
          case _ =>
        }
      })
  }

}
