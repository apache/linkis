package com.webank.wedatasphere.linkis.engine.io

import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorFactory}
import com.webank.wedatasphere.linkis.server.JMap
import org.springframework.stereotype.Component

/**
  * Created by johnnwang on 2018/10/30.
  */
@Component("engineExecutorFactory")
class IOEngineExecutorFactory extends EngineExecutorFactory{
  override def createExecutor(options: JMap[String, String]): EngineExecutor = {
    new IOEngineExecutor()
  }
}
