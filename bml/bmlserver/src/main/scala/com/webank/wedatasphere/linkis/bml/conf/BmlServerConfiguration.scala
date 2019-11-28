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
package com.webank.wedatasphere.linkis.bml.conf

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * created by cooperyang on 2019/5/21
  * Description:
  */
object BmlServerConfiguration {
  val BML_HDFS_PREFIX = CommonVars("wds.linkis.bml.hdfs.prefix", "/tmp/linkis")

  val BML_CLEAN_EXPIRED_TIME:CommonVars[Int] = CommonVars[Int]("wds.linkis.bml.cleanExpired.time", 100)

  val BML_CLEAN_EXPIRED_TIME_TYPE = CommonVars("wds.linkis.bml.clean.time.type", TimeUnit.HOURS)

  val BML_MAX_THREAD_SIZE:CommonVars[Int] = CommonVars[Int]("wds.linkis.server.maxThreadSize", 30)

}
