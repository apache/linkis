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

package org.apache.linkis.engineconnplugin.sqoop.context

import org.apache.linkis.common.conf.{CommonVars, TimeType}

object SqoopEnvConfiguration {

  val SQOOP_HADOOP_SITE_FILE: CommonVars[String] = CommonVars(
    "wds.linkis.hadoop.site.xml",
    "core-site.xml;hdfs-site.xml;yarn-site.xml;mapred-site.xml"
  )

  val SQOOP_STATUS_FETCH_INTERVAL: CommonVars[TimeType] =
    CommonVars("sqoop.fetch.status.interval", new TimeType("5s"))

  val LINKIS_DATASOURCE_SERVICE_NAME: CommonVars[String] =
    CommonVars("wds.linkis.datasource.service.name", "linkis-ps-data-source-manager")

  val SQOOP_HOME: CommonVars[String] = CommonVars("SQOOP_HOME", "")

  val SQOOP_CONF_DIR: CommonVars[String] = CommonVars("SQOOP_CONF_DIR", "")

  val SQOOP_HCAT_HOME: CommonVars[String] = CommonVars("HCAT_HOME", "")

  val SQOOP_HBASE_HOME: CommonVars[String] = CommonVars("HBASE_HOME", "")

  val SQOOP_ZOOCFGDIR: CommonVars[String] = CommonVars("ZOOCFGDIR", "")
}
