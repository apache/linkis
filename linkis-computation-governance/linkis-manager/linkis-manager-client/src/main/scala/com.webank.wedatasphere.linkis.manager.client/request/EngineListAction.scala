package com.webank.wedatasphere.linkis.manager.client.request

import com.webank.wedatasphere.linkis.httpclient.request.GetAction
import com.webank.wedatasphere.linkis.manager.client.exception.LinkisManagerClientBuilderException

class EngineListAction extends GetAction with LinkisManagerAction {
  override def suffixURLs: Array[String] = Array("linkisManager","listUserEngines")
}
object EngineListAction{
  def builder(): Builder = new Builder
  class Builder private[EngineListAction]() {
    private var user: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def build(): EngineListAction = {
      val action = new EngineListAction
      if(user == null) throw new LinkisManagerClientBuilderException("user is needed!")
      action.setUser(user)
      action
    }
  }
}
