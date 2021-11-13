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

import java.io.File
import java.util

import org.apache.linkis.common.utils.Logging


trait ProcessEngineCommandExec {

  def execute(): Unit

  def getProcess: Process

}

class ShellProcessEngineCommandExec(command: Array[String], baseDir: String,
                                    environment: util.Map[String, String], timeout: Long) extends
  ProcessEngineCommandExec with Logging {

  private var process: Process = _

  def this(command: Array[String], baseDir: String) = this(command, baseDir, null, 0)

  def this(command: Array[String]) = this(command, null)

  override def execute(): Unit = {

    info(s"Invoke subProcess, base dir ${this.baseDir} cmd is: ${command.mkString(" ")}")
    val builder = new ProcessBuilder(command: _*)
    if (environment != null) builder.environment.putAll(this.environment)
    if (baseDir != null) builder.directory(new File(this.baseDir))
    builder.redirectErrorStream(true)
    process = builder.start
  }

  override def getProcess: Process = process
}