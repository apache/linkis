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

package org.apache.linkis.common.io.resultset

import org.apache.linkis.common.io.{FsPath, MetaData, Record}

trait ResultSet[K <: MetaData, V <: Record] {
  val charset: String

  /**
   * Result set type(结果集类型)
   * @return
   */
  def resultSetType(): String

  /**
   * Give a file path via an Fs path and file name<br> The file suffix name is determined by the
   * uniform specification of the method. effect: <ul> <li>[ResultSetWriter]] and
   * [[ResultSetReader]] normalize the generated file name by this method</li> <li>Assist the
   * [[belongToPath]] method to determine whether a path is a resultSet path</li>
   *
   * </ul> 通过一个Fs路径和文件名，给出一个文件路径<br> 文件后缀名由该该方法统一规范决定 作用： <ul>
   * <li>[[ResultSetWriter]]和[[ResultSetReader]]通过该方法规范化生成文件名</li>
   * <li>协助[[belongToPath]]方法判断，某个路径是不是resultSet路径</li> </ul>
   * @param parentDir
   *   The parent path of an underlying storage system(某个底层存储系统的父路径)
   * @param fileName
   *   File name, without suffix(文件名，不包含后缀)
   * @return
   */
  def getResultSetPath(parentDir: FsPath, fileName: String): FsPath

  /**
   * Generates a file header for a resultSet that identifies a string as a resultSet of that type.
   * 作用： <ul> <li>[[ResultSetWriter]]和[[ResultSetReader]]通过该方法规范化生成文件名</li>
   * <li>协助[[belongToPath]]方法判断，某个路径是不是resultSet路径</li> </ul>
   *
   * 生成某个resultSet的文件头，用于标识某段string为该类型的resultSet. 作用： <ul>
   * <li>[[ResultSetWriter]]和[[ResultSetReader]]通过该方法规范化写入和读取文件头</li>
   * <li>协助[[belongToResultSet]]方法判断，某个string是不是resultSet</li> </ul>
   * @return
   */
  def getResultSetHeader: Array[Byte]
  def belongToPath(path: String): Boolean
  def belongToResultSet(content: String): Boolean
  def createResultSetSerializer(): ResultSerializer
  def createResultSetDeserializer(): ResultDeserializer[K, V]
}

trait ResultSetFactory {
  def getResultSetByType(resultSetType: String): ResultSet[_ <: MetaData, _ <: Record]
  def getResultSetByPath(fsPath: FsPath): ResultSet[_ <: MetaData, _ <: Record]
  def getResultSetByContent(content: String): ResultSet[_ <: MetaData, _ <: Record]

  def exists(resultSetType: String): Boolean
  def isResultSetPath(path: String): Boolean
  def isResultSet(content: String): Boolean
}
