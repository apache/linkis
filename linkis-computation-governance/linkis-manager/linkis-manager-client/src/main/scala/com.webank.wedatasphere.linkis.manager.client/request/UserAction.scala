package com.webank.wedatasphere.linkis.manager.client.request

trait UserAction extends com.webank.wedatasphere.linkis.httpclient.request.UserAction {
  private var user: String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = user
}
