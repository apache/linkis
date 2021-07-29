package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.builder

import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder
import com.webank.wedatasphere.linkis.manager.label.entity.engine.UserCreatorLabel
import com.webank.wedatasphere.linkis.storage.utils.StorageConfiguration

class ElasticSearchProcessEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder {

  override def getEngineStartUser(label: UserCreatorLabel): String = {
    StorageConfiguration.HDFS_ROOT_USER.getValue
  }

}
