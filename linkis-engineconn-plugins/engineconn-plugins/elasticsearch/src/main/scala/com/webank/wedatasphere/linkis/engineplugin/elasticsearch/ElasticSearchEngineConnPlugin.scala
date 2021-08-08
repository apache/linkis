package com.webank.wedatasphere.linkis.engineplugin.elasticsearch

import java.util

import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.builder.ElasticSearchProcessEngineConnLaunchBuilder
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.factory.ElasticSearchEngineConnFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.common.EngineConnPlugin
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.EngineConnFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.{EngineResourceFactory, GenericEngineResourceFactory}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineType, EngineTypeLabel}

class ElasticSearchEngineConnPlugin extends EngineConnPlugin {

  private val EP_CONTEXT_CONSTRUCTOR_LOCK = new Object()

  private var engineResourceFactory: EngineResourceFactory = _

  private var engineLaunchBuilder: EngineConnLaunchBuilder = _

  private var engineFactory: EngineConnFactory = _

  private val defaultLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  override def init(params: util.Map[String, Any]): Unit = {
    val typeLabel =new EngineTypeLabel()
    typeLabel.setEngineType(EngineType.ELASTICSEARCH.toString)
    typeLabel.setVersion(ElasticSearchConfiguration.DEFAULT_VERSION.getValue)
    this.defaultLabels.add(typeLabel)
  }

  override def getEngineResourceFactory: EngineResourceFactory = EP_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
    if (null == engineResourceFactory) {
      engineResourceFactory = new GenericEngineResourceFactory
    }
    engineResourceFactory
  }

  override def getEngineConnLaunchBuilder: EngineConnLaunchBuilder = EP_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
    if (null == engineLaunchBuilder) {
      engineLaunchBuilder = new ElasticSearchProcessEngineConnLaunchBuilder
    }
    engineLaunchBuilder
  }

  override def getEngineConnFactory: EngineConnFactory = EP_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
    if (null == engineFactory) {
      engineFactory = new ElasticSearchEngineConnFactory
    }
    engineFactory
  }

  override def getDefaultLabels: util.List[Label[_]] = defaultLabels

}
