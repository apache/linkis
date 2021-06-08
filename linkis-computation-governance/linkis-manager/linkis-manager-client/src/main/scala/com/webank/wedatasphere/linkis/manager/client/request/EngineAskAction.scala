package com.webank.wedatasphere.linkis.manager.client.request

import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.POSTAction
import com.webank.wedatasphere.linkis.manager.client.constants.EngineConstant
import com.webank.wedatasphere.linkis.manager.client.exception.LinkisManagerClientBuilderException
import org.apache.commons.lang.StringUtils

import java.util

class EngineAskAction private() extends POSTAction with LinkisManagerAction{
  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

  override def suffixURLs: Array[String] = Array("linkisManager","askEngine")
}
object EngineAskAction{
  def builder(): Builder = new Builder
  class Builder private[EngineAskAction]() {
    private var user: String = _
    private var properties:java.util.Map[String,Any] = _
    private var labels:java.util.Map[String,String] = _
    private var timeOut:Long = _
    private var createService:String = _
    private var description:String =_

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setProperties(properties:java.util.Map[String,Any]): Builder ={
      this.properties = properties
      this
    }

    def setLabels(labels:java.util.Map[String,String]): Builder ={
      this.labels = labels
      this
    }

    def setTimeOut(timeOut:Long): Builder ={
      this.timeOut = timeOut
      this
    }

    def setCreateService(createService:String): Builder ={
      this.createService = createService
      this
    }

    def setDescription(description:String): Builder ={
      this.description = description
      this
    }


    def build(): EngineAskAction = {
     val action = new EngineAskAction()
      if(user == null) throw new LinkisManagerClientBuilderException("user is needed!")
      if(properties == null) properties = new util.HashMap[String,Any]()
      if(labels == null) labels = new util.HashMap[String,String]()
      if(createService == null)  throw new LinkisManagerClientBuilderException("createService is needed!")
      if(timeOut > 0L) action.addRequestPayload(EngineConstant.TIMEOUT,timeOut)
      if(StringUtils.isNotBlank(description))  action.addRequestPayload(EngineConstant.DESCRIPTION,description)

      action.addRequestPayload(EngineConstant.USER,user)
      action.addRequestPayload(EngineConstant.PROPERTIES,properties)
      action.addRequestPayload(EngineConstant.LABELS,labels)
      action.addRequestPayload(EngineConstant.CREATESERVICE,createService)
      action
    }

  }
}