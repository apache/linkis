package com.webank.wedatasphere.linkis.shell.enginemanager.process

import com.webank.wedatasphere.linkis.enginemanager.AbstractEngineCreator
import com.webank.wedatasphere.linkis.enginemanager.process.ProcessEngineBuilder
import org.springframework.stereotype.Component

/**
  * created by cooperyang on 2019/5/14
  * Description:
  */
@Component("engineCreator")
class ShellEngineCreator extends AbstractEngineCreator{
  override protected def createProcessEngineBuilder(): ProcessEngineBuilder = new ShellEngineProcessBuilder
}
