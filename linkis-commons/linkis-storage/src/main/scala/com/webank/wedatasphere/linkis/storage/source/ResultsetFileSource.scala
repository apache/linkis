package com.webank.wedatasphere.linkis.storage.source

import com.webank.wedatasphere.linkis.storage.resultset.table.TableRecord
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils


class ResultsetFileSource(fileSplits: Array[FileSplit]) extends AbstractFileSource(fileSplits) {

  shuffle({
    case t: TableRecord => new TableRecord(t.row.map {
      case null | "NULL" | "" => getParams.getOrDefault("nullValue", "NULL")
      case value: Double => StorageUtils.doubleToString(value)
      case r => r
    })
    case record => record
  })

}
