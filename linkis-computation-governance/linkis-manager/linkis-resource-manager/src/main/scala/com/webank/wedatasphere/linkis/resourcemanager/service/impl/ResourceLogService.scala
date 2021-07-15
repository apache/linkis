package com.webank.wedatasphere.linkis.resourcemanager.service.impl

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{Resource, ResourceType}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.em.EMInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineInstanceLabel
import com.webank.wedatasphere.linkis.resourcemanager.service.LabelResourceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
case class ResourceLogService() extends Logging{

  @Autowired
  var labelResourceService: LabelResourceService = _

  private def printLog(changeType: String, status: String, engineLabel: EngineInstanceLabel = null, ecmLabel: EMInstanceLabel = null) :String = {
    val logString = new StringBuilder(changeType + " ")
    logString ++= (status + ", ")
    if (engineLabel != null) {
      val engineResource = labelResourceService.getLabelResource(engineLabel)
      var usedResource = Resource.initResource(ResourceType.Default)
      if (engineResource != null && engineResource.getUsedResource != null) {
        usedResource = engineResource.getUsedResource
      }
      logString ++= ("engine current resource:")
      logString ++= (engineLabel.getServiceInstance.getInstance)
      logString ++= (usedResource.toJson + " ")
    }
    if (ecmLabel != null) {
      val ecmResource = labelResourceService.getLabelResource(ecmLabel)
      var usedResource = Resource.initResource(ResourceType.Default)
      if (ecmResource != null && ecmResource.getUsedResource != null) {
        usedResource = ecmResource.getUsedResource
      }
      logString ++= ("ecm current resource:")
      logString ++= (ecmLabel.getServiceInstance.getInstance)
      logString ++= (usedResource.toJson + " ")
    }
    logString.toString()
  }

  def failed(changeType: String, engineLabel: EngineInstanceLabel = null, ecmLabel: EMInstanceLabel = null, exception: Exception = null): Unit = {
    if (changeType != null) {
      val log: String = changeType match {
        case ChangeType.ENGINE_INIT => {
          printLog(changeType, ChangeType.FAILED, engineLabel, ecmLabel)
        }
        case ChangeType.ENGINE_CLEAR => {
          printLog(changeType, ChangeType.FAILED, engineLabel, ecmLabel)
        }
        case ChangeType.ENGINE_RESOURCE_ADD => {
          printLog(changeType, ChangeType.FAILED, engineLabel, ecmLabel)
        }
        case ChangeType.ENGINE_RESOURCE_MINUS => {
          printLog(changeType, ChangeType.FAILED, engineLabel, ecmLabel)
        }
        case ChangeType.ECM_INIT => {
          printLog(changeType, ChangeType.FAILED, null, ecmLabel)
        }
        case ChangeType.ECM_CLEAR => {
          printLog(changeType, ChangeType.FAILED, null, ecmLabel)
        }
        case ChangeType.ECM_RESOURCE_ADD => {
          printLog(changeType, ChangeType.FAILED, engineLabel, ecmLabel)
        }
        case ChangeType.ECM_Resource_MINUS => {
          printLog(changeType, ChangeType.FAILED, engineLabel, ecmLabel)
        }
        case _ => " "
      }
      if(exception != null){
        error(log, exception)
      }else{
        error(log)
      }
    }
  }
  def success(changeType: String, engineLabel: EngineInstanceLabel = null, ecmLabel: EMInstanceLabel = null): Unit = {
    if (changeType != null) {
      val log: String = changeType match {
        case ChangeType.ENGINE_INIT => {
          printLog(changeType, ChangeType.SUCCESS, engineLabel, ecmLabel)
        }
        case ChangeType.ENGINE_CLEAR => {
          printLog(changeType, ChangeType.SUCCESS, engineLabel, ecmLabel)
        }
        case ChangeType.ENGINE_RESOURCE_ADD => {
          printLog(changeType, ChangeType.SUCCESS, engineLabel, ecmLabel)
        }
        case ChangeType.ENGINE_RESOURCE_MINUS => {
          printLog(changeType, ChangeType.SUCCESS, engineLabel, ecmLabel)
        }
        case ChangeType.ECM_INIT => {
          printLog(changeType, ChangeType.SUCCESS, null, ecmLabel)
        }
        case ChangeType.ECM_CLEAR => {
          printLog(changeType, ChangeType.SUCCESS, null, ecmLabel)
        }
        case ChangeType.ECM_RESOURCE_ADD => {
          printLog(changeType, ChangeType.SUCCESS, engineLabel, ecmLabel)
        }
        case ChangeType.ECM_Resource_MINUS => {
          printLog(changeType, ChangeType.SUCCESS, engineLabel, ecmLabel)
        }
        case _ =>" "
      }
      info(log)
    }
  }
}

object ChangeType {

  val ENGINE_INIT = "EngineResourceInit"

  val ENGINE_CLEAR = "EngineResourceClear"

  val ENGINE_RESOURCE_ADD = "EngineResourceAdd"

  val ENGINE_RESOURCE_MINUS = "EngineResourceMinus"

  val ECM_INIT = "ECMResourceInit"

  val ECM_CLEAR = "ECMResourceClear"

  val ECM_RESOURCE_ADD = "ECMResourceAdd"

  val ECM_Resource_MINUS = "ECMResourceMinus"

  val SUCCESS = "success"

  val FAILED = "failed"
}