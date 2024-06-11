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

package org.apache.linkis.scheduler.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

object SchedulerConfiguration {

  val FIFO_CONSUMER_AUTO_CLEAR_ENABLED =
    CommonVars("wds.linkis.fifo.consumer.auto.clear.enabled", true)

  val FIFO_CONSUMER_MAX_IDLE_TIME =
    CommonVars("wds.linkis.fifo.consumer.max.idle.time", new TimeType("10m")).getValue.toLong

  val FIFO_CONSUMER_IDLE_SCAN_INTERVAL =
    CommonVars("wds.linkis.fifo.consumer.idle.scan.interval", new TimeType("30m"))

  val FIFO_CONSUMER_IDLE_SCAN_INIT_TIME =
    CommonVars("wds.linkis.fifo.consumer.idle.scan.init.time", new TimeType("1s"))

  val MAX_GROUP_ALTER_WAITING_SIZE =
    CommonVars("linkis.fifo.consumer.group.max.alter.waiting.size", 1000).getValue

}
