package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.governance.common.protocol.engineconn.{RequestEngineStatusBatch, ResponseEngineStatusBatch}
import com.webank.wedatasphere.linkis.manager.am.vo.AMEngineNodeVo
import com.webank.wedatasphere.linkis.manager.common.entity.node.{EMNode, EngineNode}

/**
 * created by v_wbjftang on 2020/8/10
 *
 */
trait EngineInfoService {
  /**
   * 通过user获取EngineNode 的基本信息，含metric
   *
   * @param user
   * @return
   */
  def listUserEngines(user: String): java.util.List[EngineNode]

  /**
   * 通过em（主要是instance信息） 获取engine的基本信息，含metric
   *
   * @param em
   * @return
   */
  def listEMEngines(em: EMNode): java.util.List[EngineNode]

  def dealBatchGetEngineStatus(request: RequestEngineStatusBatch): ResponseEngineStatusBatch = ResponseEngineStatusBatch(null, "Please implements method")

  def modifyEngineLabel(instance: ServiceInstance, map: java.util.Map[String,String]):Unit

}
