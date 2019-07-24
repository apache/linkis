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
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.pipeline.util.PipeLineConstants
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.server._
import com.webank.wedatasphere.linkis.storage.FSFactory
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component

/**
  * Created by johnnwang on 2019/1/30.
  */

class CopyExecutor extends PipeLineExecutor{
  override def execute(sourcePath: String, destPath: String): ExecuteResponse = {
    //val sourcePath = pipeEntity.getSource
    val sourceFsPath = new FsPath(sourcePath)
    //val destPath = pipeEntity.getDest
    val destFsPath = new FsPath(destPath)
    val sourceFs = FSFactory.getFs(sourceFsPath)
    sourceFs.init(null)
    val destFs = FSFactory.getFs(destFsPath)
    destFs.init(null)
    val inputStream = sourceFs.read(sourceFsPath)
    var isOverWrite = options.get("pipeline.output.isoverwtite").toBoolean
    //The export table is currently only exported to the workspace, so other places are temporarily not modified.（导出表目前因为是只导出到工作空间，所以别的地方暂时不修改）
    if(!isOverWrite && !destFs.exists(destFsPath)){
      isOverWrite = true
    }
    val outputStream = destFs.write(destFsPath, isOverWrite)
    IOUtils.copy(inputStream, outputStream)
    // TODO: a series of close（一系列的close）
    outputStream.close()
    inputStream.close()
    sourceFs.close()
    destFs.close()
    cleanOptions
    SuccessExecuteResponse()
  }

  override def Kind: String = "cp"

}

object CopyExecutor{
  val copyExecutor = new CopyExecutor
  def getInstance:PipeLineExecutor = copyExecutor
}

