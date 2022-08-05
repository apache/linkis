/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.engineplugin.shell.executor

import org.apache.commons.lang3.StringUtils
import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import java.io.BufferedReader
import java.util


class ReaderThread extends Thread with Logging {
  private var engineExecutionContext: EngineExecutionContext = _
  private var inputReader: BufferedReader = _
  private var extractor: YarnAppIdExtractor = _
  private var isStdout: Boolean = false
  private val logListCount = CommonVars[Int]("wds.linkis.engineconn.log.list.count", 50)

  def this(engineExecutionContext: EngineExecutionContext, inputReader: BufferedReader, extractor: YarnAppIdExtractor, isStdout: Boolean) {
    this()
    this.inputReader = inputReader
    this.engineExecutionContext = engineExecutionContext
    this.extractor = extractor
    this.isStdout = isStdout
  }

  def onDestroy(): Unit = {
    Utils.tryCatch {
      inputReader synchronized inputReader.close()
    } { t =>
      logger.warn("inputReader while closing the error stream", t)
    }
  }


  def startReaderThread(): Unit = {
    Utils.tryCatch {
      this.start()
    } { t =>
      if (t.isInstanceOf[OutOfMemoryError]) {
        logger.warn("Caught " + t + ". One possible reason is that ulimit" + " setting of 'max user processes' is too low. If so, do" + " 'ulimit -u <largerNum>' and try again.")
      }
      logger.warn("Cannot start thread to read from inputReader stream", t)
    }
  }

  override def run(): Unit = {
    Utils.tryCatch {
      var line: String = null
      val logArray: util.List[String] = new util.ArrayList[String]
      while ({line = inputReader.readLine(); line != null}) {
        logger.info("read logger line :{}", line)
        logArray.add(line)
        if (logArray.size > logListCount.getValue) {
          val linelist = StringUtils.join(logArray, "\n")
          extractor.appendLineToExtractor(linelist)
          if (isStdout) engineExecutionContext.appendStdout(linelist)
          engineExecutionContext.appendTextResultSet(linelist)
          logArray.clear()
        }
      }
      if (logArray.size > 0) {
        val linelist = StringUtils.join(logArray, "\n")
        extractor.appendLineToExtractor(linelist)
        if (isStdout) engineExecutionContext.appendStdout(linelist)
        engineExecutionContext.appendTextResultSet(linelist)
        logArray.clear()
      }
    } { t =>
      logger.warn("inputReader reading the input stream", t)
    }
  }
}
