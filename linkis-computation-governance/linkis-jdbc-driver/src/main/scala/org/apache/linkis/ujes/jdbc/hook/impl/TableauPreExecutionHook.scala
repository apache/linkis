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

package org.apache.linkis.ujes.jdbc.hook.impl

import org.apache.linkis.ujes.jdbc.hook.JDBCDriverPreExecutionHook

class TableauPreExecutionHook extends JDBCDriverPreExecutionHook {

  override def callPreExecutionHook(sql: String): String = {
    if (
        sql.contains("CREATE INDEX") || sql
          .contains("CREATE TABLE") || sql.contains("INSERT INTO") || sql.contains("DROP TABLE")
    ) {
      "SELECT 1"
    } else if (sql.contains("LOCAL TEMPORARY")) {
      sql.replace("LOCAL TEMPORARY", "").replace("ON COMMIT PRESERVE ROWS", "")
    } else if (sql.contains("GROUP BY 2")) {
      sql.replace("GROUP BY 2", "")
    } else if (sql.contains("`#")) {
      sql.replace("`#", "`")
    } else if (sql.startsWith("INSERT")) {
      sql.replace("(COL)", "").replaceAll("VALUES (.*)", "VALUES (1)")
    } else {
      sql.replace("TOP", "").replace("CHECKTOP", "")
    }
  }

}
