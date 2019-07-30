package com.webank.wedatasphere.spark.excel

import org.apache.spark.sql.sources.DataSourceRegister

class DefaultSource15 extends DefaultSource with DataSourceRegister {
  override def shortName(): String = "excel"
}
