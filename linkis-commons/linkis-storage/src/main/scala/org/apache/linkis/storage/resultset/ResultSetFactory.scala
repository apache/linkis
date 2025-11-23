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

package org.apache.linkis.storage.resultset

import org.apache.linkis.common.io.{Fs, FsPath, MetaData, Record}
import org.apache.linkis.common.io.resultset.ResultSet

import scala.collection.mutable

trait ResultSetFactory extends scala.AnyRef {

  def getResultSetByType(resultSetType: scala.Predef.String): ResultSet[_ <: MetaData, _ <: Record]

  def getResultSetByPath(fsPath: FsPath): ResultSet[_ <: MetaData, _ <: Record]
  def getResultSetByPath(fsPath: FsPath, fs: Fs): ResultSet[_ <: MetaData, _ <: Record]
  def getResultSetByContent(content: scala.Predef.String): ResultSet[_ <: MetaData, _ <: Record]
  def exists(resultSetType: scala.Predef.String): scala.Boolean
  def isResultSetPath(path: scala.Predef.String): scala.Boolean
  def isResultSet(content: scala.Predef.String): scala.Boolean
  def getResultSet(output: String): ResultSet[_ <: MetaData, _ <: Record]

  def getResultSetByPath(fsPath: FsPath, proxyUser: String): ResultSet[_ <: MetaData, _ <: Record]

  def getResultSet(output: String, proxyUser: String): ResultSet[_ <: MetaData, _ <: Record]

  /**
   * The first must-time text(第一个必须时text)
   * @return
   */
  def getResultSetType: Array[String]
}

object ResultSetFactory {

  val TEXT_TYPE = "1"
  val TABLE_TYPE = "2"
  val IO_TYPE = "3"
  val PICTURE_TYPE = "4"
  val HTML_TYPE = "5"

  /**
   * TODO 修改为注册形式，并修改ResultSet的getResultType逻辑 Result set corresponding type record(结果集对应类型记录)
   */
  val resultSetType = mutable.LinkedHashMap[String, String](
    TEXT_TYPE -> "TEXT",
    TABLE_TYPE -> "TABLE",
    IO_TYPE -> "IO",
    PICTURE_TYPE -> "PICTURE",
    HTML_TYPE -> "HTML"
  )

  val factory = new DefaultResultSetFactory
  def getInstance: ResultSetFactory = factory
}
