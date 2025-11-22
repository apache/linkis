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
import org.apache.linkis.common.utils.{Logging, ResultSetUtils}
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.manager.engineplugin.pipeline.conf.PipelineEngineConfiguration
import org.apache.linkis.manager.engineplugin.pipeline.conf.PipelineEngineConfiguration.PIPELINE_OUTPUT_ISOVERWRITE_SWITCH
import org.apache.linkis.manager.engineplugin.pipeline.constant.PipeLineConstant
import org.apache.linkis.manager.engineplugin.pipeline.constant.PipeLineConstant._
import org.apache.linkis.manager.engineplugin.pipeline.errorcode.PopelineErrorCodeSummary._
import org.apache.linkis.manager.engineplugin.pipeline.exception.PipeLineErrorException
import org.apache.linkis.scheduler.executer.ExecuteResponse
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.conf.LinkisStorageConf
import org.apache.linkis.storage.conf.LinkisStorageConf.FIELD_TRUNCATION_ENABLED
import org.apache.linkis.storage.excel.{ExcelFsWriter, StorageExcelWriter, StorageMultiExcelWriter}
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.source.FileSource
import org.apache.linkis.storage.utils.ResultUtils

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import java.io.OutputStream
import java.util

import scala.collection.JavaConverters._

class ExcelExecutor extends PipeLineExecutor with Logging {

  override def execute(
      sourcePath: String,
      destPath: String,
      engineExecutorContext: EngineExecutionContext
  ): ExecuteResponse = {
    // Extract masked field names from options
    val maskedFieldNames = options.getOrDefault(PipeLineConstant.PIPELINE_MASKED_CONF, "")

    var fileSource: FileSource = null
    var excelFsWriter: ExcelFsWriter = null
    val sourceFsPath = new FsPath(sourcePath)
    val destFsPath = new FsPath(destPath)
    val sourceFs = FSFactory.getFs(sourceFsPath)
    sourceFs.init(null)
    val destFs = FSFactory.getFs(destFsPath)
    destFs.init(null)

    try {
      val outputStream: OutputStream =
        destFs.write(destFsPath, PIPELINE_OUTPUT_ISOVERWRITE_SWITCH.getValue(options))
      val paramsMap = new util.HashMap[String, String]()
      engineExecutorContext.getProperties.asScala
        .filter(_._2 != null)
        .map(kv => (kv._1, kv._2.toString))
        .foreach(kv => paramsMap.put(kv._1, kv._2))
      val excelAutoFormat =
        PipelineEngineConfiguration.EXPORT_EXCEL_AUTO_FORMAT.getValue(paramsMap)
      if (sourcePath.contains(".")) {
        // sourcePaht 是文件形式
        // TODO: fs 加目录判断
        if (!FileSource.isResultSet(sourcePath)) {
          throw new PipeLineErrorException(
            NOT_A_RESULT_SET_FILE.getErrorCode,
            NOT_A_RESULT_SET_FILE.getErrorDesc
          )
        }
        fileSource = FileSource.create(sourceFsPath, sourceFs)
        excelFsWriter = ExcelFsWriter.getExcelFsWriter(
          DEFAULTC_HARSET,
          DEFAULT_SHEETNAME,
          DEFAULT_DATEFORMATE,
          outputStream,
          excelAutoFormat
        )
      } else {
        // 目录形式
        excelFsWriter = new StorageMultiExcelWriter(outputStream, excelAutoFormat)
        val fsPathListWithError =
          sourceFs.asInstanceOf[FileSystem].listPathWithError(sourceFsPath)
        if (fsPathListWithError == null) {
          throw new PipeLineErrorException(EMPTY_DIR.getErrorCode, EMPTY_DIR.getErrorDesc)
        }
        val fsPathList = fsPathListWithError.getFsPaths
        ResultSetUtils.sortByNameNum(fsPathList)
        fileSource = FileSource.create(fsPathList.toArray(Array[FsPath]()), sourceFs)
      }
      if (!FileSource.isTableResultSet(fileSource)) {
        throw new PipeLineErrorException(
          ONLY_RESULT_CONVERTED_TO_EXCEL.getErrorCode,
          ONLY_RESULT_CONVERTED_TO_EXCEL.getErrorDesc
        )
      }

      var nullValue = options.getOrDefault(PIPELINE_OUTPUT_SHUFFLE_NULL_TYPE, "NULL")
      if (BLANK.equalsIgnoreCase(nullValue)) nullValue = ""
      OutputStreamCache.osCache.put(engineExecutorContext.getJobId.get, outputStream)

      try {
        // Apply field masking if maskedFieldNames is provided
        fileSource.addParams("nullValue", nullValue)
        // 如果同时提供了字段屏蔽和字段截取参数，则先执行字段屏蔽，再执行字段截取
        if (StringUtils.isNotBlank(maskedFieldNames) && FIELD_TRUNCATION_ENABLED) {
          // 同时执行字段屏蔽和字段截取
          ResultUtils.applyFieldMaskingAndTruncation(
            maskedFieldNames,
            excelFsWriter.asInstanceOf[StorageExcelWriter],
            fileSource,
            LinkisStorageConf.FIELD_EXPORT_MAX_LENGTH
          )
        } else if (StringUtils.isNotBlank(maskedFieldNames)) {
          // 只执行字段屏蔽
          ResultUtils.dealMaskedField(maskedFieldNames, excelFsWriter, fileSource)
        } else if (FIELD_TRUNCATION_ENABLED) {
          // 只执行字段截取
          ResultUtils.detectAndHandle(
            excelFsWriter.asInstanceOf[StorageExcelWriter],
            fileSource,
            LinkisStorageConf.FIELD_EXPORT_MAX_LENGTH
          )
        } else {
          // Original stream write logic
          logger.info("No field masking, using stream write for Excel export")
          fileSource.write(excelFsWriter)
        }
      } finally {
        IOUtils.closeQuietly(excelFsWriter)
        IOUtils.closeQuietly(fileSource)
      }
    } finally {
      IOUtils.closeQuietly(sourceFs)
      IOUtils.closeQuietly(destFs)
    }

    super.execute(sourcePath, destPath, engineExecutorContext)
  }

  override def Kind: String = "xlsx"
}

object ExcelExecutor {
  val excelExecutor = new ExcelExecutor

  def getInstance: PipeLineExecutor = excelExecutor
}
