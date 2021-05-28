package com.webank.wedatasphere.linkis.ujes.client.request

import com.webank.wedatasphere.linkis.httpclient.request.GetAction


/**
 * @author alexyang
 * @date 2020/12/21
 * @description
 */
class OpenLogAction private extends GetAction with UJESJobAction {

  override def suffixURLs: Array[String] = Array("filesystem", "openLog")

}

object OpenLogAction {
  def newBuilder(): Builder = new Builder

  class Builder private[OpenLogAction]() {
    private var proxyUser: String = _
    private var logPath: String = _

    def setProxyUser(user: String): Builder = {
      this.proxyUser = user
      this
    }

    def setLogPath(path: String): Builder = {
      this.logPath = path
      this
    }

    def build(): OpenLogAction = {
      val openLogAction = new OpenLogAction
      openLogAction.setUser(proxyUser)
      openLogAction.setParameter("path", logPath)
      openLogAction
    }

  }

}
