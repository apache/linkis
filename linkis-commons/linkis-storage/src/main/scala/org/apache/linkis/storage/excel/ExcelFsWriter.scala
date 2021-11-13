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
 
package org.apache.linkis.storage.excel

import java.io.OutputStream

import org.apache.linkis.common.io.FsWriter


abstract class ExcelFsWriter extends FsWriter {
  val charset: String
  val sheetName: String
  val dateFormat: String
}

object ExcelFsWriter {
  def getExcelFsWriter(charset: String, sheetName: String, dateFormat: String, outputStream: OutputStream): ExcelFsWriter = new StorageExcelWriter(charset, sheetName, dateFormat, outputStream: OutputStream)
}

