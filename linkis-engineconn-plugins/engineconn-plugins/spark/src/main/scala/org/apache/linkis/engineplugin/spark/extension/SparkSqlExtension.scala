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
 
package org.apache.linkis.engineplugin.spark.extension

import java.util.concurrent._

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.spark.sql.execution.QueryExecution
import org.apache.spark.sql.{DataFrame, SQLContext}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
/**
  *
  */
abstract class SparkSqlExtension extends Logging{

  private val maxPoolSize = CommonVars("wds.linkis.dws.ujes.spark.extension.max.pool",5).getValue

  private  val executor = new ThreadPoolExecutor(2, maxPoolSize, 2, TimeUnit.SECONDS, new LinkedBlockingQueue[Runnable](), new ThreadFactory {
    override def newThread(r: Runnable): Thread = {
      val thread = new Thread(r)
      thread.setDaemon(true)
      thread
    }
  })

  final def afterExecutingSQL(sqlContext: SQLContext,command: String,dataFrame: DataFrame,timeout:Long,sqlStartTime:Long):Unit = {
    try {
      val thread = new Runnable {
        override def run(): Unit = extensionRule(sqlContext,command,dataFrame.queryExecution,sqlStartTime)
      }
      val future = executor.submit(thread)
      val duration = Duration(timeout, TimeUnit.MILLISECONDS)
      Utils.waitUntil(future.isDone, duration)
    } catch {
      case e: Throwable => info("Failed to execute SparkSqlExtension: ", e)
    }
  }

  protected def extensionRule(sqlContext: SQLContext,command: String,queryExecution: QueryExecution,sqlStartTime:Long):Unit


}

object SparkSqlExtension extends Logging {

  private val extensions = ArrayBuffer[SparkSqlExtension]()

  def register(sqlExtension: SparkSqlExtension):Unit = {
    info("Get a sqlExtension register")
    extensions.append(sqlExtension)
  }

  def getSparkSqlExtensions():Array[SparkSqlExtension] = {
    extensions.toArray
  }
}
