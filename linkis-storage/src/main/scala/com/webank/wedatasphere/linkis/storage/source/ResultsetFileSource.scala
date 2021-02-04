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

package com.webank.wedatasphere.linkis.storage.source

import com.webank.wedatasphere.linkis.storage.resultset.table.TableRecord
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils

/**
  * Created by johnnwang on 2020/1/15.
  */
class ResultsetFileSource extends AbstractFileSource {

  shuffler = {
    case t: TableRecord => new TableRecord(t.row.map {
      case null => params.getOrDefault("nullValue", "NULL")
      case value: Double => StorageUtils.doubleToString(value)
      case r => r
    })
    case record => record
  }

}
