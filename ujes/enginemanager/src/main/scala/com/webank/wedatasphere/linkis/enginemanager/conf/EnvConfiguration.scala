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

package com.webank.wedatasphere.linkis.enginemanager.conf

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}
import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration.ENGINE_SPRING_APPLICATION_NAME
import org.apache.commons.lang.time.DateFormatUtils

/**
  * Created by johnnwang on 2018/10/11.
  */
object EnvConfiguration {

  val JAVA_HOME = CommonVars[String]("java.home", CommonVars[String]("JAVA_HOME", "/nemo/jdk1.7.0_67").getValue)
  val SCALA_HOME = CommonVars[String]("scala.home", CommonVars[String]("SCALA_HOME", "").getValue)

  val HADOOP_HOME = CommonVars[String]("hadoop.home", CommonVars[String]("HADOOP_HOME", "/appcom/Install/hadoop").getValue)
  val HADOOP_CONF_DIR = CommonVars[String]("hadoop.config.dir", CommonVars[String]("HADOOP_CONF_DIR", "/appcom/config/hadoop-config").getValue)
  val HBASE_HOME = CommonVars[String]("hbase.home", CommonVars[String]("HBASE_HOME", "/appcom/Install/hbase").getValue)
  val HBASE_CONF_DIR = CommonVars[String]("hbase.config.dir", CommonVars[String]("HBASE_CONF_DIR", "/appcom/config/hbase-config").getValue)
  val SPARK_CONF_DIR = CommonVars[String]("spark.config.dir", CommonVars[String]("SPARK_CONF_DIR", "/appcom/config/spark-config").getValue)
  val HIVE_CONF_DIR = CommonVars[String]("hive.config.dir", CommonVars[String]("HIVE_CONF_DIR", "/appcom/config/hive-config").getValue)

  val ENGINE_MANAGER_MAX_MEMORY_AVAILABLE = CommonVars[ByteType]("wds.linkis.enginemanager.memory.max", new ByteType("40g"))
  val ENGINE_MANAGER_MAX_CORES_AVAILABLE = CommonVars[Integer]("wds.linkis.enginemanager.cores.max", 20)
  val ENGINE_MANAGER_MAX_CREATE_INSTANCES = CommonVars[Integer]("wds.linkis.enginemanager.engine.instances.max", 20)

  val ENGINE_MANAGER_PROTECTED_MEMORY = CommonVars[ByteType]("wds.linkis.enginemanager.protected.memory", new ByteType("4g"))
  val ENGINE_MANAGER_PROTECTED_CORES = CommonVars[Integer]("wds.linkis.enginemanager.protected.cores.max", 2)
  val ENGINE_MANAGER_PROTECTED_INSTANCES = CommonVars[Integer]("wds.linkis.enginemanager.protected.engine.instances", 2)

  val DEFAULT_JAVA_OPTS = CommonVars[String]("wds.linkis.engine.javaOpts.default", "-server -XX:+UseG1GC -XX:MaxPermSize=250m -XX:PermSize=128m " +
    "-Xloggc:%s -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -Dwds.linkis.configuration=linkis-engine.properties")
  val ENGINE_CLIENT_MEMORY = CommonVars[ByteType]("wds.linkis.engine.client.memory", new ByteType("2g"), "指定所有Engine客户端的默认内存大小")
  val ENGINE_CLIENT_EXTRACLASSPATH = CommonVars[String]("wds.linkis.engine.client.extraClassPath", "", "指定用户自定义的jar包全路径（多个以英文,分隔）。")

  val HADOOP_LIB_NATIVE = CommonVars[String]("linkis.hadoop.lib.native","/appcom/Install/hadoop/lib/native")

  private val LOG4J_PROPERTIES_FILE = CommonVars[String]("wds.linkis.engine.log4j.properties.file", "engine-log4j.properties")
  private val LOG_PATH_FOR_GC_FILE = CommonVars[String]("wds.linkis.engine.gclog.path", "/appcom/logs/dataworkcloud/")
  private val LOG_PATH_FOR_ENGINE_GROUP = "dataworkcloud.engine.group"
  private val LOG_PATH_FOR_SESSION_ID = "dataworkcloud.engine.port"
  private val LOG_PATH_FOR_SESSION_KIND = "dataworkcloud.engine.kind"

  private val LOG4J2_XML_FILE = CommonVars[String]("wds.linkis.engine.log4j2.xml.file", "engine-log4j2.xml")

//  def engineLogJavaOpts(id: Long, alias: String) = s" -Dlog4j.configuration=${LOG4J_PROPERTIES_FILE.getValue} -D$LOG_PATH_FOR_SESSION_ID=$id" +
//    s" -D$LOG_PATH_FOR_ENGINE_GROUP=${ENGINE_SPRING_APPLICATION_NAME.getValue} -D$LOG_PATH_FOR_SESSION_KIND=$alias"
  def engineLogJavaOpts(id: Long, alias: String) = s" -Dlogging.file=${LOG4J2_XML_FILE.getValue} -D$LOG_PATH_FOR_SESSION_ID=$id" +
    s" -D$LOG_PATH_FOR_ENGINE_GROUP=${ENGINE_SPRING_APPLICATION_NAME.getValue} -D$LOG_PATH_FOR_SESSION_KIND=$alias"
  def engineGCLogPath(id: Long, umUser: String, alias: String) = LOG_PATH_FOR_GC_FILE.getValue + umUser +
    "/" + ENGINE_SPRING_APPLICATION_NAME.getValue + "/" + alias + "/" + id + "-gc.log" + DateFormatUtils.format(System.currentTimeMillis, "yyyyMMdd-HH_mm")
}
