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

package org.apache.linkis.manager.engineplugin.shell.executor

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import java.io.BufferedReader
import java.util
import java.util.concurrent.CountDownLatch

class ReaderThread extends Thread with Logging {

  private var engineExecutionContext: EngineExecutionContext = _
  private var inputReader: BufferedReader = _
  private var extractor: YarnAppIdExtractor = _
  private var isStdout: Boolean = false
  private val logListCount = CommonVars[Int]("wds.linkis.engineconn.log.list.count", 50)
  private var counter: CountDownLatch = _

  private var isReaderAlive = true

  def this(
      engineExecutionContext: EngineExecutionContext,
      inputReader: BufferedReader,
      extractor: YarnAppIdExtractor,
      isStdout: Boolean,
      counter: CountDownLatch
  ) {
    this()
    this.inputReader = inputReader
    this.engineExecutionContext = engineExecutionContext
    this.extractor = extractor
    this.isStdout = isStdout
    this.counter = counter
  }

  def onDestroy(): Unit = {
    isReaderAlive = false
  }

  def startReaderThread(): Unit = {
    Utils.tryCatch {
      this.start()
    } { t =>
      throw t
    }
  }

  override def run(): Unit = {
    Utils.tryCatch {
      var line: String = null
      val logArray: util.List[String] = new util.ArrayList[String]
      while ({ line = inputReader.readLine(); line != null && isReaderAlive }) {
        logger.info("read logger line :{}", line)
        logArray.add(line)
        extractor.appendLineToExtractor(line)
        if (isStdout) engineExecutionContext.appendTextResultSet(line)
        if (logArray.size > logListCount.getValue) {
          val linelist = StringUtils.join(logArray, "\n")
          engineExecutionContext.appendStdout(linelist)
          logArray.clear()
        }
      }
      if (logArray.size > 0) {
        val linelist = StringUtils.join(logArray, "\n")
        engineExecutionContext.appendStdout(linelist)
        logArray.clear()
      }
    } { t =>
      logger.warn("inputReader reading the input stream", t)
    }
    IOUtils.closeQuietly(inputReader)
    counter.countDown()
  }

}
