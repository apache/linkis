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

package org.apache.linkis.bml.conf

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.Utils

import java.util.concurrent.TimeUnit

object BmlServerConfiguration {
  val BML_HDFS_PREFIX = CommonVars("wds.linkis.bml.hdfs.prefix", "/apps-data")

  val BML_LOCAL_PREFIX = CommonVars("wds.linkis.bml.local.prefix", "/data/dss/bml")

  val BML_IS_HDFS: CommonVars[Boolean] = CommonVars[Boolean]("wds.linkis.bml.is.hdfs", true)

  val BML_CLEAN_EXPIRED_TIME: CommonVars[Int] =
    CommonVars[Int]("wds.linkis.bml.cleanExpired.time", 100)

  val BML_CLEAN_EXPIRED_TIME_TYPE = CommonVars("wds.linkis.bml.clean.time.type", TimeUnit.HOURS)

  val BML_MAX_THREAD_SIZE: CommonVars[Int] =
    CommonVars[Int]("wds.linkis.server.maxThreadSize", 30)

  val BML_DEFAULT_PROXY_USER = CommonVars("wds.linkis.bml.default.proxy.user", Utils.getJvmUser)

}
