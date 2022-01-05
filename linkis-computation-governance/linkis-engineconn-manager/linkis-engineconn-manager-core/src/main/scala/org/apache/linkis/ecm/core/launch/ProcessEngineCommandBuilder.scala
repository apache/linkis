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
 
package org.apache.linkis.ecm.core.launch

import java.io.OutputStream

import org.apache.linkis.manager.engineplugin.common.launch.process.LaunchConstants
import org.apache.commons.io.IOUtils


trait ProcessEngineCommandBuilder {

  def setCommand(command: Array[String]): Unit

  def setEnv(key: String, value: String): Unit

  def newLine(command: Array[String])

  def link(fromPath: String, toPath: String)

  def mkdir(dir: String): Unit

  def writeTo(output: OutputStream): Unit

  def replaceExpansionMarker(value: String): String

}

abstract class ShellProcessEngineCommandBuilder extends ProcessEngineCommandBuilder {

  private val LINE_SEPARATOR = System.getProperty("line.separator")
  private val sb = new StringBuilder

  def newLine(command: String): Unit = newLine(Array(command))

  override def newLine(command: Array[String]): Unit = {
    command.foreach(sb ++= _)
    sb ++= LINE_SEPARATOR
  }

  def writeTo(output: OutputStream): Unit = {
    IOUtils.write(sb, output)
  }

  override def replaceExpansionMarker(value: String): String = value.replaceAll(LaunchConstants.EXPANSION_MARKER_LEFT, "\\${")
    .replaceAll(LaunchConstants.EXPANSION_MARKER_RIGHT, "}")
}

class UnixProcessEngineCommandBuilder extends ShellProcessEngineCommandBuilder {

  newLine("#!/bin/bash")

  private def addErrorCheck(): Unit = {
    newLine("linkis_engineconn_errorcode=$?")
    newLine("if [ $linkis_engineconn_errorcode -ne 0 ]")
    newLine("then")
    newLine("  cat ${LOG_DIRS}/stderr")
    newLine("  exit $linkis_engineconn_errorcode")
    newLine("fi")
  }

  override def setCommand(command: Array[String]): Unit = {
    newLine(Array(command.mkString(" ")))
    addErrorCheck()
  }

  override def setEnv(key: String, value: String): Unit = newLine(Array("export ", key, "=\"", value, "\""))

  override def link(fromPath: String, toPath: String): Unit = {
    newLine(Array("ln -sf \"", fromPath, "\" \"", toPath, "\""))
    addErrorCheck()
  }

  override def mkdir(dir: String): Unit = {
    newLine(Array("mkdir -p ", dir))
    addErrorCheck()
  }
}