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
package com.webank.wedatasphere.linkis.manager.engineplugin.pipeline.executor

import java.io.File
import java.util

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.engineplugin.pipeline.exception.PipeLineErrorException

/**
  */
object PipelineExecutorSelector extends Logging {

  @throws[PipeLineErrorException]
  def select(sourcePath: String, destPath: String, options: util.Map[String, String]): PipeLineExecutor = {
    PipelineEngineConnExecutor.listPipelineExecutors.foreach(_.init(options))
    Utils.tryCatch {
      if (new File(sourcePath).getName.equals(new File(destPath).getName)) return PipelineEngineConnExecutor.listPipelineExecutors()(0)
      getSuffix(destPath) match {
        case ".csv" => PipelineEngineConnExecutor.listPipelineExecutors()(1)
        case ".xlsx" => PipelineEngineConnExecutor.listPipelineExecutors()(2)
        case _ => throw new PipeLineErrorException(70008, "unsupport output type")
      }
    } {
      case e: Exception => error("select executor failed", e); throw new PipeLineErrorException(70008, "unsupport output type")
    }

  }


  def getSuffix(str: String): String = str.substring(str.lastIndexOf("."))

}
