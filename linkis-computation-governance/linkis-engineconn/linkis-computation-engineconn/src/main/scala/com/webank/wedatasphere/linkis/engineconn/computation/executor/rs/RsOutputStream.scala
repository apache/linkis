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

package com.webank.wedatasphere.linkis.engineconn.computation.executor.rs

import java.io.OutputStream

import com.webank.wedatasphere.linkis.common.io.resultset.ResultSetWriter
import com.webank.wedatasphere.linkis.common.io.{MetaData, Record}
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.storage.LineRecord

import scala.collection.mutable.ArrayBuffer


class RsOutputStream extends OutputStream with Logging{
  private val line = ArrayBuffer[Byte]()
  private var isReady = false
  private var writer: ResultSetWriter[_ <: MetaData, _ <: Record] = _
  override def write(b: Int) = if(isReady) synchronized {
    if(writer != null) {
      if (b == '\n') {
        val outStr = new String(line.toArray,"UTF-8")
        writer.addRecord(new LineRecord(outStr))
        line.clear()
      } else line += b.toByte
    }else{
       warn("writer is null")
    }
  }

  def reset(engineExecutionContext: EngineExecutionContext) = {
    writer = engineExecutionContext.createDefaultResultSetWriter()
    writer.addMetaData(null)
  }

  def ready() = isReady = true

  override def flush(): Unit = if(writer != null && line.nonEmpty) {
    val outStr = new String(line.toArray,"UTF-8")
    writer.addRecord(new LineRecord(outStr))
    line.clear()
  }

  override def toString = if(writer != null) writer.toString() else null

  override def close() = if(writer != null) {
    flush()
    writer.close()
    writer = null
  }
}
