package com.webank.wedatasphere.linkis.entrance.utils

import com.facebook.presto.resourceGroups.{FileResourceGroupConfig, FileResourceGroupConfigurationManager}
import com.facebook.presto.spi.resourceGroups.SelectionCriteria
import com.webank.wedatasphere.linkis.entrance.exception.PrestoSourceGroupException

import scala.collection.mutable

/**
 * Created by yogafire on 2020/5/21
 */
object PrestoResourceUtils {

  private val groupManagerMap = mutable.Map[String, FileResourceGroupConfigurationManager]()

  def getGroupName(criteria: SelectionCriteria, filePath: String): String = {
    //FIXME add LRU cache; update cache
    if (!groupManagerMap.contains(filePath)) {
      val config = new FileResourceGroupConfig()
      config.setConfigFile(filePath)
      groupManagerMap += (filePath -> new FileResourceGroupConfigurationManager((_, _) => {}, config))
    }
    groupManagerMap(filePath).`match`(criteria)
      .orElseThrow(() => PrestoSourceGroupException("Presto resource group not found."))
      .getResourceGroupId.toString
  }
}
