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

package com.webank.wedatasphere.linkis.engine.pipeline.executor

import java.io.OutputStream

import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.pipeline.OutputStreamCache
import com.webank.wedatasphere.linkis.engine.pipeline.constant.PipeLineConstant._
import com.webank.wedatasphere.linkis.engine.pipeline.conversions.FsConvertions._
import com.webank.wedatasphere.linkis.engine.pipeline.exception.PipeLineErrorException
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.excel.ExcelFsWriter
import com.webank.wedatasphere.linkis.storage.source.FileSource
import org.apache.commons.io.IOUtils

/**
 * Created by johnnwang on 2019/1/30.
 */

class ExcelExecutor extends PipeLineExecutor {
  override def execute(sourcePath: String, destPath: String, engineExecutorContext: EngineExecutorContext): ExecuteResponse = {
    if (!FileSource.isResultSet(sourcePath)) {
      throw new PipeLineErrorException(70005, "不是结果集文件")
    }
    val sourceFsPath = new FsPath(sourcePath)
    val destFsPath = new FsPath(s"$destPath.xlsx")
    val sourceFs = FSFactory.getFs(sourceFsPath)
    sourceFs.init(null)
    val destFs = FSFactory.getFs(destFsPath)
    destFs.init(null)
    val fileSource = FileSource.create(sourceFsPath, sourceFs)
    if (!FileSource.isTableResultSet(fileSource)) {
      throw new PipeLineErrorException(70005, "只有table类型的结果集才能转为excel")
    }
    var nullValue = options.getOrDefault(PIPELINE_OUTPUT_SHUFFLE_NULL_TYPE, "NULL")
    if (BLANK.equalsIgnoreCase(nullValue)) nullValue = ""
    val outputStream: OutputStream = destFs.write(destFsPath, options.get(PIPELINE_OUTPUT_ISOVERWRITE).toBoolean)
    val excelFsWriter = ExcelFsWriter.getExcelFsWriter(DEFAULTC_HARSET, DEFAULT_SHEETNAME, DEFAULT_DATEFORMATE, outputStream)
    import scala.collection.JavaConversions._
    OutputStreamCache.osCache += engineExecutorContext.getJobId.get -> outputStream
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
