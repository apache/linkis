package com.webank.wedatasphere.linkis.manager.am.service.em

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration
import com.webank.wedatasphere.linkis.manager.am.manager.EMNodeManager
import com.webank.wedatasphere.linkis.manager.common.protocol.em.{EMInfoClearRequest, StopEMRequest}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.protocol.label.NodeLabelRemoveRequest
import com.webank.wedatasphere.linkis.rpc.utils.RPCUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class DefaultEMUnregisterService extends EMUnregisterService with Logging {

  @Autowired
  private var emNodeManager: EMNodeManager = _

  @Receiver
  override def stopEM(stopEMRequest: StopEMRequest, smc: ServiceMethodContext): Unit = {
    info(s" user ${stopEMRequest.getUser} prepare to stop em ${stopEMRequest.getEm}")
    val node = emNodeManager.getEM(stopEMRequest.getEm)
    if (null == node) return
    if (node.getOwner != stopEMRequest.getUser) {
      info(s" ${stopEMRequest.getUser}  are not owner, will not to stopEM")
    }
    if (!RPCUtils.getServiceInstanceFromSender(smc.getSender).equals(stopEMRequest.getEm)) {
      emNodeManager.stopEM(node)
    }
    info(s" user ${stopEMRequest.getUser} Finished to stop em process ${stopEMRequest.getEm}")
    //clear RM info
    val emClearRequest = new EMInfoClearRequest
    emClearRequest.setEm(node)
    emClearRequest.setUser(stopEMRequest.getUser)
    val job = smc.publish(emClearRequest)
    // clear Label
    val instanceLabelRemoveRequest = new NodeLabelRemoveRequest(node.getServiceInstance, false)
    val labelJob = smc.publish(instanceLabelRemoveRequest)
    Utils.tryAndWarn(job.get(AMConfiguration.STOP_ENGINE_WAIT.getValue.toLong, TimeUnit.MILLISECONDS))
    Utils.tryAndWarn(labelJob.get(AMConfiguration.STOP_ENGINE_WAIT.getValue.toLong, TimeUnit.MILLISECONDS))
    clearEMInstanceInfo(emClearRequest)
    info(s" user ${stopEMRequest.getUser} finished to stop em ${stopEMRequest.getEm}")
  }

  override def clearEMInstanceInfo(emClearRequest: EMInfoClearRequest): Unit = {
    info(s" user ${emClearRequest.getUser} prepare to clear em info ${emClearRequest.getEm.getServiceInstance}")
    emNodeManager.deleteEM(emClearRequest.getEm)
    info(s" user ${emClearRequest.getUser} Finished to clear em info ${emClearRequest.getEm.getServiceInstance}")
  }


}
