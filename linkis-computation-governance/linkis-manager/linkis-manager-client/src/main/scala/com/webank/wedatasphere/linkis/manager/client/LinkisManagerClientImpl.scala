package com.webank.wedatasphere.linkis.manager.client

import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig
import com.webank.wedatasphere.linkis.httpclient.request.Action
import com.webank.wedatasphere.linkis.httpclient.response.Result
import com.webank.wedatasphere.linkis.manager.client.request.LinkisManagerAction

class LinkisManagerClientImpl(clientConfig: DWSClientConfig) extends LinkisManagerClient{
  private val dwsHttpClient = new DWSHttpClient(clientConfig, "LinkisManager-Execution-Thread")
  override protected[client] def executeJob(linkisManagerJobAction: LinkisManagerAction): Result = linkisManagerJobAction match {
    case action: Action => dwsHttpClient.execute(action)
  }
  override def close(): Unit = dwsHttpClient.close()
}
