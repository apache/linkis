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

package org.apache.linkis.engineplugin.spark.datacalc

import org.apache.spark.sql.{Dataset, Row, SQLContext}

import scala.collection.mutable

object DataCalcTempData {

  private val RESULT_TABLES: mutable.Set[String] = mutable.Set[String]()
  private val PERSIST_DATASETS: mutable.Set[Dataset[Row]] = mutable.Set[Dataset[Row]]()

  def putResultTable(resultTableName: String): Unit = {
    RESULT_TABLES.add(resultTableName)
  }

  def putPersistDataSet(ds: Dataset[Row]): Unit = {
    PERSIST_DATASETS.add(ds)
  }

  /**
   * clean temporary data
   * @param sqlContext
   */
  def clean(sqlContext: SQLContext): Unit = {
    RESULT_TABLES.foreach(resultTable => sqlContext.dropTempTable(resultTable))
    RESULT_TABLES.clear()

    PERSIST_DATASETS.foreach(ds => ds.unpersist())
    PERSIST_DATASETS.clear()
  }

}
