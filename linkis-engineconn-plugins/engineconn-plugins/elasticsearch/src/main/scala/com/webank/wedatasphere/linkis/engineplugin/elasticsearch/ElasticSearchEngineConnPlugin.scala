package com.webank.wedatasphere.linkis.engineplugin.elasticsearch

import java.util

import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.ElasticSearchEngineConnPlugin._
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

  private val defaultLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  override def init(params: util.Map[String, Any]): Unit = {
    val typeLabel =new EngineTypeLabel()
    typeLabel.setEngineType(EngineType.ELASTICSEARCH.toString)
    typeLabel.setVersion(ElasticSearchConfiguration.DEFAULT_VERSION.getValue)
    this.defaultLabels.add(typeLabel)
  }

  override def getEngineResourceFactory: EngineResourceFactory = ENGINE_RESOURCE_FACTORY

  override def getEngineConnLaunchBuilder: EngineConnLaunchBuilder = ENGINE_LAUNCH_BUILDER

  override def getEngineConnFactory: EngineConnFactory = ENGINE_FACTORY

  override def getDefaultLabels: util.List[Label[_]] = defaultLabels

}

private object ElasticSearchEngineConnPlugin {

  val ENGINE_LAUNCH_BUILDER = new ElasticSearchProcessEngineConnLaunchBuilder

  val ENGINE_RESOURCE_FACTORY = new GenericEngineResourceFactory

  val ENGINE_FACTORY = new ElasticSearchEngineConnFactory

}
