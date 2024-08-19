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

package org.apache.linkis.filesystem.conf

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.filesystem.quartz.FSQuartz

import org.springframework.context.annotation.{Bean, Configuration}

import org.quartz._

@Configuration
class WorkspaceSpringConfiguration extends Logging {

  @Bean
  def getWorkspaceJobDetail: JobDetail = {
    JobBuilder.newJob(classOf[FSQuartz]).withIdentity("FSQuartz").storeDurably().build();
  }

  @Bean
  def getWorkspaceTrigger: Trigger = {
    val scheduleBuilder =
      SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(1).repeatForever()
    // val scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(1).repeatForever()
    TriggerBuilder
      .newTrigger()
      .forJob(getWorkspaceJobDetail)
      .withIdentity("FSQuartz")
      .withSchedule(scheduleBuilder)
      .build()
  }

}
