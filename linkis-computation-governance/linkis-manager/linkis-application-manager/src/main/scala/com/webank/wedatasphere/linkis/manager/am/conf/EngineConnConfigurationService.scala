package com.webank.wedatasphere.linkis.manager.am.conf

import java.util

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.server.JMap
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}

import scala.collection.JavaConversions._

/**
  * @author peacewong
  * @date 2020/9/15 20:46
  */
trait EngineConnConfigurationService {

  def getConsoleConfiguration(label: util.List[Label[_]]): util.Map[String, String]

}

class DefaultEngineConnConfigurationService extends EngineConnConfigurationService with Logging {

  override def getConsoleConfiguration(label: util.List[Label[_]]): util.Map[String, String] = {
    val properties = new JMap[String, String]
    val userCreatorLabelOption = label.find(_.isInstanceOf[UserCreatorLabel])
    val engineTypeLabelOption = label.find(_.isInstanceOf[EngineTypeLabel])
    if (userCreatorLabelOption.isDefined) {
      val userCreatorLabel = userCreatorLabelOption.get.asInstanceOf[UserCreatorLabel]
      val globalConfig = Utils.tryAndWarn(ConfigurationMapCache.globalMapCache.getCacheMap(userCreatorLabel))
      if (null != globalConfig) {
        properties.putAll(globalConfig)
      }
      if (engineTypeLabelOption.isDefined) {
        val engineTypeLabel = engineTypeLabelOption.get.asInstanceOf[EngineTypeLabel]
        val engineConfig = Utils.tryAndWarn(ConfigurationMapCache.engineMapCache.getCacheMap((userCreatorLabel, engineTypeLabel)))
        if (null != engineConfig) {
          properties.putAll(engineConfig)
        }
      }
    }
    properties
  }

}


@Configuration
class ApplicationManagerSpringConfiguration{

  @ConditionalOnMissingBean
  @Bean
  def getDefaultEngineConnConfigurationService:EngineConnConfigurationService ={
    new DefaultEngineConnConfigurationService
  }

}