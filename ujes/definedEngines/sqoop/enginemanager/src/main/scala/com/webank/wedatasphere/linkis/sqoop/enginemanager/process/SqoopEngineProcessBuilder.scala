package com.webank.wedatasphere.linkis.sqoop.enginemanager.process

import com.webank.wedatasphere.linkis.enginemanager.EngineResource
import com.webank.wedatasphere.linkis.enginemanager.process.JavaProcessEngineBuilder
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine

/**
 * @Classname ShellEngineProcessBuilder
 * @Description TODO
 * @Date 2020/8/24 16:21
 * @Created by limeng
 */
class SqoopEngineProcessBuilder extends JavaProcessEngineBuilder {
  override protected def getExtractJavaOpts: String = ""

  override protected def getAlias(request: RequestEngine): String = ""

  override protected def getExtractClasspath: Array[String] = Array()

  override protected def classpathCheck(jarOrFiles: Array[String]): Unit = ""

  override protected val addApacheConfigPath: Boolean = true

  override def build(engineRequest: EngineResource, request: RequestEngine): Unit = {
    super.build(engineRequest, request)
  }
}
