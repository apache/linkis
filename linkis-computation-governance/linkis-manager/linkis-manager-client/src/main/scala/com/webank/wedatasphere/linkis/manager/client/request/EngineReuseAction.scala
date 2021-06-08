package com.webank.wedatasphere.linkis.manager.client.request

import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.POSTAction
import com.webank.wedatasphere.linkis.manager.client.constants.EngineConstant
import com.webank.wedatasphere.linkis.manager.client.exception.LinkisManagerClientBuilderException


import java.util

class EngineReuseAction private() extends POSTAction with LinkisManagerAction{
  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

  override def suffixURLs: Array[String] = Array("linkisManager","reuseEngine")
}
object EngineReuseAction{
  def builder(): Builder = new Builder
  class Builder private[EngineReuseAction]() {
    private var user: String = _
    private var labels:java.util.Map[String,String] = _
    private var timeOut:Long = _
    private var reuseCount:Int = _

    def setUser(user: String): Builder = {
      this.user = user
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

    def setReuseCount(reuseCount:Int): Builder ={
      this.reuseCount = reuseCount
      this
    }

    def build(): EngineReuseAction = {
      val action = new EngineReuseAction()
      if(user == null) throw new LinkisManagerClientBuilderException("user is needed!")

      if(labels == null) labels = new util.HashMap[String,String]()
      if(timeOut > 0L) action.addRequestPayload(EngineConstant.TIMEOUT,timeOut)
      if(reuseCount > 0) action.addRequestPayload(EngineConstant.REUSECOUNT,reuseCount)

      action.addRequestPayload(EngineConstant.USER,user)
      action.addRequestPayload(EngineConstant.LABELS,labels)
      action
    }

  }
}