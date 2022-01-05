/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.common.utils

import java.io.{InputStream, OutputStream}


@Deprecated
trait FileService {

  def getLength(path: String, user: String): Long

  def open(path: String, user: String): InputStream

  def append(path: String, user: String, overwrite: Boolean): OutputStream

  def readFile(path: String, user: String): String

  def mkdirs(path: String, user: String, deleteWhenExists: Boolean): Unit

  def createFile(path: String, user: String, deleteWhenExists: Boolean): Unit

  def deleteFile(path: String, user: String): Unit

  def deleteDir(path: String, user: String): Unit

  def writeFile(path: String, content: String, user: String, overwrite: Boolean): Unit

  def copyFile(fromFile: String, toFile: String, user: String): Unit

  def renameFile(fromFile: String, toFile: String, user: String): Unit

  /**
    * Return only the file name(只返回文件名)
    * @param path
    * @param user
    * @return
    */
  def listFileNames(path: String, user: String): Array[String]

  def exists(path: String, user: String): Boolean

  def isDir(path: String, user: String): Boolean = !isFile(path, user)

  def isFile(path: String, user: String): Boolean

}
