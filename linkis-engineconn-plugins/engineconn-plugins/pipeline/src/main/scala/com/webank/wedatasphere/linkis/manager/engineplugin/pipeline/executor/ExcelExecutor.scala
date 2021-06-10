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

package com.webank.wedatasphere.linkis.manager.engineplugin.pipeline.executor

import java.io.OutputStream

import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.manager.engineplugin.pipeline.conf.PipelineEngineConfiguration.PIPELINE_OUTPUT_ISOVERWRITE_SWITCH
import com.webank.wedatasphere.linkis.manager.engineplugin.pipeline.constant.PipeLineConstant._
import com.webank.wedatasphere.linkis.manager.engineplugin.pipeline.exception.PipeLineErrorException
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.excel.{ExcelFsWriter, StorageMultiExcelWriter}
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import com.webank.wedatasphere.linkis.storage.source.FileSource
import org.apache.commons.io.IOUtils

class ExcelExecutor extends PipeLineExecutor {
  override def execute(sourcePath: String, destPath: String, engineExecutorContext: EngineExecutionContext): ExecuteResponse = {
    var fileSource: FileSource = null
    var excelFsWriter: ExcelFsWriter = null
    val sourceFsPath = new FsPath(sourcePath)
    val destFsPath = new FsPath(s"$destPath.xlsx")
    val sourceFs = FSFactory.getFs(sourceFsPath)
    sourceFs.init(null)
    val destFs = FSFactory.getFs(destFsPath)
    destFs.init(null)
    val outputStream: OutputStream = destFs.write(destFsPath, PIPELINE_OUTPUT_ISOVERWRITE_SWITCH.getValue(options))
    if (sourcePath.contains(".")) {
      //sourcePaht 是文件形式
      // TODO: fs 加目录判断
      if (!FileSource.isResultSet(sourcePath)) {
        throw new PipeLineErrorException(70003, "Not a result set file(不是结果集文件)")
      }
      fileSource = FileSource.create(sourceFsPath, sourceFs)
      excelFsWriter = ExcelFsWriter.getExcelFsWriter(DEFAULTC_HARSET, DEFAULT_SHEETNAME, DEFAULT_DATEFORMATE, outputStream)
    } else {
      //目录形式
      excelFsWriter = new StorageMultiExcelWriter(outputStream)
      val fsPathListWithError = sourceFs.asInstanceOf[FileSystem].listPathWithError(sourceFsPath)
      if (fsPathListWithError == null) {
        throw new PipeLineErrorException(70005, "empty dir!")
      }
      fileSource = FileSource.create(fsPathListWithError.getFsPaths.toArray(Array[FsPath]()), sourceFs)
    }
    if (!FileSource.isTableResultSet(fileSource)) {
      throw new PipeLineErrorException(70004, "Only result sets of type Table can be converted to Excel(只有table类型的结果集才能转为excel)")
    }
    var nullValue = options.getOrDefault(PIPELINE_OUTPUT_SHUFFLE_NULL_TYPE, "NULL")
    if (BLANK.equalsIgnoreCase(nullValue)) nullValue = ""
    OutputStreamCache.osCache.put(engineExecutorContext.getJobId.get, outputStream)
    fileSource.addParams("nullValue", nullValue).write(excelFsWriter)
    IOUtils.closeQuietly(excelFsWriter)
    IOUtils.closeQuietly(fileSource)
    IOUtils.closeQuietly(sourceFs)
    IOUtils.closeQuietly(destFs)
    super.execute(sourcePath, destPath, engineExecutorContext)
  }

  override def Kind: String = "excel"
}

object ExcelExecutor {
  val excelExecutor = new ExcelExecutor

  def getInstance: PipeLineExecutor = excelExecutor
}
