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

package org.apache.linkis.hadoop.common.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

object HadoopConf {

  val HADOOP_ROOT_USER = CommonVars("wds.linkis.hadoop.root.user", "hadoop")

  val KERBEROS_ENABLE = CommonVars("wds.linkis.keytab.enable", false).getValue

  val KERBEROS_ENABLE_MAP =
    CommonVars("linkis.keytab.enable.map", "cluster1=false,cluster2=true")

  val KEYTAB_FILE = CommonVars("wds.linkis.keytab.file", "/appcom/keytab/")

  val LINKIS_KEYTAB_FILE = CommonVars("linkis.copy.keytab.file", "/mnt/bdap/keytab/")

  val EXTERNAL_KEYTAB_FILE_PREFIX =
    CommonVars("linkis.external.keytab.file.prefix", "/appcom/config/external-conf/keytab")

  val KEYTAB_HOST = CommonVars("wds.linkis.keytab.host", "127.0.0.1")

  val KEYTAB_HOST_MAP =
    CommonVars("linkis.keytab.host.map", "cluster1=127.0.0.2,cluster2=127.0.0.3")

  val KEYTAB_HOST_ENABLED = CommonVars("wds.linkis.keytab.host.enabled", false)

  val KEYTAB_PROXYUSER_ENABLED = CommonVars("wds.linkis.keytab.proxyuser.enable", false)

  val KEYTAB_PROXYUSER_SUPERUSER = CommonVars("wds.linkis.keytab.proxyuser.superuser", "hadoop")

  val KEYTAB_PROXYUSER_SUPERUSER_MAP =
    CommonVars("linkis.keytab.proxyuser.superuser.map", "cluster1=hadoop1,cluster2=hadoop2")

  val hadoopConfDir =
    CommonVars("hadoop.config.dir", CommonVars("HADOOP_CONF_DIR", "").getValue).getValue

  val HADOOP_EXTERNAL_CONF_DIR_PREFIX =
    CommonVars("wds.linkis.hadoop.external.conf.dir.prefix", "/appcom/config/external-conf/hadoop")

  /**
   * Whether to close the hdfs underlying cache or turn it off if it is ture
   */
  val FS_CACHE_DISABLE =
    CommonVars[java.lang.Boolean]("wds.linkis.fs.hdfs.impl.disable.cache", false)

  val HDFS_ENABLE_CACHE = CommonVars("wds.linkis.hadoop.hdfs.cache.enable", false).getValue

  val HDFS_ENABLE_CACHE_CLOSE =
    CommonVars("linkis.hadoop.hdfs.cache.close.enable", true).getValue

  val HDFS_ENABLE_NOT_CLOSE_USERS =
    CommonVars("linkis.hadoop.hdfs.cache.not.close.users", "hadoop").getValue

  val HDFS_ENABLE_CACHE_IDLE_TIME =
    CommonVars("wds.linkis.hadoop.hdfs.cache.idle.time", 3 * 60 * 1000).getValue

  val HDFS_ENABLE_CACHE_MAX_TIME =
    CommonVars("wds.linkis.hadoop.hdfs.cache.max.time", new TimeType("12h")).getValue.toLong

}
