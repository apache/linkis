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

import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.manager.engineplugin.pipeline.conf.PipelineEngineConfiguration.PIPELINE_OUTPUT_ISOVERWRITE_SWITCH
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse
import com.webank.wedatasphere.linkis.storage.FSFactory
import org.apache.commons.io.IOUtils

class CopyExecutor extends PipeLineExecutor {
  override def execute(sourcePath: String, destPath: String, engineExecutionContext: EngineExecutionContext): ExecuteResponse = {
    val sourceFsPath = new FsPath(sourcePath)
    val destFsPath = new FsPath(destPath)
    val sourceFs = FSFactory.getFs(sourceFsPath)
    sourceFs.init(null)
    val destFs = FSFactory.getFs(destFsPath)
    destFs.init(null)
    val inputStream = sourceFs.read(sourceFsPath)
    var isOverWrite = PIPELINE_OUTPUT_ISOVERWRITE_SWITCH.getValue(options)
    //导出表目前因为是只导出到工作空间，所以别的地方暂时不修改
    // todo check
    if (!isOverWrite && !destFs.exists(destFsPath)) {
      isOverWrite = true
    }
    val outputStream = destFs.write(destFsPath, isOverWrite)
    OutputStreamCache.osCache.put(engineExecutionContext.getJobId.get, outputStream)
    IOUtils.copy(inputStream, outputStream)
    IOUtils.closeQuietly(outputStream)
    IOUtils.closeQuietly(inputStream)
    IOUtils.closeQuietly(sourceFs)
    IOUtils.closeQuietly(destFs)
    super.execute(sourcePath, destPath, engineExecutionContext)
  }

  override def Kind: String = "cp"

}

object CopyExecutor {
  val copyExecutor = new CopyExecutor

  def getInstance: PipeLineExecutor = copyExecutor
}


