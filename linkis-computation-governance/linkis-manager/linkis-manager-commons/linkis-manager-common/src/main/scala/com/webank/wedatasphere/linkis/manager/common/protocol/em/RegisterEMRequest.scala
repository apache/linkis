package com.webank.wedatasphere.linkis.manager.common.protocol.em

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol

/**
  * @author peacewong
  * @date 2020/8/27 17:10
  */
class RegisterEMRequest extends EMRequest with RequestProtocol with Serializable {

  private var serviceInstance: ServiceInstance = null

  private var labels: util.Map[String, AnyRef] = null

  private var nodeResource: NodeResource = null

  private var user: String = null

  private var alias: String = null

  def getServiceInstance: ServiceInstance = serviceInstance

  def setServiceInstance(serviceInstance: ServiceInstance): Unit = {
    this.serviceInstance = serviceInstance
  }

  def getLabels: util.Map[String, AnyRef] = labels

  def setLabels(labels: util.Map[String, AnyRef]): Unit = {
    this.labels = labels
  }

  def getNodeResource: NodeResource = nodeResource

  def setNodeResource(nodeResource: NodeResource): Unit = {
    this.nodeResource = nodeResource
  }

  def setUser(user: String): Unit = {
    this.user = user
  }

  def getAlias: String = alias

  def setAlias(alias: String): Unit = {
    this.alias = alias
  }

  override def getUser: String = this.user
}
