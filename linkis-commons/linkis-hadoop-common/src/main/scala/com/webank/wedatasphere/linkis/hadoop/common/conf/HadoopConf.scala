/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.hadoop.common.conf

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}


object HadoopConf {

  val HADOOP_ROOT_USER = CommonVars("wds.linkis.hadoop.root.user", "hadoop")

  val KERBEROS_ENABLE = CommonVars("wds.linkis.keytab.enable", false)

  val KEYTAB_FILE = CommonVars("wds.linkis.keytab.file", "/appcom/keytab/")

  val KEYTAB_HOST = CommonVars("wds.linkis.keytab.host", "127.0.0.1")

  val KEYTAB_HOST_ENABLED = CommonVars("wds.linkis.keytab.host.enabled", false)

  val hadoopConfDir = CommonVars("hadoop.config.dir", CommonVars("HADOOP_CONF_DIR", "").getValue).getValue

  val HADOOP_EXTERNAL_CONF_DIR_PREFIX = CommonVars("wds.linkis.hadoop.external.conf.dir.prefix", "/appcom/config/external-conf/hadoop")

  val HDFS_ENABLE_CACHE = CommonVars("wds.linkis.hadoop.hdfs.cache.enable", false).getValue

  val HDFS_ENABLE_CACHE_IDLE_TIME = CommonVars("wds.linkis.hadoop.hdfs.cache.idle.time", 3*60*1000).getValue

  val HDFS_ENABLE_CACHE_MAX_TIME = CommonVars("wds.linkis.hadoop.hdfs.cache.max.time",  new TimeType("12h")).getValue.toLong
}
