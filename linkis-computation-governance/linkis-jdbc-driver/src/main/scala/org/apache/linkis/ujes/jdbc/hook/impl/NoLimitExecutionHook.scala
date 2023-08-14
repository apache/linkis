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

import org.apache.linkis.ujes.jdbc.UJESSQLDriverMain
import org.apache.linkis.ujes.jdbc.hook.JDBCDriverPreExecutionHook

import java.util.Locale

class NoLimitExecutionHook extends JDBCDriverPreExecutionHook {

  override def callPreExecutionHook(sql: String, skip: Boolean): String = {
    if (UJESSQLDriverMain.LIMIT_ENABLED.equalsIgnoreCase("false")) {
      var noLimitSql = "--set ide.engine.no.limit.allow=true\n" + sql
      val lowerCaseLimitSql = noLimitSql.toLowerCase()
      if (lowerCaseLimitSql.contains("limit ") && lowerCaseLimitSql.contains("tableausql")) {
        val lastIndexOfLimit = lowerCaseLimitSql.lastIndexOf("limit ")
        noLimitSql = noLimitSql.substring(0, lastIndexOfLimit)
      }
      noLimitSql
    } else {
      sql
    }

  }

}
