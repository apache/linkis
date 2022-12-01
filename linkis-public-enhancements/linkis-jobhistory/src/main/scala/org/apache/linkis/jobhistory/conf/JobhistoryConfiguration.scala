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

package org.apache.linkis.jobhistory.conf

import org.apache.linkis.common.conf.{CommonVars, Configuration}

object JobhistoryConfiguration {

  val JOB_HISTORY_SAFE_TRIGGER = CommonVars("wds.linkis.jobhistory.safe.trigger", true).getValue

  val ENTRANCE_SPRING_NAME = CommonVars("wds.linkis.entrance.spring.name", "linkis-cg-entrance")
  val ENTRANCE_INSTANCE_DELEMITER = CommonVars("wds.linkis.jobhistory.instance.delemiter", ";")

  val UPDATE_RETRY_TIMES = CommonVars("wds.linkis.jobhistory.update.retry.times", 3)
  val UPDATE_RETRY_INTERVAL = CommonVars("wds.linkis.jobhistory.update.retry.interval", 3 * 1000)

  val UNDONE_JOB_MINIMUM_ID: CommonVars[Long] =
    CommonVars("wds.linkis.jobhistory.undone.job.minimum.id", 0L)

  val UNDONE_JOB_REFRESH_TIME_DAILY: CommonVars[String] =
    CommonVars("wds.linkis.jobhistory.undone.job.refreshtime.daily", "00:15")

  val DIRTY_DATA_UNFINISHED_JOB_STATUS =
    "Inited,WaitForRetry,Scheduled,Running".split(",").map(s => s.toUpperCase())

}
