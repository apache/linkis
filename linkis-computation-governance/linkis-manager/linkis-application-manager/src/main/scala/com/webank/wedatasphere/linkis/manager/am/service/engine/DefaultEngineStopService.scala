package com.webank.wedatasphere.linkis.manager.am.service.engine

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineInfoClearRequest, EngineStopRequest, EngineSuicideRequest}
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.protocol.label.NodeLabelRemoveRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DefaultEngineStopService extends AbstractEngineService with EngineStopService with Logging {

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Receiver
  override def stopEngine(engineStopRequest: EngineStopRequest, smc: ServiceMethodContext): Unit = {
    //TODO delete
    engineStopRequest.getServiceInstance.setApplicationName(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue)
    info(s" user ${engineStopRequest.getUser} prepare to stop engine ${engineStopRequest.getServiceInstance}")
    val node = getEngineNodeManager.getEngineNode(engineStopRequest.getServiceInstance)
    if (null == node) {
      info(s" engineConn is not exists in db: $engineStopRequest ")
      return
    }
    node.setLabels(nodeLabelService.getNodeLabels(engineStopRequest.getServiceInstance))
    //clear RM and AM info
    val engineInfoClearRequest = new EngineInfoClearRequest
    engineInfoClearRequest.setEngineNode(node)
    engineInfoClearRequest.setUser(engineStopRequest.getUser)
    val job = smc.publish(engineInfoClearRequest)
    Utils.tryAndWarn(job.get(AMConfiguration.STOP_ENGINE_WAIT.getValue.toLong, TimeUnit.MILLISECONDS))
    info(s"Finished to clear RM info and stop Engine $node")
    // clear Label
    val instanceLabelRemoveRequest = new NodeLabelRemoveRequest(node.getServiceInstance, true)
    val labelJob = smc.publish(instanceLabelRemoveRequest)

    Utils.tryAndWarn(labelJob.get(AMConfiguration.STOP_ENGINE_WAIT.getValue.toLong, TimeUnit.MILLISECONDS))
    info(s"Finished to clear engineNode $node Label info")
    getEngineNodeManager.deleteEngineNode(node)
    info(s" user ${engineStopRequest.getUser} finished to stop engine ${engineStopRequest.getServiceInstance}")
  }

  @Receiver
  override def engineSuicide(engineSuicideRequest: EngineSuicideRequest, smc: ServiceMethodContext): Unit = {
    info(s"Will ask engine : ${engineSuicideRequest.getServiceInstance.toString} of user : ${engineSuicideRequest.getUser} to suicide.")
    EngineStopService.askEngineToSuicide(engineSuicideRequest)
  }
}
