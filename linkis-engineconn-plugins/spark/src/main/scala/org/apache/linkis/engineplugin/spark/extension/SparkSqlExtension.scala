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

package org.apache.linkis.engineplugin.spark.extension

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{DataFrame, SQLContext}

import java.util.concurrent._

import scala.concurrent.duration.Duration

trait SparkSqlExtension extends Logging {

  private val maxPoolSize = CommonVars("wds.linkis.park.extension.max.pool", 2).getValue

  private val executor = new ThreadPoolExecutor(
    2,
    maxPoolSize,
    2,
    TimeUnit.SECONDS,
    new LinkedBlockingQueue[Runnable](),
    new ThreadFactory {

      override def newThread(r: Runnable): Thread = {
        val thread = new Thread(r)
        thread.setDaemon(true)
        thread
      }

    }
  )

  def afterExecutingSQL(
      sqlContext: SQLContext,
      command: String,
      dataFrame: DataFrame,
      timeout: Long,
      sqlStartTime: Long
  ): Unit = {
    Utils.tryCatch {
      val thread = new Runnable {
        override def run(): Unit = extensionRule(sqlContext, command, dataFrame, sqlStartTime)
      }
      val future = executor.submit(thread)
      val duration = Duration(timeout, TimeUnit.MILLISECONDS)
      Utils.waitUntil(future.isDone, duration)
    } { case e: Throwable =>
      logger.info("Failed to execute SparkSqlExtension:", e)
    }
  }

  protected def extensionRule(
      sqlContext: SQLContext,
      command: String,
      dataFrame: DataFrame,
      sqlStartTime: Long
  ): Unit

}

object SparkSqlExtension extends Logging {

  val SQL_EXTENSION_CLAZZ = CommonVars(
    "wds.linkis.spark.extension.clazz",
    "org.apache.linkis.engineplugin.spark.lineage.LineageSparkSqlExtension"
  )

  private val extensions = initSparkSqlExtensions

  private def initSparkSqlExtensions: Array[SparkSqlExtension] = {

    val hooks = SQL_EXTENSION_CLAZZ.getValue
    if (StringUtils.isNotBlank(hooks)) {
      val clazzArr = hooks.split(",")
      if (null != clazzArr && clazzArr.nonEmpty) {
        clazzArr
          .map { clazz =>
            Utils.tryAndWarn(Utils.getClassInstance[SparkSqlExtension](clazz))
          }
          .filter(_ != null)
      } else {
        Array.empty
      }
    } else {
      Array.empty
    }

  }

  def getSparkSqlExtensions(): Array[SparkSqlExtension] = {
    logger.info(s"getSparkSqlExtensions classLoader ${this.getClass.getClassLoader}")
    extensions
  }

}
