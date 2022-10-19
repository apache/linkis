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

package org.apache.linkis.engineconn.computation.executor.rs

import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.common.io.resultset.ResultSetWriter
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.storage.LineRecord

import java.io.OutputStream

import scala.collection.mutable.ArrayBuffer

class RsOutputStream extends OutputStream with Logging {
  private val line = ArrayBuffer[Byte]()
  private var isReady = false
  private var writer: ResultSetWriter[_ <: MetaData, _ <: Record] = _

  override def write(b: Int): Unit = if (isReady) synchronized {
    if (writer != null) {
      if (b == '\n') {
        val outStr = new String(line.toArray, "UTF-8")
        writer.addRecord(new LineRecord(outStr))
        line.clear()
      } else line += b.toByte
    } else {
      logger.warn("writer is null")
    }
  }

  def reset(engineExecutionContext: EngineExecutionContext): Unit = {
    writer = engineExecutionContext.createDefaultResultSetWriter()
    writer.addMetaData(null)
  }

  def ready(): Unit = isReady = true

  override def flush(): Unit = if (writer != null && line.nonEmpty) {
    val outStr = new String(line.toArray, "UTF-8")
    writer.addRecord(new LineRecord(outStr))
    line.clear()
  }

  override def toString: String = if (writer != null) writer.toString() else null

  override def close(): Unit = if (writer != null) {
    flush()
    writer.close()
    writer = null
  }

}
