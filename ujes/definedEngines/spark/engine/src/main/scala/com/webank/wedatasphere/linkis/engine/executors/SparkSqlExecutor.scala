/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engine.executors

import java.lang.reflect.InvocationTargetException
import java.util.concurrent.atomic.AtomicLong

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engine.configuration.SparkConfiguration
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.extension.SparkSqlExtension
import com.webank.wedatasphere.linkis.engine.spark.common.{Kind, SparkSQL}
import com.webank.wedatasphere.linkis.engine.spark.utils.EngineUtils
import com.webank.wedatasphere.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SQLContext}
/**
  * Created by allenlliu on 2019/4/8.
  */
class SparkSqlExecutor(val sc: SparkContext,val sqlContext: SQLContext) extends SparkExecutor with Logging{
  val queryNum = new AtomicLong(0)

  val SQL_EXTENSION_TIMEOUT = CommonVars("wds.linkis.dws.ujes.spark.extension.timeout",3000L)

  override def execute(sparkEngineExecutor: SparkEngineExecutor,code: String,engineExecutorContext: EngineExecutorContext,jobGroup:String): ExecuteResponse = {
//    val jobGroup = String.valueOf("dwc-sql-" + queryNum.incrementAndGet())
//    info("Set jobGroup to " + jobGroup)
//    sc.setJobGroup(jobGroup, code, true)
    var rdd: Any = null
    info("SQLExecutor run query: " + code)
    engineExecutorContext.appendStdout(s"${EngineUtils.getName} >> $code")
    try{
      val sqlStartTime = System.currentTimeMillis()
      val sqlMethod = sqlContext.getClass.getMethod("sql", classOf[String])
      rdd = sqlMethod.invoke(sqlContext, code)

      /**
        * update by allenlliu
        * add lineage hook
        */
      Utils.tryQuietly(SparkSqlExtension.getSparkSqlExtensions().foreach(_.afterExecutingSQL(sqlContext,code,rdd.asInstanceOf[DataFrame],SQL_EXTENSION_TIMEOUT.getValue,sqlStartTime)))
      /**
        * end
        */
      SQLSession.showDF(sc, jobGroup, rdd, null, SparkConfiguration.SHOW_DF_MAX_RES.getValue,engineExecutorContext)
      SuccessExecuteResponse()
    } catch {
      case e: InvocationTargetException =>
        var cause = ExceptionUtils.getCause(e)
        if(cause == null) cause = e
        error("execute sparkSQL failed!", cause)
        ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(e), cause)
      case ite: Exception =>
        error("execute sparkSQL failed!", ite)
        ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(ite), ite)
    } finally sc.clearJobGroup()
  }



  override def kind: Kind = SparkSQL()

  override def open: Unit = {}

  override def close: Unit = {}

}
