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

package org.apache.linkis.ujes.jdbc

import java.sql.{SQLException, Timestamp, Types}
import java.util.Locale

object UJESSQLTypeParser {

  def parserFromName(typeName: String): Int = {
    val typeNameLowerCase = typeName.toLowerCase(Locale.getDefault())
    typeName.toLowerCase() match {
      case null => throw new LinkisSQLException(LinkisSQLErrorCode.METADATA_EMPTY)
      case "string" => Types.NVARCHAR
      case "short" => Types.SMALLINT
      case "int" => Types.INTEGER
      case "integer" => Types.INTEGER
      case "long" => Types.BIGINT
      case "float" => Types.FLOAT
      case "double" => Types.DOUBLE
      case "boolean" => Types.BOOLEAN
      case "byte" => Types.TINYINT
      case "char" => Types.CHAR
      case "timestamp" => Types.TIMESTAMP
      case "varchar" => Types.VARCHAR
      case "tinyint" => Types.TINYINT
      case "smallint" => Types.SMALLINT
      case "decimal" => Types.DECIMAL
      case "date" => Types.DATE
      case "bigint" => Types.BIGINT
      case "array" => Types.ARRAY
      case "map" => Types.JAVA_OBJECT
      case _ =>
        if (typeNameLowerCase.startsWith("decimal")) {
          Types.DECIMAL
        } else {
          Types.NVARCHAR
        }
    }
  }

  def parserFromVal(obj: Any): Int = {
    obj match {
      case _: String => Types.NVARCHAR
      case _: Short => Types.SMALLINT
      case _: Int => Types.INTEGER
      case _: Long => Types.BIGINT
      case _: Float => Types.FLOAT
      case _: Double => Types.DOUBLE
      case _: Boolean => Types.BOOLEAN
      case _: Byte => Types.TINYINT
      case _: Char => Types.CHAR
      case _: BigDecimal => Types.DECIMAL
      case _: Timestamp => Types.TIMESTAMP
      case _ => throw new LinkisSQLException(LinkisSQLErrorCode.PREPARESTATEMENT_TYPEERROR)
    }
  }

  def parserFromMetaData(dataType: Int): String = {
    dataType match {
      case Types.CHAR => "string"
      case Types.SMALLINT => "short"
      case Types.INTEGER => "int"
      case Types.BIGINT => "long"
      case Types.FLOAT => "float"
      case Types.DOUBLE => "double"
      case Types.BOOLEAN => "boolean"
      case Types.TINYINT => "byte"
      case Types.CHAR => "char"
      case Types.TIMESTAMP => "timestamp"
      case Types.DECIMAL => "decimal"
      case Types.VARCHAR => "varchar"
      case Types.NVARCHAR => "string"
      case Types.DATE => "date"
      case _ => throw new LinkisSQLException(LinkisSQLErrorCode.PREPARESTATEMENT_TYPEERROR)
    }
  }

}
