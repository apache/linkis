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

package org.apache.linkis.manager.engineplugin.pipeline.executor

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.engineplugin.pipeline.errorcode.PopelineErrorCodeSummary._
import org.apache.linkis.manager.engineplugin.pipeline.exception.PipeLineErrorException

import java.io.File
import java.util

/**
 */
object PipelineExecutorSelector extends Logging {

  @throws[PipeLineErrorException]
  def select(
      sourcePath: String,
      destPath: String,
      options: util.Map[String, String]
  ): PipeLineExecutor = {
    PipelineEngineConnExecutor.listPipelineExecutors.foreach(_.init(options))
    Utils.tryCatch {
      if (new File(sourcePath).getName.equals(new File(destPath).getName)) {
        return PipelineEngineConnExecutor.listPipelineExecutors()(0)
      }
      getSuffix(destPath) match {
        case ".csv" => PipelineEngineConnExecutor.listPipelineExecutors()(1)
        case ".xlsx" => PipelineEngineConnExecutor.listPipelineExecutors()(2)
        case _ =>
          throw new PipeLineErrorException(
            UNSUPPORT_OUTPUT_TYPE.getErrorCode,
            UNSUPPORT_OUTPUT_TYPE.getErrorDesc
          )
      }
    } { case e: Exception =>
      logger.error("select executor failed", e);
      throw new PipeLineErrorException(
        UNSUPPORT_OUTPUT_TYPE.getErrorCode,
        UNSUPPORT_OUTPUT_TYPE.getErrorDesc
      )
    }

  }

  def getSuffix(str: String): String = str.substring(str.lastIndexOf("."))

}
