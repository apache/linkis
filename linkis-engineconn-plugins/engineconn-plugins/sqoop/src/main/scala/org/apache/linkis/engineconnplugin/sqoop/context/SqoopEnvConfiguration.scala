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

package org.apache.linkis.engineconnplugin.sqoop.context

import org.apache.linkis.common.conf.CommonVars

object SqoopEnvConfiguration {
  val HADOOP_SITE_FILE = CommonVars("wds.linkis.hadoop.site.xml", "core-site.xml;hdfs-site.xml;yarn-site.xml;mapred-site.xml")
  val LINKIS_SQOOP_TASK_MANAGER_MEMORY = CommonVars[Int]("sqoop.taskmanager.memory", 4)
  val LINKIS_SQOOP_TASK_MANAGER_CPU_CORES = CommonVars[Int]("sqoop.taskmanager.cpu.cores", 1)
  val LINKIS_QUEUE_NAME = CommonVars[String]("wds.linkis.rm.yarnqueue", "default")

}
