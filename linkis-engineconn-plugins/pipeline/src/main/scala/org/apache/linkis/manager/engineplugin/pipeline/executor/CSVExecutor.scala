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

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.manager.engineplugin.pipeline.conf.PipelineEngineConfiguration.{
  PIPELINE_FIELD_QUOTE_RETOUCH_ENABLE,
  PIPELINE_FIELD_SPLIT_STR,
  PIPELINE_OUTPUT_CHARSET_STR,
  PIPELINE_OUTPUT_ISOVERWRITE_SWITCH
}
import org.apache.linkis.manager.engineplugin.pipeline.constant.PipeLineConstant._
import org.apache.linkis.manager.engineplugin.pipeline.errorcode.PopelineErrorCodeSummary._
import org.apache.linkis.manager.engineplugin.pipeline.exception.PipeLineErrorException
import org.apache.linkis.scheduler.executer.ExecuteResponse
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.csv.CSVFsWriter
import org.apache.linkis.storage.source.FileSource
import org.apache.linkis.storage.utils.StorageConfiguration.STORAGE_RS_FILE_SUFFIX

import org.apache.commons.io.IOUtils

import java.io.OutputStream

class CSVExecutor extends PipeLineExecutor {

  override def execute(
      sourcePath: String,
      destPath: String,
      engineExecutionContext: EngineExecutionContext
  ): ExecuteResponse = {
    if (!sourcePath.contains(STORAGE_RS_FILE_SUFFIX.getValue)) {
      throw new PipeLineErrorException(
        EXPROTING_MULTIPLE.getErrorCode,
        EXPROTING_MULTIPLE.getErrorDesc
      )
    }
    if (!FileSource.isResultSet(sourcePath)) {
      throw new PipeLineErrorException(
        NOT_A_RESULT_SET_FILE.getErrorCode,
        NOT_A_RESULT_SET_FILE.getErrorDesc
      )
    }
    val sourceFsPath = new FsPath(sourcePath)
    val destFsPath = new FsPath(destPath)
    val sourceFs = FSFactory.getFs(sourceFsPath)
    sourceFs.init(null)
    val destFs = FSFactory.getFs(destFsPath)
    destFs.init(null)
    val fileSource = FileSource.create(sourceFsPath, sourceFs)
    if (!FileSource.isTableResultSet(fileSource)) {
      throw new PipeLineErrorException(
        ONLY_RESULT_CONVERTED_TO_CSV.getErrorCode,
        ONLY_RESULT_CONVERTED_TO_CSV.getErrorDesc
      )
    }
    var nullValue = options.getOrDefault(PIPELINE_OUTPUT_SHUFFLE_NULL_TYPE, "NULL")
    if (BLANK.equalsIgnoreCase(nullValue)) nullValue = ""
    val outputStream: OutputStream =
      destFs.write(destFsPath, PIPELINE_OUTPUT_ISOVERWRITE_SWITCH.getValue(options))
    OutputStreamCache.osCache.put(engineExecutionContext.getJobId.get, outputStream)
    val cSVFsWriter = CSVFsWriter.getCSVFSWriter(
      PIPELINE_OUTPUT_CHARSET_STR.getValue(options),
      PIPELINE_FIELD_SPLIT_STR.getValue(options),
      PIPELINE_FIELD_QUOTE_RETOUCH_ENABLE.getValue(options),
      outputStream
    )
    fileSource.addParams("nullValue", nullValue).write(cSVFsWriter)
    IOUtils.closeQuietly(cSVFsWriter)
    IOUtils.closeQuietly(fileSource)
    IOUtils.closeQuietly(sourceFs)
    IOUtils.closeQuietly(destFs)
    super.execute(sourcePath, destPath, engineExecutionContext)
  }

  override def Kind: String = "csv"

}

object CSVExecutor {
  val csvExecutor = new CSVExecutor

  def getInstance: PipeLineExecutor = csvExecutor
}
