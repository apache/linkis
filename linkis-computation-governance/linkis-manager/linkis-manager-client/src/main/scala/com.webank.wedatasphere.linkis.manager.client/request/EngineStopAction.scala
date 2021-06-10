package com.webank.wedatasphere.linkis.manager.client.request

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.POSTAction
import com.webank.wedatasphere.linkis.manager.client.constants.EngineConstant
import com.webank.wedatasphere.linkis.manager.client.exception.LinkisManagerClientBuilderException
import org.apache.commons.lang.StringUtils


class EngineStopAction private() extends POSTAction with LinkisManagerAction{
  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

  override def suffixURLs: Array[String] = Array("linkisManager","stopEngine")
}
object EngineStopAction {
  def builder(): Builder = new Builder

  class Builder private[EngineStopAction]() {
    private var user: String = _
    private var serviceInstance: ServiceInstance = _
    private var applicationName: String = _
    private var instance: String=_

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setServiceInstance(serviceInstance: ServiceInstance): Builder = {
      this.serviceInstance = serviceInstance
      this
    }

    def setApplicationName(applicationName: String): Builder ={
      this.applicationName = applicationName
      this
    }

    def setInstance(instance: String): Builder ={
      this.instance = instance
      this
    }

    def getServiceInstance:ServiceInstance={
      if(serviceInstance != null) return serviceInstance
      else if(StringUtils.isNotBlank(applicationName) && StringUtils.isNotBlank(instance)){
        return ServiceInstance(applicationName, instance)
      }else throw new LinkisManagerClientBuilderException("serviceInstance is needed!")
    }


    def build(): EngineStopAction = {
      val action = new EngineStopAction()
      if (user == null) throw new LinkisManagerClientBuilderException("user is needed!")

      action.addRequestPayload(EngineConstant.USER, user)
      action.addRequestPayload(EngineConstant.SERVICEINSTANCE, getServiceInstance)
      action
    }

  }
}
