/*
 *
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.engineplugin.spark.metadata

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.catalyst.analysis.UnresolvedRelation
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.execution.command.CreateTableLikeCommand
import org.apache.spark.sql.execution.datasources.{CreateTable, LogicalRelation}
import org.apache.spark.sql.hive.execution.{CreateHiveTableAsSelectCommand, InsertIntoHiveTable}

/**
 *
 * Description:
 */
object LineageResolveLogic extends Logging{



  def resolveLogicPlan(plan: LogicalPlan, currentDB:String): (util.Set[TableInfo], util.Set[TableInfo]) ={
    val inputTables = new util.HashSet[TableInfo]()
    val outputTables = new util.HashSet[TableInfo]()
    resolveLogic(plan, currentDB, inputTables, outputTables)
    Tuple2(inputTables, outputTables)
  }

  def resolveLogic(plan: LogicalPlan, currentDB:String, inputTables:util.Set[TableInfo], outputTables:util.Set[TableInfo]): Unit ={
    plan match {

      case plan: Project =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: Union =>
        for(child <- plan.children){
          resolveLogic(child, currentDB, inputTables, outputTables)
        }

      case plan: Join =>
        resolveLogic(plan.left, currentDB, inputTables, outputTables)
        resolveLogic(plan.right, currentDB, inputTables, outputTables)

      case plan: Aggregate =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: Filter =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: Generate =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: RepartitionByExpression =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: SerializeFromObject =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: MapPartitions =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: DeserializeToObject =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: Repartition =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: Deduplicate =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: Window =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: MapElements =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: TypedFilter =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: Distinct =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: GlobalLimit =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: LocalLimit =>
        resolveLogic(plan.child, currentDB, inputTables, outputTables)

      case plan: SubqueryAlias =>
        val childInputTables = new util.HashSet[TableInfo]()
        val childOutputTables = new util.HashSet[TableInfo]()

        resolveLogic(plan.child, currentDB, childInputTables, childOutputTables)
        if(childInputTables.size()>0){
          inputTables.addAll(childInputTables)
        }else{
          inputTables.add(TableInfo(currentDB, plan.alias))
        }

      case plan: UnresolvedRelation =>
        val tableInfo = TableInfo(plan.tableIdentifier.database.getOrElse(currentDB), plan.tableIdentifier.table)
        inputTables.add(tableInfo)

      case plan: InsertIntoTable =>
        resolveLogic(plan.table, currentDB, outputTables, inputTables)
        resolveLogic(plan.query, currentDB, inputTables, outputTables)

      case plan: CreateTable =>
        if(plan.query.isDefined){
          resolveLogic(plan.query.get, currentDB, inputTables, outputTables)
        }
        val tableIdentifier = plan.tableDesc.identifier
        val tableInfo = TableInfo(tableIdentifier.database.getOrElse(currentDB), tableIdentifier.table)
        outputTables.add(tableInfo)

      case plan:CreateTableLikeCommand =>
        val outputTable = TableInfo(plan.targetTable.database.getOrElse(currentDB),plan.targetTable.table)
        val inputTable = TableInfo(plan.sourceTable.database.getOrElse(currentDB),plan.sourceTable.table)
        outputTables.add(outputTable)
        inputTables.add(inputTable)

      /**
       * Spark 2.4.3
       */
      case plan:CreateHiveTableAsSelectCommand =>
        if(plan.query != null){
          resolveLogic(plan.query, currentDB, inputTables, outputTables)
        }
        val tableIdentifier = plan.tableDesc.identifier
        val tableInfo = TableInfo(tableIdentifier.database.getOrElse(currentDB), tableIdentifier.table)
        outputTables.add(tableInfo)

      case plan:InsertIntoHiveTable =>
        if(plan.query != null){
          resolveLogic(plan.query, currentDB, inputTables, outputTables)
        }
        val tableIdentifier = plan.table.identifier
        val tableInfo = TableInfo(tableIdentifier.database.getOrElse(currentDB), tableIdentifier.table)
        outputTables.add(tableInfo)
      /* spark2.1.0
      case plan: CatalogRelation =>
     val identifier = plan.tableMeta.identifier
     val tableInfo = TableInfo(identifier.database.getOrElse(currentDB), identifier.table)
     inputTables.add(tableInfo)*/

      case plan: LogicalRelation =>
        val identifier = plan.catalogTable.get
        info(s"one identifier is ${identifier.database}")
        info(s"two identifier is ${identifier.identifier.database.get}")
        val inputTable = TableInfo(identifier.database,identifier.identifier.table)
        inputTables.add(inputTable)

      case `plan` => /*logger.info("******child plan******:\n"+plan)*/
    }
  }


}

case class TableInfo(db:String,table:String){
  override def toString: String = {
    if(StringUtils.isNotEmpty(db)){
      db + "." + table
    } else {
      table
    }
  }
}
