package com.webank.wedatasphere.linkis.computation.client.once.action

import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.POSTAction

/**
  * Created by enjoyyin on 2021/6/8.
  */
class GetEngineConnAction extends POSTAction with LinkisManagerAction {
  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

  override def suffixURLs: Array[String] = Array("linkisManager", "getEngineConn")
}
object GetEngineConnAction {
  def newBuilder(): Builder = new Builder

  class Builder private[GetEngineConnAction]() extends ServiceInstanceBuilder[GetEngineConnAction] {
    override protected def createGetEngineConnAction(): GetEngineConnAction = new GetEngineConnAction
  }

}