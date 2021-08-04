/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.storage.io.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars

object IOFileClientConf {

  val IO_FACTORY_MAX_CAPACITY = CommonVars("wds.linkis.io.group.factory.capacity", 1000)

  val IO_FACTORY_MAX_RUNNING_JOBS = CommonVars("wds.linkis.io.group.factory.running.jobs", 30)

  val IO_FACTORY_EXECUTOR_TIME = CommonVars("wds.linkis.io.group.factory.executor.time", 5 * 60 * 1000)

  val IO_LOADBALANCE_CAPACITY = CommonVars("wds.linkis.io.loadbalance.capacity", "1")

  val IO_EXTRA_LABELS = CommonVars("wds.linkis.io.extra.labels", "")

  val IO_EXECUTE_FAILED_CODE = 52005

  val IO_EXECUTE_UNKNOWN_REASON_CODE = 52006

}
