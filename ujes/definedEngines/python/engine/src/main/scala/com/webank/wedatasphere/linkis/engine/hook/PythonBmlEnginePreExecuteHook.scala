package com.webank.wedatasphere.linkis.engine.hook

import com.webank.wedatasphere.linkis.bml.hook.BmlEnginePreExecuteHook
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest

class PythonBmlEnginePreExecuteHook extends BmlEnginePreExecuteHook {
  override def callResourcesDownloadedHook(resourcePaths: Array[String], engineExecutorContext: EngineExecutorContext,
                                           executeRequest: ExecuteRequest, code: String): String = {
    (resourcePaths.map(path => s"sys.path.insert(0, '${new FsPath(path).getPath}')") ++ Array(code)).mkString("\n")
  }
}
