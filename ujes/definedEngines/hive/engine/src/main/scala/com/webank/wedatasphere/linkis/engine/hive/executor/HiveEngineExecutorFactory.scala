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

package com.webank.wedatasphere.linkis.engine.hive.executor

import java.io.PrintStream

import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorFactory}
import com.webank.wedatasphere.linkis.engine.hive.common.HiveUtils
import com.webank.wedatasphere.linkis.engine.hive.exception.HiveSessionStartFailedException
import com.webank.wedatasphere.linkis.server.JMap
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.ql.Driver
import org.apache.hadoop.hive.ql.session.SessionState
import org.apache.hadoop.security.UserGroupInformation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
  * created by cooperyang on 2018/11/22
  * Description:
  */
@Component
class HiveEngineExecutorFactory extends EngineExecutorFactory {
  private val logger = LoggerFactory.getLogger(getClass)
  private val HIVE_QUEUE_NAME:String = "mapreduce.job.queuename"
  private val BDP_QUEUE_NAME:String = "wds.linkis.yarnqueue"
  override def createExecutor(options: JMap[String, String]): EngineExecutor = {
    val hiveConf:HiveConf = HiveUtils.getHiveConf
    hiveConf.setVar(HiveConf.ConfVars.HIVEJAR, HiveUtils.jarOfClass(classOf[Driver])
      .getOrElse(throw HiveSessionStartFailedException(40012 ,"cannot find hive-exec.jar, start session failed!")))
    import scala.collection.JavaConversions._
    options.foreach{ case(k,v) => logger.info(s"key is $k, value is $v")}
    options.filter{case (k,v) => k.startsWith("hive.") || k.startsWith("mapreduce.") || k.startsWith("wds.linkis.")}.foreach{case(k, v) =>
      logger.info(s"key is $k, value is $v")
      if (BDP_QUEUE_NAME.equals(k)) hiveConf.set(HIVE_QUEUE_NAME, v) else hiveConf.set(k, v)}
    val sessionState:SessionState = new SessionState(hiveConf)
    sessionState.out = new PrintStream(System.out, true, "utf-8")
    sessionState.info = new PrintStream(System.out, true, "utf-8")
    sessionState.err = new PrintStream(System.out, true, "utf-8")
    SessionState.start(sessionState)
    val ugi = UserGroupInformation.getCurrentUser
    new HiveEngineExecutor(5000, sessionState, ugi, hiveConf)
  }

}
