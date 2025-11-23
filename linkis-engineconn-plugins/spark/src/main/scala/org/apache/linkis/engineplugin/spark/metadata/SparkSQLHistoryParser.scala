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

package org.apache.linkis.engineplugin.spark.metadata

import org.apache.linkis.common.utils.ClassUtils._
import org.apache.linkis.cs.common.entity.history.metadata.TableOperationType
import org.apache.linkis.cs.common.entity.metadata.CSColumn
import org.apache.linkis.engineplugin.spark.metadata.{SparkHiveObject => HPO}

import org.apache.hadoop.hive.ql.security.authorization.plugin.HivePrivilegeObject.HivePrivilegeObjectType
import org.apache.spark.SPARK_VERSION
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.analysis.UnresolvedRelation
import org.apache.spark.sql.catalyst.catalog.CatalogTable
import org.apache.spark.sql.catalyst.expressions.{AttributeReference, NamedExpression}
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.execution.command._
import org.apache.spark.sql.execution.datasources._
import org.apache.spark.sql.hive.execution.CreateHiveTableAsSelectCommand
import org.apache.spark.sql.types.{StructField, StructType}

import java.util.{ArrayList => JAList, List => JList}

import scala.collection.JavaConverters._

/**
 */
object SparkSQLHistoryParser {

  /**
   * Parse input and output metadata from a Spark's [[LogicalPlan]]
   *
   * For [[ExplainCommand]]s, parse its child. For other queries, build inputs.
   *
   * @param plan
   *   A Spark [[LogicalPlan]]
   */
  def parse(plan: LogicalPlan): (JList[HPO], JList[HPO]) = {
    plan match {
      case e: ExplainCommand => doParse(e.logicalPlan)
      case p => doParse(p)
    }
  }

  /**
   * parse outputs if it has an target to write, parse inputs for the inside query if exists.For
   * other queries, only parse inputs.
   *
   * @param plan
   *   A Spark [[LogicalPlan]]
   * @return
   *   (Inputs, OutPuts)
   */
  def doParse(plan: LogicalPlan): (JList[HPO], JList[HPO]) = {
    val inputMetas = new JAList[HPO]
    val outputMetas = new JAList[HPO]
    plan match {
      case cmd: Command => parseRunnableCommand(cmd, inputMetas, outputMetas)
      case _ => ParseQuery(plan, inputMetas)
    }
    (inputMetas, outputMetas)
  }

  def toCSColumns(schema: StructType): JList[CSColumn] = {
    if (null == schema) {
      return null
    }
    schema.map(toCSColumn).filter(null != _).asJava
  }

  def toCSColumns(fields: Seq[StructField]): JList[CSColumn] = {
    if (null == fields) {
      return null
    }
    fields.map(toCSColumn).filter(null != _).asJava
  }

  def toCSColumn(field: StructField): CSColumn = {
    if (null == field) {
      return null
    }
    val csColumn = new CSColumn
    csColumn.setName(field.name)
    csColumn.setComment(field.getComment().orNull)
    csColumn.setType(field.dataType.typeName)
    csColumn
  }

  def toCSColumnsByNamed(projectionList: Seq[NamedExpression]): JList[CSColumn] = {
    if (null == projectionList) {
      return null
    }
    projectionList
      .map { namedExpression =>
        val csColumn = new CSColumn
        csColumn.setName(namedExpression.name)
        namedExpression match {
          case attribute: AttributeReference =>
            csColumn.setType(attribute.dataType.typeName)
          case _ =>
        }
        csColumn
      }
      .filter(null != _)
      .asJava
  }

  def toCSColumnsByColumnName(columnNames: Seq[String]): JList[CSColumn] = {
    if (null == columnNames) {
      return null
    }
    columnNames
      .map { name =>
        val csColumn = new CSColumn
        csColumn.setName(name)
        csColumn
      }
      .filter(null != _)
      .asJava
  }

