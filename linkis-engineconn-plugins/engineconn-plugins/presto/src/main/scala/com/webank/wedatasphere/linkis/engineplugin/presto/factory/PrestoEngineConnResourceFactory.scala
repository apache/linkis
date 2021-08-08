package com.webank.wedatasphere.linkis.engineplugin.presto.factory

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.resource.Resource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.AbstractEngineResourceFactory

class PrestoEngineConnResourceFactory extends AbstractEngineResourceFactory with Logging {

  override protected def getRequestResource(properties: util.Map[String, String]): Resource = {
    // TODO
    ???
  }

}
