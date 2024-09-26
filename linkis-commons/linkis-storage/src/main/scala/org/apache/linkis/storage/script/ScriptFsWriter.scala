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

package org.apache.linkis.storage.script

import org.apache.linkis.common.io.{FsPath, FsWriter, MetaData}
import org.apache.linkis.storage.LineRecord
import org.apache.linkis.storage.script.compaction.{
  PYScriptCompaction,
  QLScriptCompaction,
  ScalaScriptCompaction,
  ShellScriptCompaction
}
import org.apache.linkis.storage.script.parser.{
  PYScriptParser,
  QLScriptParser,
  ScalaScriptParser,
  ShellScriptParser
}
import org.apache.linkis.storage.script.writer.StorageScriptFsWriter

import java.io.{InputStream, OutputStream}

abstract class ScriptFsWriter extends FsWriter {

  val path: FsPath
  val charset: String

  def getInputStream(): InputStream

}

object ScriptFsWriter {

  def getScriptFsWriter(
      path: FsPath,
      charset: String,
      outputStream: OutputStream = null
  ): ScriptFsWriter =
    new StorageScriptFsWriter(path, charset, outputStream)

}

object ParserFactory {

  def listParsers(): Array[Parser] =
    Array(PYScriptParser(), QLScriptParser(), ScalaScriptParser(), ShellScriptParser())

}

object Compaction {

  def listCompactions(): Array[Compaction] = Array(
    PYScriptCompaction(),
    QLScriptCompaction(),
    ScalaScriptCompaction(),
    ShellScriptCompaction()
  )

}

trait Parser {
  def prefixConf: String

  def prefix: String

  def belongTo(suffix: String): Boolean

  def parse(line: String): Variable

  def getAnnotationSymbol(): String
}

trait Compaction {

  def prefixConf: String

  def prefix: String

  def belongTo(suffix: String): Boolean

  def compact(variable: Variable): String

  def getAnnotationSymbol(): String
}

class ScriptMetaData(var variables: Array[Variable]) extends MetaData {
  override def cloneMeta(): MetaData = new ScriptMetaData(variables)

  def getMetaData: Array[Variable] = variables

  def setMetaData(variables: Array[Variable]): Unit = {
    this.variables = variables
  }

}

class ScriptRecord(line: String) extends LineRecord(line)

// definition  variable; specialConfiguration ;runConfiguration; startUpConfiguration;
case class Variable(sortParent: String, sort: String, key: String, value: String)
