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

import java.io.{InputStream, Reader}
import java.net.URL
import java.sql.{
  Blob,
  Clob,
  Date,
  NClob,
  ParameterMetaData,
  PreparedStatement,
  Ref,
  ResultSetMetaData,
  RowId,
  SQLXML,
  Time,
  Timestamp
}
import java.util
import java.util.Calendar

class LinkisSQLPreparedStatement(ujesSQLConnection: LinkisSQLConnection, sql: String)
    extends LinkisSQLStatement(ujesSQLConnection)
    with PreparedStatement {

  private val parameters = new util.HashMap[Int, Any]

  private var parameterMetaData: ParameterMetaData = _

  private var batchTimes = 0

  private def updateSql(sql: String, parameters: util.HashMap[Int, Any]): String = {
    if (!sql.contains("?")) {
      sql
    } else {
      val newSql = new StringBuilder(sql)
      for (paramLoc <- 1 to parameters.size()) {
        if (parameters.containsKey(paramLoc)) {
          val charIndex = getCharIndexFromSqlByParamLocation(newSql.toString(), '?', 1)
          newSql.deleteCharAt(charIndex)
          newSql.insert(charIndex, parameters.get(paramLoc).asInstanceOf[String])
        }
      }
      newSql.toString()
    }
  }

  private def getCharIndexFromSqlByParamLocation(sql: String, cchar: Char, paramLoc: Int): Int = {
    var signalCount = 0
    var charIndex = -1
    var num = 0
    for (i <- 0 to sql.length - 1 if charIndex == -1) {
      val c = sql.charAt(i)
      if (c != '\'' && c != '\\') {
        if (c == cchar && signalCount % 2 == 0) {
          num += 1
          if (num == paramLoc) {
            charIndex = i
          }
        }
      } else {
        signalCount += signalCount
      }
    }
    charIndex
  }

  override def executeQuery(): UJESSQLResultSet = {
    super.executeQuery(updateSql(sql, parameters))
  }

  override def executeUpdate(): Int = {
    super.executeUpdate(updateSql(sql, parameters))
  }

  override def setNull(parameterIndex: Int, sqlType: Int): Unit = {
    parameters.put(parameterIndex, "NULL")
  }

  override def setBoolean(parameterIndex: Int, x: Boolean): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setByte(parameterIndex: Int, x: Byte): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setShort(parameterIndex: Int, x: Short): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setInt(parameterIndex: Int, x: Int): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setLong(parameterIndex: Int, x: Long): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setFloat(parameterIndex: Int, x: Float): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setDouble(parameterIndex: Int, x: Double): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setBigDecimal(parameterIndex: Int, x: java.math.BigDecimal): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setString(parameterIndex: Int, x: String): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setBytes(parameterIndex: Int, x: Array[Byte]): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setDate(parameterIndex: Int, x: Date): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setTime(parameterIndex: Int, x: Time): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setTimestamp(parameterIndex: Int, x: Timestamp): Unit = {
    parameters.put(parameterIndex, x + "")
  }

  override def setAsciiStream(parameterIndex: Int, x: InputStream, length: Int): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setUnicodeStream(parameterIndex: Int, x: InputStream, length: Int): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setBinaryStream(parameterIndex: Int, x: InputStream, length: Int): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def clearParameters(): Unit = {
    parameters.clear()
  }

  override def setObject(parameterIndex: Int, x: scala.Any, targetSqlType: Int): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setObject(parameterIndex: Int, x: scala.Any): Unit = {
    x match {
      case null => setNull(parameterIndex, 0)
      case x: String => setString(parameterIndex, x)
      case x: Short => setShort(parameterIndex, x)
      case x: Int => setInt(parameterIndex, x)
      case x: Long => setLong(parameterIndex, x)
      case x: Float => setFloat(parameterIndex, x)
      case x: Double => setDouble(parameterIndex, x)
      case x: Boolean => setBoolean(parameterIndex, x)
      case x: Byte => setByte(parameterIndex, x)
      case x: Char => setString(parameterIndex, x.toString)
      case x: Timestamp => setTimestamp(parameterIndex, x)
      case _ =>
        throw new LinkisSQLException(
          LinkisSQLErrorCode.PREPARESTATEMENT_TYPEERROR,
          s"Can''t infer the SQL type to use for an instance of ${x.getClass.getName}. Use setObject() with an explicit Types value to specify the type to use"
        )
    }
  }

  override def execute(): Boolean = {
    val res = super.execute(updateSql(sql, parameters))
    for (i <- 1 to batchTimes) {
      super.execute(updateSql(sql, parameters))
    }
    res
  }

  override def executeBatch(): Array[Int] = {

    for (i <- 0 to batchTimes) {
      super.execute(updateSql(sql, parameters))
    }
    Array(1, 1)
  }

  override def addBatch(): Unit = {
    this.batchTimes = this.batchTimes + 1
  }

  override def setCharacterStream(parameterIndex: Int, reader: Reader, length: Int): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setRef(parameterIndex: Int, x: Ref): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setBlob(parameterIndex: Int, x: Blob): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setClob(parameterIndex: Int, x: Clob): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setArray(parameterIndex: Int, x: java.sql.Array): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def getMetaData: ResultSetMetaData = {
    if (super.getResultSet == null) {
      return null
    }
    super.getResultSet.getMetaData
  }

  override def setDate(parameterIndex: Int, x: Date, cal: Calendar): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setTime(parameterIndex: Int, x: Time, cal: Calendar): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setTimestamp(parameterIndex: Int, x: Timestamp, cal: Calendar): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setNull(parameterIndex: Int, sqlType: Int, typeName: String): Unit = {
    parameters.put(parameterIndex, "NULL")
  }

  override def setURL(parameterIndex: Int, x: URL): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def getParameterMetaData: ParameterMetaData = {
    synchronized {
      if (null == this.parameterMetaData) {
        this.parameterMetaData = new LinkisParameterMetaData(sql.count(_ == '?'))
      }
    }
    this.parameterMetaData
  }

  override def setRowId(parameterIndex: Int, x: RowId): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setNString(parameterIndex: Int, value: String): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setNCharacterStream(parameterIndex: Int, value: Reader, length: Long): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setNClob(parameterIndex: Int, value: NClob): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setClob(parameterIndex: Int, reader: Reader, length: Long): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setBlob(parameterIndex: Int, inputStream: InputStream, length: Long): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setNClob(parameterIndex: Int, reader: Reader, length: Long): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setSQLXML(parameterIndex: Int, xmlObject: SQLXML): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setObject(
      parameterIndex: Int,
      x: scala.Any,
      targetSqlType: Int,
      scaleOrLength: Int
  ): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setAsciiStream(parameterIndex: Int, x: InputStream, length: Long): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setBinaryStream(parameterIndex: Int, x: InputStream, length: Long): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setCharacterStream(parameterIndex: Int, reader: Reader, length: Long): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setAsciiStream(parameterIndex: Int, x: InputStream): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setBinaryStream(parameterIndex: Int, x: InputStream): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setCharacterStream(parameterIndex: Int, reader: Reader): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setNCharacterStream(parameterIndex: Int, value: Reader): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setClob(parameterIndex: Int, reader: Reader): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setBlob(parameterIndex: Int, inputStream: InputStream): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def setNClob(parameterIndex: Int, reader: Reader): Unit = {
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT)
  }

  override def getResultSetType: Int = {
    super.getResultSetType
  }

}
