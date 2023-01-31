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

package org.apache.linkis.engineconnplugin.flink.executor.interceptor

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.once.executor.OnceExecutorExecutionContext
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration.{
  FLINK_APPLICATION_MAIN_CLASS_JAR,
  FLINK_SHIP_DIRECTORIES
}
import org.apache.linkis.engineconnplugin.flink.resource.FlinkJobResourceCleaner

import org.apache.commons.lang3.StringUtils

import java.util

/**
 * Flink Job resource clean interceptor
 */
class FlinkJobResCleanInterceptor(cleaner: FlinkJobResourceCleaner)
    extends FlinkJobSubmitInterceptor
    with Logging {

  /**
   * Before submit the job
   *
   * @param onceExecutorExecutionContext
   *   execution context
   */
  override def beforeSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext): Unit = {
    // Do nothing

  }

  /**
   * After success to submit the job
   *
   * @param onceExecutorExecutionContext
   *   execution context
   */
  override def afterSubmitSuccess(
      onceExecutorExecutionContext: OnceExecutorExecutionContext
  ): Unit = {
    logger.info("Clean the flink job resource after success to submit")
    cleanResources(onceExecutorExecutionContext.getEngineCreationContext.getOptions)
  }

  /**
   * After fail to submit the job
   *
   * @param onceExecutorExecutionContext
   *   execution context
   * @param throwable
   *   throwable
   */
  override def afterSubmitFail(
      onceExecutorExecutionContext: OnceExecutorExecutionContext,
      throwable: Throwable
  ): Unit = {
    logger.info("Clean the flink job resource after fail to submit")
    cleanResources(onceExecutorExecutionContext.getEngineCreationContext.getOptions)
  }

  private def cleanResources(options: util.Map[String, String]): Unit = {
    val mainClassJar = FLINK_APPLICATION_MAIN_CLASS_JAR.getValue(options)
    logger.trace(s"mainClassJar to clean: ${mainClassJar}")
    if (StringUtils.isNotBlank(mainClassJar) && cleaner.accept(mainClassJar)) {
      cleaner.cleanup(Array(mainClassJar))
    }
    val shipDirsArray = FLINK_SHIP_DIRECTORIES.getValue(options).split(",")
    logger.trace(s"Ship directories to clean: ${shipDirsArray.length}")
    shipDirsArray match {
      case resArray: Array[String] => cleaner.cleanup(resArray.filter(cleaner.accept))
      case _ =>
    }
  }

}
