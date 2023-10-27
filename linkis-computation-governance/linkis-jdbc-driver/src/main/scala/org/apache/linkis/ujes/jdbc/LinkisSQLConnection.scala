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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.engine.{EngineType, EngineTypeLabel, RunType}
import org.apache.linkis.manager.label.utils.EngineTypeLabelCreator
import org.apache.linkis.ujes.client.UJESClient
import org.apache.linkis.ujes.client.request.JobSubmitAction
import org.apache.linkis.ujes.client.response.JobExecuteResult
import org.apache.linkis.ujes.jdbc.UJESSQLDriverMain._
import org.apache.linkis.ujes.jdbc.utils.JDBCUtils

import org.apache.commons.lang3.StringUtils

import java.{sql, util}
import java.sql.{
  Blob,
  CallableStatement,
  Clob,
  Connection,
  DatabaseMetaData,
  NClob,
  PreparedStatement,
  ResultSet,
  Savepoint,
  SQLException,
  SQLWarning,
  SQLXML,
  Statement,
  Struct
}
import java.util.Properties
import java.util.concurrent.Executor

import scala.collection.JavaConverters._

class LinkisSQLConnection(private[jdbc] val ujesClient: UJESClient, props: Properties)
    extends Connection
    with Logging {

  private[jdbc] var creator = "JDBCDriver"

  private[jdbc] var tableauFlag = false

  private[jdbc] val variableMap = {
    val params = props.getProperty(PARAMS)
    val map = new util.HashMap[String, AnyRef]
    if (params != null) {
      params.split(PARAM_SPLIT).map(_.split(KV_SPLIT)).foreach {
        case Array(k, v) if k.startsWith(VARIABLE_HEADER) =>
          map.put(k.substring(VARIABLE_HEADER.length), v)
        case Array(CREATOR, v) =>
          creator = v
        case _ =>
      }
    }
    map
  }

  def isTableau(): Boolean = {
    val params = props.getProperty(PARAMS)
    if (params != null) {
      params.split(PARAM_SPLIT).map(_.split(KV_SPLIT)).foreach {
        case Array(TABLEAU, v) =>
          tableauFlag = true
        case _ =>
      }
    }
    tableauFlag
  }

  private[jdbc] val dbName =
    if (StringUtils.isNotBlank(props.getProperty(DB_NAME))) props.getProperty(DB_NAME)
    else "default"

  private val runningSQLStatements = new util.LinkedList[Statement]

  private var closed = false

  private var inited = false

  private[jdbc] val user = props.getProperty(USER)

  private[jdbc] val serverURL = props.getProperty("URL")

  private[jdbc] val fixedSessionEnabled =
    if (
        props
          .containsKey(FIXED_SESSION) && "true".equalsIgnoreCase(props.getProperty(FIXED_SESSION))
    ) {
      true
    } else {
      false
    }

  private val connectionId = JDBCUtils.getUniqId()

  private val labelMap: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]

  private val startupParams: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]

  private val runtimeParams: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]

  private[jdbc] def getEngineType: EngineTypeLabel = {
    val engineType: EngineTypeLabel =
      EngineTypeLabelCreator.createEngineTypeLabel(EngineType.TRINO.toString)
    if (props.containsKey(PARAMS)) {
      val params = props.getProperty(PARAMS)
      if (params != null & params.length() > 0) {
        params.split(PARAM_SPLIT).map(_.split(KV_SPLIT)).foreach {
          case Array(k, v) if k.equals(UJESSQLDriver.ENGINE_TYPE) =>
            if (v.contains('-')) {
              val factory = LabelBuilderFactoryContext.getLabelBuilderFactory
              val label = factory.createLabel(classOf[EngineTypeLabel])
              label.setStringValue(v)
              return label
            } else {
              return EngineTypeLabelCreator.createEngineTypeLabel(v)
            }
          case _ =>
        }
      }
    }
    engineType
  }

  private[jdbc] def throwWhenClosed[T](op: => T): T =
    if (isClosed) throw new LinkisSQLException(LinkisSQLErrorCode.CONNECTION_CLOSED)
    else op

  private def createStatementAndAdd[T <: Statement](op: => T): T = throwWhenClosed {

    val statement = op
    runningSQLStatements.add(statement)
    if (!inited) {
      inited = true
      Utils.tryAndWarn(statement.execute(s"USE $dbName"))
    }
    statement
  }

  def getProps: Properties = props

  def removeStatement(statement: LinkisSQLStatement): Unit = runningSQLStatements.remove(statement)

  override def createStatement(): Statement = createStatementAndAdd(new LinkisSQLStatement(this))

  override def prepareStatement(sql: String): LinkisSQLPreparedStatement = {
    val statement = createStatementAndAdd(new LinkisSQLPreparedStatement(this, sql))
    statement.clearQuery()
    statement
  }

  override def createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement = {
    if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
      throw new SQLException(
        "Statement with resultset concurrency " + resultSetConcurrency + " is not supported",
        "HYC00"
      )
    }
    if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
      throw new SQLException(
        "Statement with resultset type " + resultSetType + " is not supported",
        "HYC00"
      )
    }
    createStatementAndAdd(new LinkisSQLStatement(this))
  }

  override def prepareStatement(sql: String, autoGeneratedKeys: Int): PreparedStatement =
    prepareStatement(sql)

  override def prepareStatement(
      sql: String,
      resultSetType: Int,
      resultSetConcurrency: Int
  ): PreparedStatement = prepareStatement(sql)

  override def getMetaData: DatabaseMetaData = throwWhenClosed(new UJESSQLDatabaseMetaData(this))

  override def close(): Unit = {
    runningSQLStatements.asScala.foreach { statement => Utils.tryQuietly(statement.close()) }
    closed = true
  }

  override def isClosed: Boolean = closed

  override def setReadOnly(readOnly: Boolean): Unit =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "setReadOnly not supported"
    )

  override def isReadOnly: Boolean = false

  override def setCatalog(catalog: String): Unit = throwWhenClosed()

  override def getCatalog: String = ""

  override def setTransactionIsolation(level: Int): Unit = {}

  override def getTransactionIsolation: Int = Connection.TRANSACTION_NONE

  override def getWarnings: SQLWarning = null

  override def clearWarnings(): Unit = {}

  override def setAutoCommit(autoCommit: Boolean): Unit = {}

  override def getAutoCommit: Boolean = true

  override def commit(): Unit = {}

  override def prepareCall(sql: String): CallableStatement =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "prepareCall not supported"
    )

  override def rollback(): Unit =
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_CONNECTION, "rollback not supported")

  override def nativeSQL(sql: String): String =
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_CONNECTION, "nativeSQL not supported")

  override def prepareCall(
      sql: String,
      resultSetType: Int,
      resultSetConcurrency: Int
  ): CallableStatement =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "prepareCall not supported"
    )

  override def getTypeMap: util.Map[String, Class[_]] =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "getTypeMap not supported"
    )

  override def setTypeMap(map: util.Map[String, Class[_]]): Unit =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "setTypeMap not supported"
    )

  override def setHoldability(holdability: Int): Unit = throw new LinkisSQLException(
    LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
    "setHoldability not supported"
  )

  override def getHoldability: Int = 0

  override def setSavepoint(): Savepoint =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "setSavepoint not supported"
    )

  override def setSavepoint(name: String): Savepoint =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "setSavepoint not supported"
    )

  override def rollback(savepoint: Savepoint): Unit =
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_CONNECTION, "rollback not supported")

  override def releaseSavepoint(savepoint: Savepoint): Unit = throw new LinkisSQLException(
    LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
    "releaseSavepoint not supported"
  )

  override def createStatement(
      resultSetType: Int,
      resultSetConcurrency: Int,
      resultSetHoldability: Int
  ): Statement =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "createStatement not supported"
    )

  override def prepareStatement(
      sql: String,
      resultSetType: Int,
      resultSetConcurrency: Int,
      resultSetHoldability: Int
  ): PreparedStatement =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "prepareStatement not supported"
    )

  override def prepareCall(
      sql: String,
      resultSetType: Int,
      resultSetConcurrency: Int,
      resultSetHoldability: Int
  ): CallableStatement =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "prepareCall not supported"
    )

  override def prepareStatement(sql: String, columnIndexes: Array[Int]): PreparedStatement =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "prepareStatement not supported"
    )

  override def prepareStatement(sql: String, columnNames: Array[String]): PreparedStatement =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "prepareStatement not supported"
    )

  override def createClob(): Clob =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "createClob not supported"
    )

  override def createBlob(): Blob =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "createBlob not supported"
    )

  override def createNClob(): NClob =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "createNClob not supported"
    )

  override def createSQLXML(): SQLXML =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "createSQLXML not supported"
    )

  override def isValid(timeout: Int): Boolean = true

  override def setClientInfo(name: String, value: String): Unit =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "setClientInfo not supported"
    )

  override def setClientInfo(properties: Properties): Unit =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "properties not supported"
    )

  override def getClientInfo(name: String): String =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "getClientInfo not supported"
    )

  override def getClientInfo: Properties =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "getClientInfo not supported"
    )

  override def createArrayOf(typeName: String, elements: Array[AnyRef]): sql.Array =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "createArrayOf not supported"
    )

  override def createStruct(typeName: String, attributes: Array[AnyRef]): Struct =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "createStruct not supported"
    )

  override def setSchema(schema: String): Unit = throwWhenClosed {
    if (StringUtils.isBlank(schema)) {
      throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT, "schema is empty!")
    }
    createStatement().execute("use " + schema)
  }

  override def getSchema: String = throwWhenClosed {
    val resultSet = createStatement().executeQuery("SELECT current_database()")
    if (!resultSet.next()) {
      throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT, "Get schema failed!")
    }
    resultSet.getString(1)
  }

  override def abort(executor: Executor): Unit =
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_CONNECTION, "abort not supported")

  override def setNetworkTimeout(executor: Executor, milliseconds: Int): Unit =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "setNetworkTimeout not supported"
    )

  override def getNetworkTimeout: Int = throw new LinkisSQLException(
    LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
    "getNetworkTimeout not supported"
  )

  override def unwrap[T](iface: Class[T]): T =
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_CONNECTION, "unwrap not supported")

  override def isWrapperFor(iface: Class[_]): Boolean =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_CONNECTION,
      "isWrapperFor not supported"
    )

  def addLabels(labels: util.Map[String, AnyRef]): Unit = {
    labelMap.putAll(labels)
  }

  def addStartUpParams(params: util.Map[String, AnyRef]): Unit = {
    startupParams.putAll(params)
  }

  def addRuntimeParams(params: util.Map[String, AnyRef]): Unit = {
    runtimeParams.putAll(params)
  }

  def engineToCodeType(engine: String): String = {
    val runType = EngineType.mapStringToEngineType(engine) match {
      case EngineType.SPARK => RunType.SQL
      case EngineType.HIVE => RunType.HIVE
      case EngineType.REPL => RunType.REPL
      case EngineType.TRINO => RunType.TRINO_SQL
      case EngineType.PRESTO => RunType.PRESTO_SQL
      case EngineType.NEBULA => RunType.NEBULA_SQL
      case EngineType.ELASTICSEARCH => RunType.ES_SQL
      case EngineType.JDBC => RunType.JDBC
      case EngineType.PYTHON => RunType.SHELL
      case _ => RunType.SQL
    }
    runType.toString
  }

  private[jdbc] def toSubmit(code: String): JobExecuteResult = {
    val engineTypeLabel = getEngineType
    labelMap.put(LabelKeyConstant.ENGINE_TYPE_KEY, engineTypeLabel.getStringValue)
    labelMap.put(LabelKeyConstant.USER_CREATOR_TYPE_KEY, s"$user-$creator")
    labelMap.put(LabelKeyConstant.CODE_TYPE_KEY, engineToCodeType(engineTypeLabel.getEngineType))
    if (fixedSessionEnabled) {
      labelMap.put(LabelKeyConstant.FIXED_EC_KEY, connectionId)
      logger.info("Fixed session is enable session id is {}", connectionId)
    }

    val jobSubmitAction = JobSubmitAction.builder
      .addExecuteCode(code)
      .setStartupParams(startupParams)
      .setUser(user)
      .addExecuteUser(user)
      .setLabels(labelMap)
      .setRuntimeParams(runtimeParams)
      .setVariableMap(variableMap)
      .build

    val result = ujesClient.submit(jobSubmitAction)
    if (result.getStatus != 0) {
      throw new SQLException(result.getMessage)
    }
    result
  }

  override def toString: String = "LinkisConnection_" + connectionId

}
