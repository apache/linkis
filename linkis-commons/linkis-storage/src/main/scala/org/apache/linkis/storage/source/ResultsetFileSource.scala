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

package org.apache.linkis.storage.source

import org.apache.linkis.storage.domain.Dolphin
import org.apache.linkis.storage.resultset.table.TableRecord
import org.apache.linkis.storage.utils.StorageUtils

class ResultsetFileSource(fileSplits: Array[FileSplit]) extends AbstractFileSource(fileSplits) {

  shuffle({
    case t: TableRecord =>
      new TableRecord(t.row.map { rvalue =>
        {
          rvalue match {
            case null | "NULL" =>
              val nullValue = getParams.getOrDefault("nullValue", "NULL")
              if (nullValue.equals(Dolphin.LINKIS_NULL)) {
                rvalue
              } else {
                nullValue
              }
            case "" =>
              val nullValue = getParams.getOrDefault("nullValue", "")
              if (nullValue.equals(Dolphin.LINKIS_NULL)) {
                ""
              } else {
                nullValue
              }
            case value: Double => StorageUtils.doubleToString(value)
            case _ => rvalue
          }
        }
      })
    case record => record
  })

}
