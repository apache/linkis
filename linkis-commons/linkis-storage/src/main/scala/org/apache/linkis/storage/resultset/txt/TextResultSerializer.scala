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
 
package org.apache.linkis.storage.resultset.txt

import org.apache.linkis.common.io.resultset.ResultSerializer
import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.storage.domain.Dolphin
import org.apache.linkis.storage.{LineMetaData, LineRecord}


class TextResultSerializer extends ResultSerializer{

  override def metaDataToBytes(metaData: MetaData): Array[Byte] = {
    if(metaData == null){
      lineToBytes(null)
    } else {
      val textMetaData = metaData.asInstanceOf[LineMetaData]
      lineToBytes(textMetaData.getMetaData)
    }
  }

  override def recordToBytes(record: Record): Array[Byte] = {
    val textRecord = record.asInstanceOf[LineRecord]
    lineToBytes(textRecord.getLine)
  }

  def lineToBytes(value: String): Array[Byte] = {
    val bytes = if(value == null) Dolphin.NULL_BYTES else Dolphin.getBytes(value)
    Dolphin.getIntBytes(bytes.length) ++ bytes
  }
}
