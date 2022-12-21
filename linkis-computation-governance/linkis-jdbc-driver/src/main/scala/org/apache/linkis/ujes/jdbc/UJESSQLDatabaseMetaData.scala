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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.ujes.client.request.{GetColumnsAction, GetDBSAction, GetTablesAction}
import org.apache.linkis.ujes.jdbc.entity.JdbcColumn
import org.apache.linkis.ujes.jdbc.utils.JDBCUtils

import org.apache.commons.lang3.StringUtils

import java.sql.{Connection, DatabaseMetaData, ResultSet, RowIdLifetime}
import java.util

import scala.collection.JavaConversions._

class UJESSQLDatabaseMetaData(ujesSQLConnection: UJESSQLConnection)
    extends DatabaseMetaData
    with Logging {
  override def allProceduresAreCallable(): Boolean = false

  override def allTablesAreSelectable(): Boolean = false

  override def getURL: String = ujesSQLConnection.getProps.getProperty("URL")

  override def getUserName: String =
    if (ujesSQLConnection.getProps.containsKey("user"))
      ujesSQLConnection.getProps.getProperty("user")
    else throw new UJESSQLException(UJESSQLErrorCode.PARAMS_NOT_FOUND, "Missing user information")

  override def isReadOnly: Boolean = false

  override def nullsAreSortedHigh(): Boolean = false

  override def nullsAreSortedLow(): Boolean = false

  override def nullsAreSortedAtStart(): Boolean = false

  override def nullsAreSortedAtEnd(): Boolean = false

  override def getDatabaseProductName: String = "linkis"

  override def getDatabaseProductVersion: String = ""

  override def getDriverName: String = "Linkis JDBC Driver"

  override def getDriverVersion: String = UJESSQLDriverMain.DEFAULT_VERSION.toString

  override def getDriverMajorVersion: Int = UJESSQLDriverMain.DEFAULT_VERSION

  override def getDriverMinorVersion: Int = UJESSQLDriverMain.DEFAULT_VERSION

  override def usesLocalFiles(): Boolean = false

  override def usesLocalFilePerTable(): Boolean = false

  override def supportsMixedCaseIdentifiers(): Boolean = false

  override def storesUpperCaseIdentifiers(): Boolean = false

  override def storesLowerCaseIdentifiers(): Boolean = false

  override def storesMixedCaseIdentifiers(): Boolean = false

  override def supportsMixedCaseQuotedIdentifiers(): Boolean = false

  override def storesUpperCaseQuotedIdentifiers(): Boolean = false

  override def storesLowerCaseQuotedIdentifiers(): Boolean = false

  override def storesMixedCaseQuotedIdentifiers(): Boolean = false

  override def getIdentifierQuoteString: String = " "

  override def getSQLKeywords: String =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "getSQLKeywords not supported")

  override def getNumericFunctions: String = ""

  override def getStringFunctions: String = ""

  override def getSystemFunctions: String = ""

  override def getTimeDateFunctions: String = ""

  override def getSearchStringEscape: String = "\\"

  override def getExtraNameCharacters: String = ""

  override def supportsAlterTableWithAddColumn(): Boolean = true

  override def supportsAlterTableWithDropColumn(): Boolean = false

  override def supportsColumnAliasing(): Boolean = true

  override def nullPlusNonNullIsNull(): Boolean = false

  override def supportsConvert(): Boolean = false

  override def supportsConvert(fromType: Int, toType: Int): Boolean = false

  override def supportsTableCorrelationNames(): Boolean = false

  override def supportsDifferentTableCorrelationNames(): Boolean = false

  override def supportsExpressionsInOrderBy(): Boolean = false

  override def supportsOrderByUnrelated(): Boolean = false

  override def supportsGroupBy(): Boolean = true

  override def supportsGroupByUnrelated(): Boolean = false

  override def supportsGroupByBeyondSelect(): Boolean = false

  override def supportsLikeEscapeClause(): Boolean = false

  override def supportsMultipleResultSets(): Boolean = false

  override def supportsMultipleTransactions(): Boolean = false

  override def supportsNonNullableColumns(): Boolean = false

  override def supportsMinimumSQLGrammar(): Boolean = false

  override def supportsCoreSQLGrammar(): Boolean = false

  override def supportsExtendedSQLGrammar(): Boolean = false

  override def supportsANSI92EntryLevelSQL(): Boolean = false

  override def supportsANSI92IntermediateSQL(): Boolean = false

  override def supportsANSI92FullSQL(): Boolean = false

  override def supportsIntegrityEnhancementFacility(): Boolean = false

  override def supportsOuterJoins(): Boolean = true

  override def supportsFullOuterJoins(): Boolean = true

  override def supportsLimitedOuterJoins(): Boolean = true

  override def getSchemaTerm: String = "database"

  override def getProcedureTerm: String = new String("UDF")

  override def getCatalogTerm: String = "instance"

  override def isCatalogAtStart: Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "isCatalogAtStart not supported"
  )

  override def getCatalogSeparator: String = "."

  override def supportsSchemasInDataManipulation(): Boolean = true

  override def supportsSchemasInProcedureCalls(): Boolean = false

  override def supportsSchemasInTableDefinitions(): Boolean = true

  override def supportsSchemasInIndexDefinitions(): Boolean = false

  override def supportsSchemasInPrivilegeDefinitions(): Boolean = false

  override def supportsCatalogsInDataManipulation(): Boolean = false

  override def supportsCatalogsInProcedureCalls(): Boolean = false

  override def supportsCatalogsInTableDefinitions(): Boolean = false

  override def supportsCatalogsInIndexDefinitions(): Boolean = false

  override def supportsCatalogsInPrivilegeDefinitions(): Boolean = false

  override def supportsPositionedDelete(): Boolean = false

  override def supportsPositionedUpdate(): Boolean = false

  override def supportsSelectForUpdate(): Boolean = false

  override def supportsStoredProcedures(): Boolean = false

  override def supportsSubqueriesInComparisons(): Boolean = false

  override def supportsSubqueriesInExists(): Boolean = false

  override def supportsSubqueriesInIns(): Boolean = false

  override def supportsSubqueriesInQuantifieds(): Boolean = false

  override def supportsCorrelatedSubqueries(): Boolean = false

  override def supportsUnion(): Boolean = false

  override def supportsUnionAll(): Boolean = true

  override def supportsOpenCursorsAcrossCommit(): Boolean = false

  override def supportsOpenCursorsAcrossRollback(): Boolean = false

  override def supportsOpenStatementsAcrossCommit(): Boolean = false

  override def supportsOpenStatementsAcrossRollback(): Boolean = false

  override def getMaxBinaryLiteralLength: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxBinaryLiteralLength not supported"
  )

  override def getMaxCharLiteralLength: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxCharLiteralLength not supported"
  )

  override def getMaxColumnNameLength: Int = 128

  override def getMaxColumnsInGroupBy: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxColumnsInGroupBy not supported"
  )

  override def getMaxColumnsInIndex: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxColumnsInIndex not supported"
  )

  override def getMaxColumnsInOrderBy: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxColumnsInOrderBy not supported"
  )

  override def getMaxColumnsInSelect: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxColumnsInSelect not supported"
  )

  override def getMaxColumnsInTable: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxColumnsInTable not supported"
  )

  override def getMaxConnections: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxConnections not supported"
  )

  override def getMaxCursorNameLength: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxCursorNameLength not supported"
  )

  override def getMaxIndexLength: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxIndexLength not supported"
  )

  override def getMaxSchemaNameLength: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxSchemaNameLength not supported"
  )

  override def getMaxProcedureNameLength: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxProcedureNameLength not supported"
  )

  override def getMaxCatalogNameLength: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxCatalogNameLength not supported"
  )

  override def getMaxRowSize: Int =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "getMaxRowSize not supported")

  override def doesMaxRowSizeIncludeBlobs(): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "doesMaxRowSizeIncludeBlobs not supported"
  )

  override def getMaxStatementLength: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxStatementLength not supported"
  )

  override def getMaxStatements: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxStatements not supported"
  )

  override def getMaxTableNameLength: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxTableNameLength not supported"
  )

  override def getMaxTablesInSelect: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxTablesInSelect not supported"
  )

  override def getMaxUserNameLength: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getMaxUserNameLength not supported"
  )

  override def getDefaultTransactionIsolation: Int = 0

  override def supportsTransactions(): Boolean = false

  override def supportsTransactionIsolationLevel(level: Int): Boolean = false

  override def supportsDataDefinitionAndDataManipulationTransactions(): Boolean = false

  override def supportsDataManipulationTransactionsOnly(): Boolean = false

  override def dataDefinitionCausesTransactionCommit(): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "dataDefinitionCausesTransactionCommit not supported"
  )

  override def dataDefinitionIgnoredInTransactions(): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "dataDefinitionIgnoredInTransactions not supported"
  )

  override def getProcedures(
      catalog: String,
      schemaPattern: String,
      procedureNamePattern: String
  ): ResultSet = null

  override def getProcedureColumns(
      catalog: String,
      schemaPattern: String,
      procedureNamePattern: String,
      columnNamePattern: String
  ): ResultSet = null

  override def getTables(
      catalog: String,
      schemaPattern: String,
      tableNamePattern: String,
      types: Array[String]
  ): ResultSet = {
    val resultCatalog = if (StringUtils.isNotBlank(catalog)) {
      catalog
    } else {
      s"${getUserName}_ind"
    }
    logger.info(s"resultCatalog is ${resultCatalog}")
    val getTableAction =
      GetTablesAction.builder().setUser(getUserName).setDatabase(resultCatalog).build()
    val result = ujesSQLConnection.ujesClient.getTables(getTableAction)
    val tables = result.getTables
    val resultTables = new util.ArrayList[util.Map[String, String]]()
    tables.foreach { table =>
      val tableType =
        if (table.get("isView").asInstanceOf[Boolean]) TableType.VIEW.name()
        else TableType.TABLE.name()
      val resultTable = new util.HashMap[String, String]()
      resultTable.put("catalog", resultCatalog)
      resultTable.put("tableName", table.get("tableName").asInstanceOf[String])
      resultTable.put("tableType", tableType)
      if (null == types || types.contains(tableType)) {
        resultTables.add(resultTable)
      }
    }
    val resultSet: LinkisMetaDataResultSet[util.Map[String, String]] =
      new LinkisMetaDataResultSet[util.Map[String, String]](
        util.Arrays.asList("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS"),
        util.Arrays.asList("string", "string", "string", "string", "string"),
        resultTables
      ) {
        private var cnt = 0

        override def next(): Boolean = {
          if (cnt < data.size()) {
            val resultTable = new util.ArrayList[Object](5)
            val table = data.get(cnt)
            resultTable.add(table.get("catalog"))
            resultTable.add(null)
            resultTable.add(table.get("tableName"))
            resultTable.add(table.get("tableType"))
            resultTable.add(table.get(""))
            row = resultTable
            cnt = cnt + 1
            true
          } else {
            false
          }
        }
      }
    logger.info(s"resultSet is ${resultSet}")
    resultSet
  }

  override def getSchemas: ResultSet = {
    getCatalogs
  }

  override def getCatalogs: ResultSet = {
    val getDBSAction = GetDBSAction.builder().setUser(getUserName).build()
    val dBSResult = ujesSQLConnection.ujesClient.getDBS(getDBSAction)
    val dbsName = dBSResult.getDBSName()
    import scala.collection.JavaConversions._
    logger.info(s"dbNames are " + dbsName.mkString(","))
    new LinkisMetaDataResultSet[String](
      util.Arrays.asList("TABLE_SCHEMA", "TABLE_CATALOG"),
      util.Arrays.asList("string", "string"),
      dbsName
    ) {
      private var cnt = 0

      override def next(): Boolean = {
        if (cnt < data.size()) {
          val db = new util.ArrayList[Object](2)
          db.add(data.get(cnt))
          db.add(data.get(cnt))
          row = db
          cnt = cnt + 1
          true
        } else {
          false
        }
      }
    }
  }

  override def getTableTypes: ResultSet = {
    val typesList = TableType.values()
    new LinkisMetaDataResultSet[TableType](
      util.Arrays.asList("TABLE_TYPE"),
      util.Arrays.asList("string"),
      typesList.toList
    ) {
      private var cnt = 0

      override def next(): Boolean = {
        if (cnt < data.size()) {
          val types = new util.ArrayList[Object](1)
          types.add(data.get(cnt).name())
          row = types
          cnt = cnt + 1
          true
        } else {
          false
        }
      }
    }
  }

  override def getColumns(
      catalog: String,
      schemaPattern: String,
      tableNamePattern: String,
      columnNamePattern: String
  ): ResultSet = {
    val resultCatalog = if (StringUtils.isNotBlank(catalog)) {
      catalog
    } else {
      s"${getUserName}_ind"
    }

    val getColumnsAction = GetColumnsAction
      .builder()
      .setUser(getUserName)
      .setDatabase(resultCatalog)
      .setTable(JDBCUtils.convertPattern(tableNamePattern))
      .build()
    val result = ujesSQLConnection.ujesClient.getColumns(getColumnsAction)
    val columns = result.getColumns
    val resultColumns = new util.ArrayList[JdbcColumn]()
    var ordinalPos = 1
    columns.foreach { column =>
      val jdbcColumn = new JdbcColumn(
        column.get("columnName").asInstanceOf[String],
        tableNamePattern,
        resultCatalog,
        column.get("columnType").asInstanceOf[String],
        column.get("columnComment").asInstanceOf[String],
        ordinalPos
      )
      resultColumns.add(jdbcColumn)
      ordinalPos = ordinalPos + 1
    }
    new LinkisMetaDataResultSet[JdbcColumn](
      util.Arrays.asList(
        "TABLE_CAT",
        "TABLE_SCHEM",
        "TABLE_NAME",
        "COLUMN_NAME",
        "DATA_TYPE",
        "TYPE_NAME",
        "COLUMN_SIZE",
        "BUFFER_LENGTH",
        "DECIMAL_DIGITS",
        "NUM_PREC_RADIX",
        "NULLABLE",
        "REMARKS",
        "COLUMN_DEF",
        "SQL_DATA_TYPE",
        "SQL_DATETIME_SUB",
        "CHAR_OCTET_LENGTH",
        "ORDINAL_POSITION",
        "IS_NULLABLE",
        "SCOPE_CATLOG",
        "SCOPE_SCHEMA",
        "SCOPE_TABLE",
        "SOURCE_DATA_TYPE"
      ),
      util.Arrays.asList(
        "string",
        "string",
        "string",
        "string",
        "int",
        "string",
        "int",
        "int",
        "int",
        "int",
        "int",
        "string",
        "string",
        "int",
        "int",
        "int",
        "int",
        "string",
        "string",
        "string",
        "string",
        "int"
      ),
      resultColumns
    ) {

      private var cnt = 0

      override def next(): Boolean = {
        if (cnt < data.size()) {
          val jdbcColumn = new util.ArrayList[Object](20)
          val column = data.get(cnt)
          jdbcColumn.add(column.getTableCatalog) // TABLE_CAT String => table catalog (may be null)

          jdbcColumn.add(null) // TABLE_SCHEM String => table schema (may be null)

          jdbcColumn.add(column.getTableName) // TABLE_NAME String => table name

          jdbcColumn.add(column.getColumnName) // COLUMN_NAME String => column name

          jdbcColumn.add(column.getSqlType) // DATA_TYPE short => SQL type from java.sql.Types

          jdbcColumn.add(column.getType) // TYPE_NAME String => Data source dependent type name.

          jdbcColumn.add(column.getColumnSize) // COLUMN_SIZE int => column size.

          jdbcColumn.add(null) // BUFFER_LENGTH is not used.

          jdbcColumn.add(
            column.getDecimalDigits
          ) // DECIMAL_DIGITS int => number of fractional digits

          jdbcColumn.add(column.getNumPrecRadix) // NUM_PREC_RADIX int => typically either 10 or 2

          jdbcColumn.add(
            DatabaseMetaData.columnNullable.asInstanceOf[Object]
          ) // NULLABLE int => is NULL allowed?

          jdbcColumn.add(
            column.getComment
          ) // REMARKS String => comment describing column (may be null)

          jdbcColumn.add(null) // COLUMN_DEF String => default value (may be null)

          jdbcColumn.add(null) // SQL_DATA_TYPE int => unused

          jdbcColumn.add(null) // SQL_DATETIME_SUB int => unused

          jdbcColumn.add(null) // CHAR_OCTET_LENGTH int

          jdbcColumn.add(column.getOrdinalPos.asInstanceOf[Object]) // ORDINAL_POSITION int

          jdbcColumn.add("YES") // IS_NULLABLE String

          jdbcColumn.add(null) // SCOPE_CATLOG String

          jdbcColumn.add(null) // SCOPE_SCHEMA String

          jdbcColumn.add(null) // SCOPE_TABLE String

          jdbcColumn.add(null) // SOURCE_DATA_TYPE short

          row = jdbcColumn
          cnt = cnt + 1
          true
        } else {
          false
        }
      }
    }

  }

  override def getColumnPrivileges(
      catalog: String,
      schema: String,
      table: String,
      columnNamePattern: String
  ): ResultSet = null

  override def getTablePrivileges(
      catalog: String,
      schemaPattern: String,
      tableNamePattern: String
  ): ResultSet = null

  override def getBestRowIdentifier(
      catalog: String,
      schema: String,
      table: String,
      scope: Int,
      nullable: Boolean
  ): ResultSet = null

  override def getVersionColumns(catalog: String, schema: String, table: String): ResultSet = null

  override def getPrimaryKeys(catalog: String, schema: String, table: String): ResultSet = null

  override def getImportedKeys(catalog: String, schema: String, table: String): ResultSet = null

  override def getExportedKeys(catalog: String, schema: String, table: String): ResultSet = null

  override def getCrossReference(
      parentCatalog: String,
      parentSchema: String,
      parentTable: String,
      foreignCatalog: String,
      foreignSchema: String,
      foreignTable: String
  ): ResultSet = null

  override def getTypeInfo: ResultSet = null

  override def getIndexInfo(
      catalog: String,
      schema: String,
      table: String,
      unique: Boolean,
      approximate: Boolean
  ): ResultSet = null

  override def supportsResultSetType(`type`: Int): Boolean = true

  override def supportsResultSetConcurrency(`type`: Int, concurrency: Int): Boolean = false

  override def ownUpdatesAreVisible(`type`: Int): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "ownUpdatesAreVisible not supported"
  )

  override def ownDeletesAreVisible(`type`: Int): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "ownDeletesAreVisible not supported"
  )

  override def ownInsertsAreVisible(`type`: Int): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "ownInsertsAreVisible not supported"
  )

  override def othersUpdatesAreVisible(`type`: Int): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "othersUpdatesAreVisible not supported"
  )

  override def othersDeletesAreVisible(`type`: Int): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "othersDeletesAreVisible not supported"
  )

  override def othersInsertsAreVisible(`type`: Int): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "othersInsertsAreVisible not supported"
  )

  override def updatesAreDetected(`type`: Int): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "updatesAreDetected not supported"
  )

  override def deletesAreDetected(`type`: Int): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "deletesAreDetected not supported"
  )

  override def insertsAreDetected(`type`: Int): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "insertsAreDetected not supported"
  )

  override def supportsBatchUpdates(): Boolean = false

  override def getUDTs(
      catalog: String,
      schemaPattern: String,
      typeNamePattern: String,
      types: Array[Int]
  ): ResultSet = null

  override def getConnection: Connection = ujesSQLConnection

  override def supportsSavepoints(): Boolean = false

  override def supportsNamedParameters(): Boolean = false

  override def supportsMultipleOpenResults(): Boolean = false

  override def supportsGetGeneratedKeys(): Boolean = false

  override def getSuperTypes(
      catalog: String,
      schemaPattern: String,
      typeNamePattern: String
  ): ResultSet =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "getSuperTypes not supported")

  override def getSuperTables(
      catalog: String,
      schemaPattern: String,
      tableNamePattern: String
  ): ResultSet =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "getSuperTables not supported")

  override def getAttributes(
      catalog: String,
      schemaPattern: String,
      typeNamePattern: String,
      attributeNamePattern: String
  ): ResultSet =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "getAttributes not supported")

  override def supportsResultSetHoldability(holdability: Int): Boolean = false

  override def getResultSetHoldability: Int = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getResultSetHoldability not supported"
  )

  override def getDatabaseMajorVersion: Int = 1

  override def getDatabaseMinorVersion: Int = 1

  override def getJDBCMajorVersion: Int = 3

  override def getJDBCMinorVersion: Int = 0

  override def getSQLStateType: Int = 2

  override def locatorsUpdateCopy(): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "locatorsUpdateCopy not supported"
  )

  override def supportsStatementPooling(): Boolean = false

  override def getRowIdLifetime: RowIdLifetime = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getRowIdLifetime not supported"
  )

  override def getSchemas(catalog: String, schemaPattern: String): ResultSet = {
    logger.info("get schemas for linkis")
    val resultSet = getCatalogs
    logger.info(s"schemas is ${resultSet}")
    resultSet
  }

  override def supportsStoredFunctionsUsingCallSyntax(): Boolean = false

  override def autoCommitFailureClosesAllResultSets(): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "autoCommitFailureClosesAllResultSets not supported"
  )

  override def getClientInfoProperties: ResultSet = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "getClientInfoProperties not supported"
  )

  override def getFunctions(
      catalog: String,
      schemaPattern: String,
      functionNamePattern: String
  ): ResultSet = null

  override def getFunctionColumns(
      catalog: String,
      schemaPattern: String,
      functionNamePattern: String,
      columnNamePattern: String
  ): ResultSet = null

  override def getPseudoColumns(
      catalog: String,
      schemaPattern: String,
      tableNamePattern: String,
      columnNamePattern: String
  ): ResultSet = null

  override def generatedKeyAlwaysReturned(): Boolean = throw new UJESSQLException(
    UJESSQLErrorCode.NOSUPPORT_METADATA,
    "generatedKeyAlwaysReturned not supported"
  )

  override def unwrap[T](iface: Class[T]): T =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "unwrap not supported")

  override def isWrapperFor(iface: Class[_]): Boolean =
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "isWrapperFor not supported")

}
