package com.webank.wedatasphere.linkis.computation.client.operator

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.computation.client.once.LinkisManagerClient

/**
  * Created by enjoyyin on 2021/6/6.
  */
trait OnceJobOperator[T] extends Operator[T] {

  private var serviceInstance: ServiceInstance = _
  private var linkisManagerClient: LinkisManagerClient = _

  protected def getServiceInstance: ServiceInstance = serviceInstance
  protected def getLinkisManagerClient: LinkisManagerClient = linkisManagerClient

  def setServiceInstance(serviceInstance: ServiceInstance): this.type = {
    this.serviceInstance = serviceInstance
    this
  }

  def setLinkisManagerClient(linkisManagerClient: LinkisManagerClient): this.type = {
    this.linkisManagerClient = linkisManagerClient
    this
  }

}
