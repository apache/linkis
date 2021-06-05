package com.webank.wedatasphere.linkis.manager.am.label

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.em.EMInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService
import com.webank.wedatasphere.linkis.manager.service.common.label.ManagerLabelService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._

/**
  * @author peacewong
  * @date 2020/8/5 14:46
  */
@Service
class DefaultManagerLabelService extends ManagerLabelService with Logging {


  @Autowired
  private var nodeLabelService: NodeLabelService = _


  override def isEngine(serviceInstance: ServiceInstance): Boolean = {
    val list = nodeLabelService.getNodeLabels(serviceInstance)
    isEngine(list)
  }

  override def isEM(serviceInstance: ServiceInstance): Boolean = {
    val list = nodeLabelService.getNodeLabels(serviceInstance)
    val isEngine = list.exists {
      case _: EngineInstanceLabel =>
        true
      case _ => false
    }
    if (!isEngine) {
      list.exists {
        case _: EMInstanceLabel =>
          true
        case _ => false
      }
    } else {
      false
    }
  }

  override def isEngine(labels: util.List[Label[_]]): Boolean = {
    labels.exists {
      case _: EngineInstanceLabel =>
        true
      case _ => false
    }
  }
}