  /**
   * Parse LogicalPlan to build sparkHiveObjects
   *
   * @param plan
   *   A Spark [[LogicalPlan]]
   * @param sparkHiveObjects
   *   input or output hive privilege object list
   * @param projectionList
   *   Projection list after pruning
   */
  private[this] def ParseQuery(
      plan: LogicalPlan,
      sparkHiveObjects: JList[HPO],
      projectionList: Seq[NamedExpression] = Nil
  ): Unit = {

    /**
     * Columns in Projection take priority for column level privilege checking
     *
     * @param table
     *   catalogTable of a given relation
     */
    def mergeProjection(table: CatalogTable): Unit = {
      if (projectionList.isEmpty) {
        addTableOrViewLevelObjs(
          table.identifier,
          sparkHiveObjects,
          table.partitionColumnNames,
          toCSColumns(table.schema)
        )
      } else {
        addTableOrViewLevelObjs(
          table.identifier,
          sparkHiveObjects,
          table.partitionColumnNames.filter(projectionList.map(_.name).contains(_)),
          toCSColumnsByNamed(projectionList)
        )
      }
    }

    plan match {
      case p: Project => ParseQuery(p.child, sparkHiveObjects, p.projectList)

      case h if h.nodeName == "HiveTableRelation" =>
        mergeProjection(getFieldVal(h, "tableMeta").asInstanceOf[CatalogTable])

      case m if m.nodeName == "MetastoreRelation" =>
        mergeProjection(getFieldVal(m, "catalogTable").asInstanceOf[CatalogTable])

      case l: LogicalRelation if l.catalogTable.nonEmpty => mergeProjection(l.catalogTable.get)

      case u: UnresolvedRelation =>
        addTableOrViewLevelObjs(TableIdentifier(u.tableName), sparkHiveObjects)

      case p =>
        for (child <- p.children) {
          ParseQuery(child, sparkHiveObjects, projectionList)
        }
    }
  }

  /**
   * Build sparkHiveObjects from Spark LogicalPlan
   *
   * @param plan
   *   a Spark LogicalPlan used to generate sparkHiveObjects
   * @param inputObjects
   *   input hive privilege object list
   * @param outputObjects
   *   output hive privilege object list
   */
  private[this] def parseRunnableCommand(
      plan: LogicalPlan,
      inputObjects: JList[HPO],
      outputObjects: JList[HPO]
  ): Unit = {
    plan match {

      case c: CreateDataSourceTableAsSelectCommand =>
        val columnList = toCSColumns(c.table.schema)
        addTableOrViewLevelObjs(
          c.table.identifier,
          outputObjects,
          columns = columnList,
          actionType = TableOperationType.CREATE
        )
        ParseQuery(c.query, inputObjects)

      case c: CreateDataSourceTableCommand =>
        addTableOrViewLevelObjs(
          c.table.identifier,
          outputObjects,
          columns = toCSColumns(c.table.schema),
          actionType = TableOperationType.CREATE
        )

      case c: CreateHiveTableAsSelectCommand =>
        val columnList = toCSColumns(c.tableDesc.schema)
        addTableOrViewLevelObjs(
          c.tableDesc.identifier,
          outputObjects,
          columns = columnList,
          actionType = TableOperationType.CREATE
        )
        ParseQuery(c.query, inputObjects)

      case c: CreateTableCommand =>
        addTableOrViewLevelObjs(
          c.table.identifier,
          outputObjects,
          columns = toCSColumns(c.table.schema),
          actionType = TableOperationType.CREATE
        )

      case c: CreateTableLikeCommand =>
        addTableOrViewLevelObjs(
          c.targetTable,
          outputObjects,
          actionType = TableOperationType.CREATE
        )
        addTableOrViewLevelObjs(c.sourceTable, inputObjects)

      case c: CreateViewCommand =>
        addTableOrViewLevelObjs(
          c.name,
          outputObjects,
          columns = toCSColumnsByNamed(c.output),
          actionType = TableOperationType.CREATE
        )

        // after spark 3.2.0, `child` field will be replaced by `plan` in CreateViewCommand
        val logicalPlan = if (SPARK_VERSION < "3.2") {
          getFieldVal(c, "child").asInstanceOf[LogicalPlan]
        } else {
          getFieldVal(c, "plan").asInstanceOf[LogicalPlan]
        }
        ParseQuery(logicalPlan, inputObjects)

      case l: LoadDataCommand => addTableOrViewLevelObjs(l.table, outputObjects)

      case i if i.nodeName == "InsertIntoHiveTable" =>
        val table = getFieldVal(i, "table").asInstanceOf[CatalogTable]
        addTableOrViewLevelObjs(
          table.identifier,
          outputObjects,
          columns = toCSColumns(table.schema),
          actionType = TableOperationType.CREATE
        )
        ParseQuery(getFieldVal(i, "query").asInstanceOf[LogicalPlan], inputObjects)

      case d: DropTableCommand =>
        addTableOrViewLevelObjs(d.tableName, outputObjects, actionType = TableOperationType.DROP)

      case s: TruncateTableCommand =>
        addTableOrViewLevelObjs(s.tableName, outputObjects, actionType = TableOperationType.DROP)

      case a: AlterTableAddPartitionCommand =>
        addTableOrViewLevelObjs(a.tableName, outputObjects, actionType = TableOperationType.ALTER)

      case a: AlterTableDropPartitionCommand =>
        addTableOrViewLevelObjs(a.tableName, outputObjects)

      case a: AlterTableRenameCommand if !a.isView || a.oldName.database.nonEmpty =>
        addTableOrViewLevelObjs(a.oldName, inputObjects)
        addTableOrViewLevelObjs(a.newName, outputObjects)

      case a: AlterTableRenamePartitionCommand =>
        addTableOrViewLevelObjs(a.tableName, inputObjects)
        addTableOrViewLevelObjs(a.tableName, outputObjects)

      case a: AlterViewAsCommand =>
        if (a.name.database.nonEmpty) {
          // it's a permanent view
          addTableOrViewLevelObjs(a.name, outputObjects, actionType = TableOperationType.ALTER)
        }
        ParseQuery(a.query, inputObjects)

      case a if a.nodeName == "AlterTableAddColumnsCommand" =>
        addTableOrViewLevelObjs(
          getFieldVal(a, "table").asInstanceOf[TableIdentifier],
          inputObjects,
          columns = toCSColumns(getFieldVal(a, "colsToAdd").asInstanceOf[Seq[StructField]])
        )
        addTableOrViewLevelObjs(
          getFieldVal(a, "table").asInstanceOf[TableIdentifier],
          outputObjects,
          columns = toCSColumns(getFieldVal(a, "colsToAdd").asInstanceOf[Seq[StructField]]),
          actionType = TableOperationType.ALTER
        )

      case a if a.nodeName == "AlterTableChangeColumnCommand" =>
        addTableOrViewLevelObjs(
          getFieldVal(a, "tableName").asInstanceOf[TableIdentifier],
          inputObjects,
          columns = toCSColumns(Seq(getFieldVal(a, "newColumn").asInstanceOf[StructField])),
          actionType = TableOperationType.ALTER
        )

      case _ =>
    }
  }

