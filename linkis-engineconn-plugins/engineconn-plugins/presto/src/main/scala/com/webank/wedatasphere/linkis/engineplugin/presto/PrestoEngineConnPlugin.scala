package com.webank.wedatasphere.linkis.engineplugin.presto

import java.util

import com.webank.wedatasphere.linkis.engineplugin.presto.builder.PrestoProcessEngineConnLaunchBuilder
import com.webank.wedatasphere.linkis.engineplugin.presto.factory.PrestoEngineConnFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.common.EngineConnPlugin
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.EngineConnFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.{EngineResourceFactory, GenericEngineResourceFactory}
import com.webank.wedatasphere.linkis.manager.label.entity.Label

class PrestoEngineConnPlugin extends EngineConnPlugin {

  private val EP_CONTEXT_CONSTRUCTOR_LOCK = new Object()

  private var engineResourceFactory: EngineResourceFactory = _

  private var engineLaunchBuilder: EngineConnLaunchBuilder = _

  private var engineFactory: EngineConnFactory = _

  private val defaultLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  override def init(params: util.Map[String, Any]): Unit = {

  }

  override def getEngineResourceFactory: EngineResourceFactory = EP_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
    if (null == engineResourceFactory) {
      engineResourceFactory = new GenericEngineResourceFactory
    }
    engineResourceFactory
  }

  override def getEngineConnLaunchBuilder: EngineConnLaunchBuilder = EP_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
    if (null == engineLaunchBuilder) {
      engineLaunchBuilder = new PrestoProcessEngineConnLaunchBuilder
    }
    engineLaunchBuilder
  }

  override def getEngineConnFactory: EngineConnFactory = EP_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
    if (null == engineFactory) {
      engineFactory = new PrestoEngineConnFactory
    }
    engineFactory
  }

  override def getDefaultLabels: util.List[Label[_]] = defaultLabels

}
