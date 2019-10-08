package com.webank.wedatasphere.linkis.ujes.jdbc

import java.sql.{Connection, DatabaseMetaData, ResultSet, RowIdLifetime}

/**
  * Created by owenxu on 2019/8/8.
  */
class UJESSQLDatabaseMetaData(ujesSQLConnection: UJESSQLConnection) extends DatabaseMetaData {
  override def allProceduresAreCallable(): Boolean = false

  override def allTablesAreSelectable(): Boolean = false

  override def getURL: String = ujesSQLConnection.getProps.getProperty("URL")

  override def getUserName: String =
    if(ujesSQLConnection.getProps.containsKey("user"))
      ujesSQLConnection.getProps.getProperty("user")
    else throw new UJESSQLException(UJESSQLErrorCode.PARAMS_NOT_FOUND,"Missing user information")

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

  override def getSQLKeywords: String = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getSQLKeywords not supported")

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

  override def isCatalogAtStart: Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "isCatalogAtStart not supported")

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

  override def getMaxBinaryLiteralLength: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "getMaxBinaryLiteralLength not supported")

  override def getMaxCharLiteralLength: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxCharLiteralLength not supported")

  override def getMaxColumnNameLength: Int = 128

  override def getMaxColumnsInGroupBy: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "getMaxColumnsInGroupBy not supported")

  override def getMaxColumnsInIndex: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxColumnsInIndex not supported")

  override def getMaxColumnsInOrderBy: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxColumnsInOrderBy not supported")

  override def getMaxColumnsInSelect: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxColumnsInSelect not supported")

  override def getMaxColumnsInTable: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxColumnsInTable not supported")

  override def getMaxConnections: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "getMaxConnections not supported")

  override def getMaxCursorNameLength: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxCursorNameLength not supported")

  override def getMaxIndexLength: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxIndexLength not supported")

  override def getMaxSchemaNameLength: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxSchemaNameLength not supported")

  override def getMaxProcedureNameLength: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxProcedureNameLength not supported")

  override def getMaxCatalogNameLength: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxCatalogNameLength not supported")

  override def getMaxRowSize: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxRowSize not supported")

  override def doesMaxRowSizeIncludeBlobs(): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"doesMaxRowSizeIncludeBlobs not supported")

  override def getMaxStatementLength: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxStatementLength not supported")

  override def getMaxStatements: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxStatements not supported")

  override def getMaxTableNameLength: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxTableNameLength not supported")

  override def getMaxTablesInSelect: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxTablesInSelect not supported")

  override def getMaxUserNameLength: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getMaxUserNameLength not supported")

  override def getDefaultTransactionIsolation: Int = 0

  override def supportsTransactions(): Boolean = false

  override def supportsTransactionIsolationLevel(level: Int): Boolean = false

  override def supportsDataDefinitionAndDataManipulationTransactions(): Boolean = false

  override def supportsDataManipulationTransactionsOnly(): Boolean = false

  override def dataDefinitionCausesTransactionCommit(): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"dataDefinitionCausesTransactionCommit not supported")

  override def dataDefinitionIgnoredInTransactions(): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"dataDefinitionIgnoredInTransactions not supported")

  override def getProcedures(catalog: String, schemaPattern: String, procedureNamePattern: String): ResultSet = null

  override def getProcedureColumns(catalog: String, schemaPattern: String, procedureNamePattern: String, columnNamePattern: String): ResultSet = null

  override def getTables(catalog: String, schemaPattern: String, tableNamePattern: String, types: Array[String]): ResultSet = null

  override def getSchemas: ResultSet = null

  override def getCatalogs: ResultSet = null

  override def getTableTypes: ResultSet = null

  override def getColumns(catalog: String, schemaPattern: String, tableNamePattern: String, columnNamePattern: String): ResultSet = null

  override def getColumnPrivileges(catalog: String, schema: String, table: String, columnNamePattern: String): ResultSet = null

  override def getTablePrivileges(catalog: String, schemaPattern: String, tableNamePattern: String): ResultSet = null

  override def getBestRowIdentifier(catalog: String, schema: String, table: String, scope: Int, nullable: Boolean): ResultSet = null

  override def getVersionColumns(catalog: String, schema: String, table: String): ResultSet = null

  override def getPrimaryKeys(catalog: String, schema: String, table: String): ResultSet = null

  override def getImportedKeys(catalog: String, schema: String, table: String): ResultSet = null

  override def getExportedKeys(catalog: String, schema: String, table: String): ResultSet = null

  override def getCrossReference(parentCatalog: String, parentSchema: String, parentTable: String, foreignCatalog: String, foreignSchema: String, foreignTable: String): ResultSet = null

  override def getTypeInfo: ResultSet = null

  override def getIndexInfo(catalog: String, schema: String, table: String, unique: Boolean, approximate: Boolean): ResultSet = null

  override def supportsResultSetType(`type`: Int): Boolean = true

  override def supportsResultSetConcurrency(`type`: Int, concurrency: Int): Boolean = false

  override def ownUpdatesAreVisible(`type`: Int): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"ownUpdatesAreVisible not supported")

  override def ownDeletesAreVisible(`type`: Int): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"ownDeletesAreVisible not supported")

  override def ownInsertsAreVisible(`type`: Int): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"ownInsertsAreVisible not supported")

  override def othersUpdatesAreVisible(`type`: Int): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"othersUpdatesAreVisible not supported")

  override def othersDeletesAreVisible(`type`: Int): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"othersDeletesAreVisible not supported")

  override def othersInsertsAreVisible(`type`: Int): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"othersInsertsAreVisible not supported")

  override def updatesAreDetected(`type`: Int): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"updatesAreDetected not supported")

  override def deletesAreDetected(`type`: Int): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"deletesAreDetected not supported")

  override def insertsAreDetected(`type`: Int): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA, "insertsAreDetected not supported")

  override def supportsBatchUpdates(): Boolean = false

  override def getUDTs(catalog: String, schemaPattern: String, typeNamePattern: String, types: Array[Int]): ResultSet = null

  override def getConnection: Connection = ujesSQLConnection

  override def supportsSavepoints(): Boolean = false

  override def supportsNamedParameters(): Boolean = false

  override def supportsMultipleOpenResults(): Boolean = false

  override def supportsGetGeneratedKeys(): Boolean = false

  override def getSuperTypes(catalog: String, schemaPattern: String, typeNamePattern: String): ResultSet = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getSuperTypes not supported")

  override def getSuperTables(catalog: String, schemaPattern: String, tableNamePattern: String): ResultSet = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getSuperTables not supported")

  override def getAttributes(catalog: String, schemaPattern: String, typeNamePattern: String, attributeNamePattern: String): ResultSet = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getAttributes not supported")

  override def supportsResultSetHoldability(holdability: Int): Boolean = false

  override def getResultSetHoldability: Int = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getResultSetHoldability not supported")

  override def getDatabaseMajorVersion: Int = 1

  override def getDatabaseMinorVersion: Int = 1

  override def getJDBCMajorVersion: Int = 3

  override def getJDBCMinorVersion: Int = 0

  override def getSQLStateType: Int = 2

  override def locatorsUpdateCopy(): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"locatorsUpdateCopy not supported")

  override def supportsStatementPooling(): Boolean = false

  override def getRowIdLifetime: RowIdLifetime = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getRowIdLifetime not supported")

  override def getSchemas(catalog: String, schemaPattern: String): ResultSet = null

  override def supportsStoredFunctionsUsingCallSyntax(): Boolean = false

  override def autoCommitFailureClosesAllResultSets(): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"autoCommitFailureClosesAllResultSets not supported")

  override def getClientInfoProperties: ResultSet = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"getClientInfoProperties not supported")

  override def getFunctions(catalog: String, schemaPattern: String, functionNamePattern: String): ResultSet = null

  override def getFunctionColumns(catalog: String, schemaPattern: String, functionNamePattern: String, columnNamePattern: String): ResultSet = null

  override def getPseudoColumns(catalog: String, schemaPattern: String, tableNamePattern: String, columnNamePattern: String): ResultSet = null

  override def generatedKeyAlwaysReturned(): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"generatedKeyAlwaysReturned not supported")

  override def unwrap[T](iface: Class[T]): T = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"unwrap not supported")

  override def isWrapperFor(iface: Class[_]): Boolean = throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_METADATA,"isWrapperFor not supported")
}
