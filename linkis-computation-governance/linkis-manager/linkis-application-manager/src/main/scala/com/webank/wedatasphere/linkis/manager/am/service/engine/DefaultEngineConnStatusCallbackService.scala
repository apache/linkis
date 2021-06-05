package com.webank.wedatasphere.linkis.manager.am.service.engine

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.constant.AMConstant
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.AMNodeMetrics
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineConnStatusCallbackToAM
import com.webank.wedatasphere.linkis.manager.persistence.NodeMetricManagerPersistence
import com.webank.wedatasphere.linkis.manager.service.common.metrics.MetricsConverter
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class DefaultEngineConnStatusCallbackService extends EngineConnStatusCallbackService with Logging {

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var metricsConverter: MetricsConverter = _

  @Receiver
  override def dealEngineConnStatusCallback(engineConnStatusCallbackToAM: EngineConnStatusCallbackToAM): Unit = {

    info(s"Start to deal engineConnStatusCallbackToAM $engineConnStatusCallbackToAM")
    val nodeMetrics = new AMNodeMetrics
    val heartBeatMsg: java.util.Map[String, String] = new util.HashMap[String, String]()
    heartBeatMsg.put(AMConstant.START_REASON, engineConnStatusCallbackToAM.initErrorMsg)
    nodeMetrics.setHeartBeatMsg(BDPJettyServerHelper.jacksonJson.writeValueAsString(heartBeatMsg))
    nodeMetrics.setServiceInstance(engineConnStatusCallbackToAM.serviceInstance)
    nodeMetrics.setStatus(metricsConverter.convertStatus(engineConnStatusCallbackToAM.status))
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(nodeMetrics)
    info(s"Finished to deal engineConnStatusCallbackToAM $engineConnStatusCallbackToAM")

  }
}
