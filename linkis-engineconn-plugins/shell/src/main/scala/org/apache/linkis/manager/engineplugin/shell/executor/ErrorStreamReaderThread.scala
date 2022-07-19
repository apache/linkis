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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.log.LogHelper
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext

import java.io.BufferedReader

class ErrorStreamReaderThread extends Thread with Logging {
  private var engineExecutionContext: EngineExecutionContext = _
  private var errReader : BufferedReader = _
  private var extractor: YarnAppIdExtractor = _

  def this(engineExecutionContext: EngineExecutionContext, errReader: BufferedReader, extractor: YarnAppIdExtractor) {
    this()
    this.errReader = errReader
    this.engineExecutionContext = engineExecutionContext
    this.extractor = extractor
  }

  def onDestroy(): Unit = {
    Utils.tryCatch{
      errReader synchronized errReader.close()
    }{ t =>
      logger.warn("Error while closing the error stream", t)
    }
  }


  def startReaderThread(): Unit = {
    Utils.tryCatch {
      this.start()
    }{ t =>
      if (t.isInstanceOf[OutOfMemoryError]) {
        logger.warn("Caught " + t + ". One possible reason is that ulimit" + " setting of 'max user processes' is too low. If so, do" + " 'ulimit -u <largerNum>' and try again.")
      }
      logger.warn("Cannot start thread to read from error stream", t)
    }
  }

  override def run(): Unit = {
    Utils.tryCatch {
      var line = errReader.readLine
      while ( {
        (line != null) && !isInterrupted
      }) {
        LogHelper.logCache.cacheLog(line)
        extractor.appendLineToExtractor(line)
        line = errReader.readLine
      }
    }{ t =>
      logger.warn("Error reading the error stream", t)
    }
  }
}
