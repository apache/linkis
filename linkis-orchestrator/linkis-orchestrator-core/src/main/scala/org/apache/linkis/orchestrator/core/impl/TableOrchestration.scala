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

package org.apache.linkis.orchestrator.core.impl

import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.orchestrator.Orchestration
import org.apache.linkis.orchestrator.core.{CacheStrategy, GlobalState}

/**
 */
trait TableOrchestration extends Orchestration {

  def createAsTempView(viewName: String, cacheStrategy: CacheStrategy): Unit

  def createAsTempView(viewName: String): Unit =
    createAsTempView(viewName, CacheStrategy.ONLY_SESSION_AND_CS_TERM_CACHE)

  def collectAsResultSetIterator: ResultSetIterator

  def collectAsResultSetIterator(maxRows: Int): ResultSetIterator

}

trait TableGlobalState extends GlobalState {

  val globalTempViewManager: TempViewManager

  // TODO val linkisDataViewCatalogPlugin: LinkisDataViewCatalogPlugin

}

import java.util

trait ResultSetIterator extends util.Iterator[Record] {

  def init(): Unit

  def getMetadata: MetaData

}

trait TempViewManager {

  def apply(viewName: String): TableOrchestration

  def getTempView(viewName: String): Option[TableOrchestration]

  def createTempView(orchestration: TableOrchestration, viewName: String, cover: Boolean): Unit

  def updateTempView(orchestration: TableOrchestration, viewName: String): Unit =
    createTempView(orchestration, viewName, true)

  def deleteTempView(viewName: String): Unit

  def deleteTempViews(func: String => Boolean): Unit

  def renameTempView(fromViewName: String, toViewName: String): Unit

  def listTempViews(): Array[String]

  def listTempViews(func: String => Boolean): Array[String]

  def listTempViews(func: (String, TableOrchestration) => Boolean): Array[String]

  def deleteAll(): Unit

}
