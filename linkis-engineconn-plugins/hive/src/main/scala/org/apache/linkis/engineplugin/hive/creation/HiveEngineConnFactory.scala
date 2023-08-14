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

package org.apache.linkis.engineplugin.hive.creation

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.computation.executor.creation.ComputationSingleExecutorEngineConnFactory
import org.apache.linkis.engineconn.executor.entity.LabelExecutor
import org.apache.linkis.engineplugin.hive.common.HiveUtils
import org.apache.linkis.engineplugin.hive.conf.HiveEngineConfiguration
import org.apache.linkis.engineplugin.hive.entity.{
  AbstractHiveSession,
  HiveConcurrentSession,
  HiveSession
}
import org.apache.linkis.engineplugin.hive.errorcode.HiveErrorCodeSummary.CREATE_HIVE_EXECUTOR_ERROR
import org.apache.linkis.engineplugin.hive.errorcode.HiveErrorCodeSummary.HIVE_EXEC_JAR_ERROR
import org.apache.linkis.engineplugin.hive.exception.HiveSessionStartFailedException
import org.apache.linkis.engineplugin.hive.executor.{
  HiveEngineConcurrentConnExecutor,
  HiveEngineConnExecutor
}
import org.apache.linkis.hadoop.common.utils.HDFSUtils
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.linkis.manager.label.entity.engine.{EngineType, RunType}
import org.apache.linkis.manager.label.entity.engine.EngineType.EngineType
import org.apache.linkis.manager.label.entity.engine.RunType.RunType

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.ql.Driver
import org.apache.hadoop.hive.ql.session.SessionState
import org.apache.hadoop.security.UserGroupInformation

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.file.Paths
import java.security.PrivilegedExceptionAction
import java.util

import scala.collection.JavaConverters._

class HiveEngineConnFactory extends ComputationSingleExecutorEngineConnFactory with Logging {

  private val HIVE_QUEUE_NAME: String = "mapreduce.job.queuename"
  private val BDP_QUEUE_NAME: String = "wds.linkis.rm.yarnqueue"
  private val HIVE_TEZ_QUEUE_NAME: String = "tez.queue.name"

  override protected def newExecutor(
      id: Int,
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): LabelExecutor = {
    engineConn.getEngineConnSession match {
      case hiveSession: HiveSession =>
        new HiveEngineConnExecutor(
          id,
          hiveSession.sessionState,
          hiveSession.ugi,
          hiveSession.hiveConf,
          hiveSession.baos
        )
      case hiveConcurrentSession: HiveConcurrentSession =>
        new HiveEngineConcurrentConnExecutor(
          id,
          hiveConcurrentSession.sessionState,
          hiveConcurrentSession.ugi,
          hiveConcurrentSession.hiveConf,
          hiveConcurrentSession.baos
        )
      case _ =>
        throw HiveSessionStartFailedException(
          CREATE_HIVE_EXECUTOR_ERROR.getErrorCode,
          CREATE_HIVE_EXECUTOR_ERROR.getErrorDesc
        )
    }
  }

  override protected def createEngineConnSession(
      engineCreationContext: EngineCreationContext
  ): AbstractHiveSession = {
    // if hive engine support concurrent, return HiveConcurrentSession
    if (AccessibleExecutorConfiguration.ENGINECONN_SUPPORT_PARALLELISM.getHotValue()) {
      return doCreateHiveConcurrentSession(engineCreationContext.getOptions)
    }

    // return HiveSession
    doCreateHiveSession(engineCreationContext.getOptions)
  }

  def doCreateHiveConcurrentSession(options: util.Map[String, String]): HiveConcurrentSession = {
    val hiveConf: HiveConf = getHiveConf(options)
    val ugi = HDFSUtils.getUserGroupInformation(Utils.getJvmUser)
    val baos = new ByteArrayOutputStream()
    val sessionState: SessionState = getSessionState(hiveConf, ugi, baos)
    HiveConcurrentSession(sessionState, ugi, hiveConf, baos)
  }

  def doCreateHiveSession(options: util.Map[String, String]): HiveSession = {
    val hiveConf: HiveConf = getHiveConf(options)
    val ugi = HDFSUtils.getUserGroupInformation(Utils.getJvmUser)
    val baos = new ByteArrayOutputStream()
    val sessionState: SessionState = getSessionState(hiveConf, ugi, baos)
    HiveSession(sessionState, ugi, hiveConf, baos)
  }

