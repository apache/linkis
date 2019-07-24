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
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream}

import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.pipeline.exception.PipeLineErrorException
import com.webank.wedatasphere.linkis.engine.pipeline.util.{PipeLineConstants, PipeLineUtils}
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.server._
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.csv.CSVFsWriter
import com.webank.wedatasphere.linkis.storage.excel.ExcelFsWriter
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetReader}
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component

/**
  * Created by johnnwang on 2019/1/30.
  */

class CSVExecutor extends PipeLineExecutor{

  override def execute(sourcePath: String, destPath: String): ExecuteResponse = {
    //val sourcePath = pipeEntity.getSource
    if (!PipeLineUtils.isDolphin(sourcePath)) throw new PipeLineErrorException(70005, "Not a result set file（不是结果集文件）")
    val sourceFsPath = new FsPath(sourcePath)
    //val destPath = pipeEntity.getDest
    val destFsPath = new FsPath(destPath+"."+Kind)
    val sourceFs = FSFactory.getFs(sourceFsPath)
    sourceFs.init(null)
    val destFs = FSFactory.getFs(destFsPath)
    destFs.init(null)
    val resultset = ResultSetFactory.getInstance.getResultSetByPath(sourceFsPath)
    val reader = ResultSetReader.getResultSetReader(resultset, sourceFs.read(sourceFsPath))
    val metadata = reader.getMetaData
    if (!PipeLineUtils.isTableResultset(metadata)) throw new PipeLineErrorException(70005, "Only the result set of the table type can be converted to csv（只有table类型的结果集才能转为csv）");
    val cSVFsWriter = CSVFsWriter.getCSVFSWriter(options.get("pipeline.output.charset"),options.get("pipeline.field.split"))
    cSVFsWriter.addMetaData(metadata)
    while (reader.hasNext){
      cSVFsWriter.addRecord(reader.getRecord)
    }
    val inputStream = cSVFsWriter.getCSVStream
    val outputStream:OutputStream = destFs.write(destFsPath,options.get("pipeline.output.isoverwtite").toBoolean)
    // TODO: a series of close(一系列的close)
    IOUtils.copy(inputStream,outputStream)
    IOUtils.closeQuietly(outputStream)
    IOUtils.closeQuietly(inputStream)
    if (cSVFsWriter != null) cSVFsWriter.close()
    if (reader != null) reader.close()
    if(sourceFs != null) sourceFs.close()
    if(destFs != null) destFs.close()
    cleanOptions
    SuccessExecuteResponse()
  }

  override def Kind: String = "csv"

}
object CSVExecutor{
  val csvExecutor = new CSVExecutor
  def getInstance:PipeLineExecutor = csvExecutor
}
