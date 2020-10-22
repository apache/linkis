package com.webank.wedatasphere.linkis.sqoop.enginemanager.process

import com.webank.wedatasphere.linkis.enginemanager.AbstractEngineCreator
import com.webank.wedatasphere.linkis.enginemanager.process.ProcessEngineBuilder
import org.springframework.stereotype.Component

/**
 * @Classname SqoopEngineCreator
 * @Description TODO
 * @Date 2020/8/24 16:19
 * @Created by limeng
 */
@Component("engineCreator")
class SqoopEngineCreator extends AbstractEngineCreator{
  override protected def createProcessEngineBuilder(): ProcessEngineBuilder = new SqoopEngineProcessBuilder()
}
