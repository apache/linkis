/**
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.entrance.utils

import java.util.function.{Consumer, Supplier}

import com.facebook.presto.resourceGroups.{FileResourceGroupConfig, FileResourceGroupConfigurationManager}
import com.facebook.presto.spi.memory.{ClusterMemoryPoolManager, MemoryPoolId, MemoryPoolInfo}
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
      groupManagerMap += (filePath -> new FileResourceGroupConfigurationManager(new ClusterMemoryPoolManager {
        override def addChangeListener(poolId: MemoryPoolId, listener: Consumer[MemoryPoolInfo]): Unit = {}
      }, config))
    }
    groupManagerMap(filePath).`match`(criteria)
      .orElseThrow(new Supplier[Throwable] {
        override def get(): Throwable = PrestoSourceGroupException("Presto resource group not found.")
      })
      .getResourceGroupId.toString
  }
}
