package com.webank.wedatasphere.linkis.shell.enginemanager.process

import com.webank.wedatasphere.linkis.enginemanager.EngineResource
import com.webank.wedatasphere.linkis.enginemanager.process.JavaProcessEngineBuilder
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine

/**
  * created by cooperyang on 2019/5/14
  * Description:
  */
class ShellEngineProcessBuilder extends JavaProcessEngineBuilder{

  override protected def getExtractJavaOpts: String = ""

  override protected def getAlias(request: RequestEngine): String = ""

  override protected def getExtractClasspath: Array[String] = Array()

  override protected def classpathCheck(jarOrFiles: Array[String]): Unit = ""

  override protected val addApacheConfigPath: Boolean = true

  override def build(engineRequest: EngineResource, request: RequestEngine): Unit = {
    super.build(engineRequest, request)
  }

}
