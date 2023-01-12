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

package org.apache.linkis.ujes.client.request

import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.request.POSTAction
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.ujes.client.exception.UJESClientBuilderException

import org.apache.commons.lang3.StringUtils

import java.util

class JobExecuteAction private () extends POSTAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("entrance", "execute")

  override def getRequestPayload: String =
    DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

}

object JobExecuteAction {
  def builder(): Builder = new Builder

  class Builder private[JobExecuteAction] () {
    private var user: String = _
    private var executeCode: String = _
    private var formatCode: Boolean = false
    private var creator: String = _
    private var engineType: EngineType = _
    private var runType: RunType = _

    private var engineTypeStr: String = _

    private var runTypeStr: String = _

    private var scriptPath: String = _
    private var params: util.Map[String, AnyRef] = _

    private var source: util.Map[String, AnyRef] = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def addExecuteCode(executeCode: String): Builder = {
      this.executeCode = executeCode
      this
    }

    def enableFormatCode(): Builder = {
      this.formatCode = true
      this
    }

    def setCreator(creator: String): Builder = {
      this.creator = creator
      this
    }

    def setEngineType(engineType: EngineType): Builder = {
      this.engineType = engineType
      this
    }

    def setRunType(runType: RunType): Builder = {
      this.runType = runType
      this
    }

    def setEngineTypeStr(engineTypeStr: String): Builder = {
      this.engineTypeStr = engineTypeStr
      this
    }

    def setRunTypeStr(runTypeStr: String): Builder = {
      this.runTypeStr = runTypeStr
      this
    }

    def setScriptPath(scriptPath: String): Builder = {
      this.scriptPath = scriptPath
      this
    }

    def setParams(params: util.Map[String, AnyRef]): Builder = {
      this.synchronized(this.params = params)
      this
    }

    def setSource(source: util.Map[String, AnyRef]): Builder = {
      this.synchronized(this.source = source)
      this
    }

    def setStartupParams(startupMap: util.Map[String, AnyRef]): Builder = {
      initParams
      TaskUtils.addStartupMap(this.params, startupMap)
      this
    }

    private def initParams(): Unit = {
      if (this.params == null) this synchronized {
        if (this.params == null) this.params = new util.HashMap[String, AnyRef]
      }
    }

    def setRuntimeParams(runtimeMap: util.Map[String, AnyRef]): Builder = {
      initParams
      TaskUtils.addRuntimeMap(this.params, runtimeMap)
      this
    }

    def setSpecialParams(specialMap: util.Map[String, AnyRef]): Builder = {
      initParams
      TaskUtils.addSpecialMap(this.params, specialMap)
      this
    }

    def setVariableMap(variableMap: util.Map[String, AnyRef]): Builder = {
      initParams
      TaskUtils.addVariableMap(this.params, variableMap)
      this
    }

    private def getEngineType: String = {
      if (engineType == null && engineTypeStr == null) {
        throw new UJESClientBuilderException("engineType is needed!")
      }
      if (engineType != null) return engineType.toString
      engineTypeStr
    }

    private def getRunType: String = {
      if (runType != null) return runType.toString
      if (runTypeStr != null) return runTypeStr
      if (engineType != null) return engineType.getDefaultRunType.toString
      else throw new UJESClientBuilderException("runType is needed!")
    }

    def build(): JobExecuteAction = {
      val executeAction = new JobExecuteAction
      executeAction.setUser(user)
      executeAction.addRequestPayload(TaskConstant.EXECUTEAPPLICATIONNAME, getEngineType)
      executeAction.addRequestPayload(TaskConstant.RUNTYPE, getRunType)
      if (formatCode) executeAction.addRequestPayload(TaskConstant.FORMATCODE, true)
      if (StringUtils.isBlank(creator)) throw new UJESClientBuilderException("creator is needed!")
      executeAction.addRequestPayload(TaskConstant.REQUESTAPPLICATIONNAME, creator)
      if (StringUtils.isEmpty(scriptPath) && StringUtils.isEmpty(executeCode) && params == null) {
        throw new UJESClientBuilderException("scriptPath or executeCode is needed!")
      }
      if (StringUtils.isEmpty(scriptPath) && StringUtils.isEmpty(executeCode)) {
        addExecuteCode(params.toString)
      }
      executeAction.addRequestPayload(TaskConstant.EXECUTIONCODE, executeCode)
      executeAction.addRequestPayload(TaskConstant.SCRIPTPATH, scriptPath)
      initParams
      executeAction.addRequestPayload(TaskConstant.PARAMS, params)
      if (this.source == null) this.source = new util.HashMap[String, AnyRef]()
      executeAction.addRequestPayload(TaskConstant.SOURCE, this.source)
      executeAction
    }

  }

  trait EngineType {
    def getDefaultRunType: RunType
  }

  trait RunType

  object EngineType {

    val SPARK = new EngineType {
      override val toString: String = "spark"

      val SQL = new RunType {
        override val toString: String = "sql"
      }

      val SCALA = new RunType {
        override val toString: String = "scala"
      }

      val PYSPARK = new RunType {
        override val toString: String = "pyspark"
      }

      val R = new RunType {
        override val toString: String = "r"
      }

      override def getDefaultRunType: RunType = SQL
    }

    val HIVE = new EngineType {
      override val toString: String = "hive"

      val HQL = new RunType {
        override val toString: String = "hql"
      }

      override def getDefaultRunType: RunType = HQL
    }

    val SHELL = new EngineType {
      override val toString: String = "shell"

      val SH = new RunType {
        override val toString: String = "shell"
      }

      override def getDefaultRunType: RunType = SH
    }

    val PYTHON = new EngineType {
      override val toString: String = "python"

      val PY = new RunType {
        override val toString: String = "python"
      }

      override def getDefaultRunType: RunType = PY
    }

    val JDBC = new EngineType {
      override val toString: String = "jdbc"

      val JDBC_RunType = new RunType {
        override val toString: String = "jdbc"
      }

      override def getDefaultRunType: RunType = JDBC_RunType
    }

    val PRESTO = new EngineType {
      override val toString: String = "presto"

      val PSQL = new RunType {
        override val toString: String = "psql"
      }

      override def getDefaultRunType: RunType = PSQL
    }

    val TRINO = new EngineType {
      override val toString: String = "trino"

      val TSQL = new RunType {
        override val toString: String = "tsql"
      }

      override def getDefaultRunType: RunType = TSQL
    }

  }

}
