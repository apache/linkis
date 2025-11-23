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

package org.apache.linkis.metadata.conf

import org.apache.linkis.common.conf.CommonVars

object MdqConfiguration {
  val DEFAULT_STORED_TYPE = CommonVars("bdp.dataworkcloud.datasource.store.type", "orc")
  val DEFAULT_PARTITION_NAME = CommonVars("bdp.dataworkcloud.datasource.default.par.name", "ds")

  val SPARK_MDQ_IMPORT_CLAZZ = CommonVars(
    "wds.linkis.spark.mdq.import.clazz",
    "org.apache.linkis.engineplugin.spark.imexport.LoadData"
  )

  val HDFS_INIT_MAX_RETRY_COUNT: CommonVars[Integer] =
    CommonVars.apply("linkis.hdfs.max.retry.count", 10)

  val HIVE_METADATA_SALVE_SWITCH: Boolean =
    CommonVars.apply("linkis.hive.metadata.slave.switch", false).getValue

  val HIVE_METADATA_SLOW_SQL_SWITCH: Boolean =
    CommonVars.apply("linkis.hive.metadata.slow.sql.switch", true).getValue

}