  private def getSessionState(
      hiveConf: HiveConf,
      ugi: UserGroupInformation,
      baos: ByteArrayOutputStream
  ) = {
    val sessionState: SessionState = ugi.doAs(new PrivilegedExceptionAction[SessionState] {
      override def run(): SessionState = new SessionState(hiveConf)
    })
    sessionState.out = new PrintStream(baos, true, "utf-8")
    sessionState.info = new PrintStream(System.out, true, "utf-8")
    sessionState.err = new PrintStream(System.out, true, "utf-8")
    SessionState.start(sessionState)
    sessionState
  }

  private def getHiveConf(options: util.Map[String, String]) = {
    val hiveConf: HiveConf = HiveUtils.getHiveConf

    if (HiveEngineConfiguration.HIVE_RANGER_ENABLE) {
      hiveConf.addResource(
        new Path(
          Paths
            .get(EnvConfiguration.HIVE_CONF_DIR.getValue, "ranger-hive-security.xml")
            .toAbsolutePath
            .toFile
            .getAbsolutePath
        )
      )
      hiveConf.addResource(
        new Path(
          Paths
            .get(EnvConfiguration.HIVE_CONF_DIR.getValue, "ranger-hive-audit.xml")
            .toAbsolutePath
            .toFile
            .getAbsolutePath
        )
      )
      hiveConf.set("hive.security.authorization.enabled", "true")
      hiveConf.set(
        "hive.security.authorization.manager",
        "org.apache.ranger.authorization.hive.authorizer.RangerHiveAuthorizerFactory"
      )
      hiveConf.set(
        "hive.conf.restricted.list",
        "hive.security.authorization.manager,hive.security.metastore.authorization.manager," +
          "hive.security.metastore.authenticator.manager,hive.users.in.admin.role,hive.server2.xsrf.filter.enabled,hive.security.authorization.enabled"
      )
    }
    hiveConf.setVar(
      HiveConf.ConfVars.HIVEJAR,
      HiveUtils
        .jarOfClass(classOf[Driver])
        .getOrElse(
          throw HiveSessionStartFailedException(
            HIVE_EXEC_JAR_ERROR.getErrorCode,
            HIVE_EXEC_JAR_ERROR.getErrorDesc
          )
        )
    )
    options.asScala.foreach { case (k, v) => logger.info(s"key is $k, value is $v") }
    options.asScala
      .filter { case (k, v) =>
        k.startsWith("hive.") || k.startsWith("mapreduce.") || k.startsWith("mapred.reduce.") || k
          .startsWith("wds.linkis.")
      }
      .foreach { case (k, v) =>
        logger.info(s"key is $k, value is $v")
        if (BDP_QUEUE_NAME.equals(k)) {
          hiveConf.set(HIVE_QUEUE_NAME, v)
          if ("tez".equals(HiveEngineConfiguration.HIVE_ENGINE_TYPE)) {
            hiveConf.set(HIVE_TEZ_QUEUE_NAME, v)
          }
        } else hiveConf.set(k, v)
      }
    hiveConf.setVar(
      HiveConf.ConfVars.HIVE_HADOOP_CLASSPATH,
      HiveEngineConfiguration.HIVE_LIB_HOME.getValue + "/*"
    )
    if (HiveEngineConfiguration.ENABLE_FETCH_BASE64) {
      hiveConf.setVar(
        HiveConf.ConfVars.HIVEFETCHOUTPUTSERDE,
        HiveEngineConfiguration.BASE64_SERDE_CLASS
      )
      hiveConf.set("enable_fetch_base64", "true")
    }
    // add hive.aux.jars.path to hive conf
    if (StringUtils.isNotBlank(HiveEngineConfiguration.HIVE_AUX_JARS_PATH)) {
      hiveConf.setVar(HiveConf.ConfVars.HIVEAUXJARS, HiveEngineConfiguration.HIVE_AUX_JARS_PATH)
    }

    /*
    //add hook to HiveDriver
     if (StringUtils.isNotBlank(EnvConfiguration.LINKIS_HIVE_POST_HOOKS)) {
       val hooks = if (StringUtils.isNotBlank(hiveConf.get("hive.exec.post.hooks"))) {
         hiveConf.get("hive.exec.post.hooks") + "," + EnvConfiguration.LINKIS_HIVE_POST_HOOKS
       } else {
         EnvConfiguration.LINKIS_HIVE_POST_HOOKS
       }
       hiveConf.set("hive.exec.post.hooks", hooks)
     }
     */
    // enable hive.stats.collect.scancols
    hiveConf.setBoolean("hive.stats.collect.scancols", true)
    hiveConf
  }

  override protected def getEngineConnType: EngineType = EngineType.HIVE

  override protected def getRunType: RunType = RunType.HIVE

}
