package com.webank.wedatasphere.linkis.httpclient.response.impl

import com.webank.wedatasphere.linkis.httpclient.response.HttpResult

class DefaultHttpResult extends HttpResult {

  var responseBody: String = _
  var statusCode: Int = _
  var uri: String = _
  var contentType: String = _

  override def getContentType: String = contentType

  override def getUri: String = uri

  override def getStatusCode: Int = statusCode

  override def set(responseBody: String, statusCode: Int, url: String, contentType: String): Unit = {
    this.responseBody = responseBody
    this.statusCode = statusCode
    this.uri = url
    this.contentType = contentType
  }

  override def getResponseBody: String = responseBody
}
