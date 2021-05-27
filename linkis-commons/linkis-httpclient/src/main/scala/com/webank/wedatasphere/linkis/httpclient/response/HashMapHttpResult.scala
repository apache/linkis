package com.webank.wedatasphere.linkis.httpclient.response

import java.util

import com.webank.wedatasphere.linkis.common.utils.JsonUtils
import com.webank.wedatasphere.linkis.httpclient.exception.HttpClientResultException


class HashMapHttpResult extends HttpResult {

  private var resultMap: util.Map[String, Object] = _
  private var responseBody: String = _
  private var statusCode: Int = _
  private var url: String = _
  private var contentType: String = _

  override def getContentType: String = contentType

  override def getUri: String = url

  override def getStatusCode: Int = statusCode

  def getResultMap: util.Map[String, Object] = resultMap

  override def set(responseBody: String, statusCode: Int, url: String, contentType: String): Unit = {
    if(statusCode != 200) throw new HttpClientResultException(s"URL $url request failed! ResponseBody is $responseBody." )
    resultMap = JsonUtils.jackson.readValue(responseBody, classOf[util.Map[String, Object]])
    this.responseBody = responseBody
    this.statusCode = statusCode
    this.url = url
    this.contentType = contentType
  }

  override def getResponseBody: String = responseBody
}
