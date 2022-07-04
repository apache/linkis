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
 
package org.apache.linkis.engineplugin.spark.common

import java.io.InputStream
import java.util.concurrent.locks.ReentrantLock

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.commons.io.IOUtils

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  *
  */
class LineBufferedStream(inputStream: InputStream) extends Logging {

  //  private[this] var _lines: IndexedSeq[String] = IndexedSeq()

  private implicit val executor: ExecutionContext = LineBufferedStream.executor
  private[this] val _lock = new ReentrantLock()
  private[this] val _condition = _lock.newCondition()
  private[this] var _finished = false

  val logSize = CommonVars("query.session.log.hold", 500).getValue
  private[this] val logs = new LogContainer(logSize)

  private val future = Future {
    val iterator = IOUtils.lineIterator(inputStream, "utf-8")
    while (iterator.hasNext && !_finished) {
      logs.putLog(iterator.next())
      notifyTo()
    }
    close
  }

  private def notifyTo() = {
    _lock.lock()
    Utils.tryFinally(_condition.signalAll()){ _lock.unlock()}
  }

  //  private val thread = new Thread {
  //    setName("stdout")
  //
  //    override def run() = {
  //      val lines = Source.fromInputStream(inputStream).getLines()
  //      for (line <- lines if continue) {
  //        _lock.lock()
  //        try {
  //          trace("stdout: ", line)
  //          logs.putLog(line)
  //          _condition.signalAll()
  //        } finally {
  //          _lock.unlock()
  //        }
  //      }
  //
  //      _lock.lock()
  //      try {
  //        _finished = true
  //        _condition.signalAll()
  //      } finally {
  //        _lock.unlock()
  //      }
  //    }
  //  }
  //  thread.setDaemon(true)
  //  thread.start()

  def lines: List[String] = logs.getLogs

  def iterator: Iterator[String] = {
    new LinesIterator
  }

  def close = {
    //    thread.continue = false
    //    thread.interrupt()
    //    thread.join()
    _finished = true
    notifyTo()  //need to notify all.
    IOUtils.closeQuietly(inputStream)
  }

  def waitForClose(atMost: Duration) = {
    Utils.tryQuietly(Utils.waitUntil(() => future.isCompleted, atMost))
    close
  }

  def waitForComplete = {
    Utils.tryQuietly(Await.result(future, Duration.Inf))
    close
  }

  private class LinesIterator extends Iterator[String] {
    private[this] var index = 0
    private[this] var _lines: List[String] = logs.getLogs

    override def hasNext: Boolean = {
      def getAndNext = {
        _lines = logs.getLogs
        index = 0
        if(_lines.length > 0) {
          true
        } else {
          false
        }
      }
      if (_lines != null && index < _lines.length) {
        true
      } else {
        if(getAndNext) return true
        // Otherwise we might still have more data.
        _lock.lock()
        try {
          if (_finished) {
            false
          } else {
            _condition.await()
            !_finished && getAndNext
          }
        } finally {
          _lock.unlock()
        }
      }
    }

    override def next(): String = {
      val line = _lines(index)
      index += 1
      line
    }
  }
}

object LineBufferedStream {
  val executor = Utils.newCachedExecutionContext(SparkConfiguration.PROCESS_MAX_THREADS.getValue, "Shell-Command-stdout-")
}
