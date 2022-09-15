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

package org.apache.linkis.storage.io.conf

import org.apache.linkis.common.conf.CommonVars

object IOFileClientConf {

  val IO_FACTORY_MAX_CAPACITY = CommonVars("wds.linkis.io.group.factory.capacity", 1000)

  val IO_FACTORY_MAX_RUNNING_JOBS = CommonVars("wds.linkis.io.group.factory.running.jobs", 30)

  val IO_FACTORY_EXECUTOR_TIME =
    CommonVars("wds.linkis.io.group.factory.executor.time", 5 * 60 * 1000)

  val IO_LOADBALANCE_CAPACITY = CommonVars("wds.linkis.io.loadbalance.capacity", "1")

  val IO_EXTRA_LABELS = CommonVars("wds.linkis.io.extra.labels", "")

  val IO__JOB_WAIT_S = CommonVars("wds.linkis.io.job.wait.second", 30).getValue

  val IO_EXECUTE_FAILED_CODE = 52005

  val IO_EXECUTE_UNKNOWN_REASON_CODE = 52006

}
