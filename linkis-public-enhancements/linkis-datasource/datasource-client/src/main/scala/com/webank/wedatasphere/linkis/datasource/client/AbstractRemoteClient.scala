package com.webank.wedatasphere.linkis.datasource.client

import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.Action
import com.webank.wedatasphere.linkis.httpclient.response.Result

abstract class AbstractRemoteClient extends RemoteClient {
  protected val dwsHttpClient:DWSHttpClient

  override def execute(action: Action): Result = action match {
    case action: Action => dwsHttpClient.execute(action)
  }

  override def close(): Unit = dwsHttpClient.close()
}
