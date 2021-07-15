package com.webank.wedatasphere.linkis.manager.am.service.heartbeat

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration
import com.webank.wedatasphere.linkis.manager.am.service.HeartbeatService
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.AMNodeMetrics
import com.webank.wedatasphere.linkis.manager.common.monitor.ManagerMonitor
import com.webank.wedatasphere.linkis.manager.common.protocol.node.NodeHeartbeatMsg
import com.webank.wedatasphere.linkis.manager.persistence.{NodeManagerPersistence, NodeMetricManagerPersistence}
import com.webank.wedatasphere.linkis.manager.service.common.metrics.MetricsConverter
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.resourcemanager.utils.RMConfiguration
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AMHeartbeatService extends HeartbeatService with Logging {

  @Autowired
  private var nodeManagerPersistence: NodeManagerPersistence = _

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var metricsConverter: MetricsConverter = _


  @Autowired(required = false)
  private var managerMonitor: ManagerMonitor = _


  @PostConstruct
  def init(): Unit = {
    if (null != managerMonitor && AMConfiguration.MONITOR_SWITCH_ON.getValue) {
      info("start init AMHeartbeatService monitor")
      Utils.defaultScheduler.scheduleAtFixedRate(managerMonitor, 1000, RMConfiguration.RM_ENGINE_SCAN_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)

    }
  }


  @Receiver
  override def heartbeatEventDeal(nodeHeartbeatMsg: NodeHeartbeatMsg): Unit = {
    val nodeMetrics = new AMNodeMetrics
    info(s"Am deal nodeHeartbeatMsg $nodeHeartbeatMsg")
    nodeMetrics.setHealthy(metricsConverter.convertHealthyInfo(nodeHeartbeatMsg.getHealthyInfo))
    nodeMetrics.setHeartBeatMsg(nodeHeartbeatMsg.getHeartBeatMsg)
    nodeMetrics.setOverLoad(metricsConverter.convertOverLoadInfo(nodeHeartbeatMsg.getOverLoadInfo))
    nodeMetrics.setServiceInstance(nodeHeartbeatMsg.getServiceInstance)
    if(nodeHeartbeatMsg.getStatus != null){
      nodeMetrics.setStatus(metricsConverter.convertStatus(nodeHeartbeatMsg.getStatus))
    }else{
      nodeMetrics.setStatus(0)
    }
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(nodeMetrics)
  }

}