  /**
   * Add database level hive privilege objects to input or output list
   *
   * @param dbName
   *   database name as hive privilege object
   * @param sparkHiveObjects
   *   input or output list
   */
  private[this] def addDbLevelObjs(
      dbName: String,
      sparkHiveObjects: JList[HPO],
      actionType: TableOperationType = TableOperationType.ACCESS
  ): Unit = {
    sparkHiveObjects.add(HPOBuilder(HivePrivilegeObjectType.DATABASE, dbName, dbName, actionType))
  }

  /**
   * Add table level hive objects to input or output list
   *
   * @param tableIdentifier
   *   table identifier contains database name, and table name as hive privilege object
   * @param sparkHiveObjects
   *   input or output list
   * @param actionType
   *   OperationType
   */
  private def addTableOrViewLevelObjs(
      tableIdentifier: TableIdentifier,
      sparkHiveObjects: JList[HPO],
      partKeys: Seq[String] = Nil,
      columns: JList[CSColumn] = null,
      actionType: TableOperationType = TableOperationType.ACCESS,
      cmdParams: Seq[String] = Nil
  ): Unit = {
    tableIdentifier.database match {
      case Some(db) =>
        val tbName = tableIdentifier.table
        sparkHiveObjects.add(
          HPOBuilder(
            HivePrivilegeObjectType.TABLE_OR_VIEW,
            db,
            tbName,
            partKeys.asJava,
            columns,
            actionType,
            cmdParams.asJava
          )
        )
      case _ =>
    }
  }

  /**
   * Add function level hive privilege objects to input or output list
   *
   * @param databaseName
   *   database name
   * @param functionName
   *   function name as hive privilege object
   * @param sparkHiveObjects
   *   input or output list
   */
  private def addFunctionLevelObjs(
      databaseName: Option[String],
      functionName: String,
      sparkHiveObjects: JList[HPO],
      actionType: TableOperationType = TableOperationType.ACCESS
  ): Unit = {
    databaseName match {
      case Some(db) =>
        sparkHiveObjects.add(
          HPOBuilder(HivePrivilegeObjectType.FUNCTION, db, functionName, actionType)
        )
      case _ =>
    }
  }

}
