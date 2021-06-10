package com.webank.wedatasphere.linkis.manager.client.request

import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.POSTAction
import com.webank.wedatasphere.linkis.manager.client.constants.EngineConstant
import com.webank.wedatasphere.linkis.manager.client.exception.LinkisManagerClientBuilderException


class EngineEMListAction  private() extends POSTAction with LinkisManagerAction {
  override def suffixURLs: Array[String] = Array("linkisManager","listEMEngines")

  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)
}
object EngineEMListAction{
  def builder(): Builder = new Builder
  class Builder private[EngineEMListAction]() {
    private var user: String = _
    private var em:String = _

    def setUser(user:String): Builder ={
      this.user = user
      this
    }
    def setEm(em:String): Builder ={
      this.em = em
      this
    }
    def build(): EngineEMListAction = {
      val action = new EngineEMListAction
      if(user == null) throw new LinkisManagerClientBuilderException("user is needed!")
      if(em == null) throw new LinkisManagerClientBuilderException("em is needed!")
      action.setUser(user)
      action.addRequestPayload(EngineConstant.EM,em)
      action
    }

  }
}
