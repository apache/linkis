package com.webank.wedatasphere.linkis.ujes.jdbc.hook.impl

import com.webank.wedatasphere.linkis.ujes.jdbc.hook.JDBCDriverPreExecutionHook


class TableauPreExecutionHook extends JDBCDriverPreExecutionHook{
  override def callPreExecutionHook(sql: String): String = {
    if (sql.contains("LOCAL TEMPORARY")){
      sql.replace("LOCAL TEMPORARY", "").replace("ON COMMIT PRESERVE ROWS", "")
    } else if (sql.contains("GROUP BY 2")){
      sql.replace("GROUP BY 2","")
    } else if (sql.contains("`#")) {
      sql.replace("`#", "`")
    } else if (sql.startsWith("INSERT")) {
      sql.replace("(COL)", "").replaceAll("VALUES (.*)", "VALUES (1)")
    } else if (sql.contains("CREATE INDEX")) {
      "select 1"
    } else{
      sql.replace("TOP", "").replace("CHECKTOP","")
    }
  }
}
