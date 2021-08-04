package com.webank.wedatasphere.linkis.engineconnplugin.flink

import com.webank.wedatasphere.linkis.engineconnplugin.flink.factory.FlinkEngineConnFactory
import com.webank.wedatasphere.linkis.engineconnplugin.flink.launch.FlinkEngineConnLaunchBuilder
import com.webank.wedatasphere.linkis.engineconnplugin.flink.resource.FlinkEngineConnResourceFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.common.EngineConnPlugin
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.EngineConnFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.EngineResourceFactory
import com.webank.wedatasphere.linkis.manager.label.entity.Label

/**
  * Created by enjoyyin on 2021/3/25.
  */
class FlinkEngineConnPlugin extends EngineConnPlugin {

  private var engineResourceFactory: EngineResourceFactory = _
  private var engineConnLaunchBuilder: EngineConnLaunchBuilder = _
  private var engineConnFactory: EngineConnFactory = _

  private val EP_CONTEXT_CONSTRUCTOR_LOCK = new Object()


  override def init(params: java.util.Map[String, Any]): Unit = {
    //do noting
//    engineResourceFactory = new FlinkEngineConnResourceFactory
//    engineConnLaunchBuilder = new FlinkEngineConnLaunchBuilder
//    engineConnFactory = new FlinkEngineConnFactory
  }

  override def getEngineResourceFactory: EngineResourceFactory = {
    EP_CONTEXT_CONSTRUCTOR_LOCK.synchronized{
      if(null == engineResourceFactory){
        engineResourceFactory = new FlinkEngineConnResourceFactory
      }
      engineResourceFactory
    }
  }

  override def getEngineConnLaunchBuilder: EngineConnLaunchBuilder = {
    EP_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      // todo check
      if (null == engineConnLaunchBuilder) {
        engineConnLaunchBuilder = new FlinkEngineConnLaunchBuilder()
      }
      engineConnLaunchBuilder
    }
  }

  override def getEngineConnFactory: EngineConnFactory = {
    EP_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (null == engineConnFactory) {
        engineConnFactory = new FlinkEngineConnFactory
      }
      engineConnFactory
    }
  }

  override def getDefaultLabels: java.util.List[Label[_]] = new java.util.ArrayList[Label[_]]
}
