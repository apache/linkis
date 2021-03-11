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

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.log

import java.util

import com.webank.wedatasphere.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.LogListener
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.event.TaskLogUpdateEvent
import org.slf4j.LoggerFactory

object LogHelper {
  private val logger = LoggerFactory.getLogger(getClass)
  val logCache = new MountLogCache(AccessibleExecutorConfiguration.ENGINECONN_LOG_CACHE_NUM.getValue)

  private var logListener: LogListener = _

  def setLogListener(logListener: LogListener): Unit = this.logListener = logListener

  def pushAllRemainLogs(): Unit = {
//    logger.info(s"start to push all remain logs")
    Thread.sleep(30)
    //logCache.synchronized{
    if (logListener == null) {
      logger.warn("logListener is null, can not push remain logs")
      //return
    } else {
      var logs: util.List[String] = null
      logCache.synchronized {
        logs = logCache.getRemain
      }
      if (logs != null && logs.size > 0) {
        val sb: StringBuilder = new StringBuilder
        import scala.collection.JavaConversions._
        logs map (log => log + "\n") foreach sb.append
        logger.info(s"remain logs is ${sb.toString()}")
        logListener.onLogUpdate(TaskLogUpdateEvent(null, sb.toString))
      }
    }
    logger.info("end to push all remain logs")
    // }
    //    if (sendAppender == null){
    //      logger.error("SendAppender has not been initialized")
    //    }else{
    //      val logCache = sendAppender.getLogCache
    //      val logListener = SendAppender.getLogListener
    //      logCache.synchronized{
    //        val logs: util.List[String] = logCache.getRemain
    //        if (logs.size > 0) {
    //          val sb: StringBuilder = new StringBuilder
    //          import scala.collection.JavaConversions._
    //          logs map (log => log + "\n") foreach sb.append
    //          logListener.onLogUpdate(null, sb.toString)
    //        }
    //      }
    //    }
  }

}
