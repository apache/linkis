/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineplugin.spark.executor

import java.lang.reflect.InvocationTargetException
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.engineplugin.spark.common.{Kind, SparkSQL}
import com.webank.wedatasphere.linkis.engineplugin.spark.config.SparkConfiguration
import com.webank.wedatasphere.linkis.engineplugin.spark.entity.SparkEngineSession
import com.webank.wedatasphere.linkis.engineplugin.spark.extension.SparkSqlExtension
import com.webank.wedatasphere.linkis.engineplugin.spark.utils.EngineUtils
import com.webank.wedatasphere.linkis.scheduler.executer.{CompletedExecuteResponse, ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.governance.common.paser.SQLCodeParser
import org.apache.commons.lang.exception.ExceptionUtils


class SparkSqlExecutor(sparkEngineSession: SparkEngineSession, id: Long) extends SparkEngineConnExecutor(sparkEngineSession.sparkContext, id) {


  override def init(): Unit = {

    setCodeParser(new SQLCodeParser)
    super.init()
    info("spark sql executor start")
  }

  override protected def getKind: Kind = SparkSQL()

  override protected def runCode(executor: SparkEngineConnExecutor, code: String, engineExecutionContext: EngineExecutionContext, jobGroup: String): ExecuteResponse = {

    info("SQLExecutor run query: " + code)
    engineExecutionContext.appendStdout(s"${EngineUtils.getName} >> $code")
    Utils.tryCatch{
      val sqlStartTime = System.currentTimeMillis()
      val df = sparkEngineSession.sqlContext.sql(code)

      Utils.tryQuietly(SparkSqlExtension.getSparkSqlExtensions().foreach(_.afterExecutingSQL(sparkEngineSession.sqlContext, code, df,
        SparkConfiguration.SQL_EXTENSION_TIMEOUT.getValue, sqlStartTime)))
      SQLSession.showDF(sparkEngineSession.sparkContext, jobGroup, df, null, SparkConfiguration.SHOW_DF_MAX_RES.getValue, engineExecutionContext)
      SuccessExecuteResponse().asInstanceOf[CompletedExecuteResponse]
    }{
      case e: InvocationTargetException =>
        var cause = ExceptionUtils.getCause(e)
        if (cause == null) cause = e
        //error("execute sparkSQL failed!", cause)
        ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(e), cause)
      case ite: Exception =>
        // error("execute sparkSQL failed!", ite)
        ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(ite), ite)
    }
  }

  override protected def getExecutorIdPreFix: String = "SparkSqlExecutor_"
}
