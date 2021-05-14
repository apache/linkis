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

package com.webank.wedatasphere.linkis.engineplugin.hive.creation

import java.io.{ByteArrayOutputStream, PrintStream}
import java.security.PrivilegedExceptionAction

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.{DefaultEngineConn, EngineConn}
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.executor.entity.Executor
import com.webank.wedatasphere.linkis.engineplugin.hive.common.HiveUtils
import com.webank.wedatasphere.linkis.engineplugin.hive.entity.HiveSession
import com.webank.wedatasphere.linkis.engineplugin.hive.exception.HiveSessionStartFailedException
import com.webank.wedatasphere.linkis.engineplugin.hive.executor.HiveEngineConnExecutor
import com.webank.wedatasphere.linkis.hadoop.common.utils.HDFSUtils
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.SingleExecutorEngineConnFactory
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineRunTypeLabel, EngineType, RunType}
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.ql.Driver
import org.apache.hadoop.hive.ql.session.SessionState

import scala.collection.JavaConversions._

class HiveEngineConnFactory extends SingleExecutorEngineConnFactory with Logging {

  private val HIVE_QUEUE_NAME: String = "mapreduce.job.queuename"
  private val BDP_QUEUE_NAME: String = "wds.linkis.rm.yarnqueue"
  private var engineCreationContext: EngineCreationContext = _

  override def createExecutor(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Executor = {
    engineConn.getEngine() match {
      case hiveSession: HiveSession =>
        this.engineCreationContext = engineCreationContext
        val id = ExecutorManager.getInstance().generateId()
        val executor = new HiveEngineConnExecutor(id, hiveSession.sessionState, hiveSession.ugi, hiveSession.hiveConf, hiveSession.baos)
        executor.getExecutorLabels().add(getDefaultEngineRunTypeLabel())
        executor
      case _ =>
        throw HiveSessionStartFailedException(40012, "Failed to create hive executor")
    }
  }


  override def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn = {
    val options = engineCreationContext.getOptions
    val hiveConf: HiveConf = HiveUtils.getHiveConf
    hiveConf.setVar(HiveConf.ConfVars.HIVEJAR, HiveUtils.jarOfClass(classOf[Driver])
      .getOrElse(throw HiveSessionStartFailedException(40012, "cannot find hive-exec.jar, start session failed!")))
    options.foreach { case (k, v) => info(s"key is $k, value is $v") }
    options.filter { case (k, v) => k.startsWith("hive.") || k.startsWith("mapreduce.") || k.startsWith("wds.linkis.") }.foreach { case (k, v) =>
      info(s"key is $k, value is $v")
      if (BDP_QUEUE_NAME.equals(k)) hiveConf.set(HIVE_QUEUE_NAME, v) else hiveConf.set(k, v)
    }

    hiveConf.setBoolean("hive.stats.collect.scancols", true)
    val ugi = HDFSUtils.getUserGroupInformation(Utils.getJvmUser)
    val sessionState: SessionState = ugi.doAs(new PrivilegedExceptionAction[SessionState] {
      override def run(): SessionState = new SessionState(hiveConf)
    })
    val baos = new ByteArrayOutputStream()
    sessionState.out = new PrintStream(baos, true, "utf-8")
    sessionState.info = new PrintStream(System.out, true, "utf-8")
    sessionState.err = new PrintStream(System.out, true, "utf-8")
    SessionState.start(sessionState)

    val hiveSession = HiveSession(sessionState, ugi, hiveConf, baos)
    val engineConn = new DefaultEngineConn(engineCreationContext)
    engineConn.setEngineType(EngineType.HIVE.toString)
    engineConn.setEngine(hiveSession)
    engineConn
  }

  def getEngineCreationContext: EngineCreationContext = engineCreationContext

  override def getDefaultEngineRunTypeLabel(): EngineRunTypeLabel = {
    val runTypeLabel = new EngineRunTypeLabel
    runTypeLabel.setRunType(RunType.HIVE.toString)
    runTypeLabel
  }
}
