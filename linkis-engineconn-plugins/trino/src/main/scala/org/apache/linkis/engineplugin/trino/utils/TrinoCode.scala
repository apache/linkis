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

package org.apache.linkis.engineplugin.trino.utils

import org.apache.linkis.engineplugin.trino.conf.TrinoConfiguration
import org.apache.linkis.engineplugin.trino.exception.{
  TrinoGrantmaException,
  TrinoModifySchemaException
}

object TrinoCode {
  private val SPACE = " "

  private def willGrant(code: String): Boolean = {
    code.matches("(?=.*grant)(?=.*on)(?=.*to)^.*$")
  }

  private def willModifySchema(code: String): Boolean = {
    val trimmedCode = code
      .replaceAll("\n", SPACE)
      .replaceAll("\\s+", SPACE)
      .toLowerCase()
    trimmedCode.contains("create schema") || trimmedCode.contains("drop schema") || trimmedCode
      .contains("alter schema")
  }

  def checkCode(code: String): Unit = {
    if (
        TrinoConfiguration.TRINO_FORBID_MODIFY_SCHEMA.getValue && TrinoCode.willModifySchema(code)
    ) {
      throw TrinoModifySchemaException("CREATE, ALTER, DROP SCHEMA is not allowed")
    }
    if (TrinoConfiguration.TRINO_FORBID_GRANT.getValue && TrinoCode.willGrant(code)) {
      throw TrinoGrantmaException("Grant schema or table is not allowed")
    }
  }

}
