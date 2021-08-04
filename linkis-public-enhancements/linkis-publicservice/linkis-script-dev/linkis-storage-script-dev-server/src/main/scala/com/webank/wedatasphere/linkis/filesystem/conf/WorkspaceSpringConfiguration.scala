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

package com.webank.wedatasphere.linkis.filesystem.conf

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.filesystem.quartz.FSQuartz
import org.quartz._
import org.springframework.context.annotation.{Bean, Configuration}


@Configuration
class WorkspaceSpringConfiguration extends Logging{
  @Bean
  def getWorkspaceJobDetail:JobDetail={
    JobBuilder.newJob(classOf[FSQuartz]).withIdentity("FSQuartz").storeDurably().build();
  }

  @Bean
  def getWorkspaceTrigger:Trigger= {
    val scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(1).repeatForever()
    //val scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(1).repeatForever()
    TriggerBuilder.newTrigger().forJob(getWorkspaceJobDetail).withIdentity("FSQuartz").withSchedule(scheduleBuilder).build()
  }
}
