package com.webank.wedatasphere.linkis.httpclient

import com.webank.wedatasphere.linkis.common.io.{Fs, FsPath}
import com.webank.wedatasphere.linkis.httpclient.config.ClientConfig
import com.webank.wedatasphere.linkis.httpclient.discovery.Discovery
import com.webank.wedatasphere.linkis.httpclient.exception.HttpMethodNotSupportException
import com.webank.wedatasphere.linkis.httpclient.request.HttpAction
import com.webank.wedatasphere.linkis.httpclient.response.{HashMapHttpResult, Result}
import org.apache.http.HttpResponse


class GenericHttpClient(clientConfig: ClientConfig, clientName: String) extends AbstractHttpClient(clientConfig, clientName) {

  override protected def createDiscovery(): Discovery = throw new HttpMethodNotSupportException("GenericHttpClient not support discovery.")

  override protected def httpResponseToResult(response: HttpResponse, requestAction: HttpAction, responseBody: String): Option[Result] = {
    val result = new HashMapHttpResult
    result.set(responseBody, response.getStatusLine.getStatusCode, requestAction.getURL, response.getEntity.getContentType.getValue)
    Some(result)
  }

  override protected def getFsByUser(user: String, path: FsPath): Fs = {
    null
  }
}
