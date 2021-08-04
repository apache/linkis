package com.webank.wedatasphere.linkis.computation.client.once.action

import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.POSTAction
import com.webank.wedatasphere.linkis.ujes.client.exception.UJESJobException

import java.util

/**
  * Created by enjoyyin on 2021/6/8.
  */
class CreateEngineConnAction extends POSTAction with LinkisManagerAction {

  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

  override def suffixURLs: Array[String] = Array("linkisManager", "createEngineConn")

}
object CreateEngineConnAction {

  def newBuilder(): Builder = new Builder

  class Builder private[CreateEngineConnAction]() {
    private var user: String = _
    private var properties: util.Map[String, String] = _
    private var labels: util.Map[String, String] = _
    private var maxSubmitTime: Long = _
    private var createService: String = _
    private var description: String = _
    private var ignoreTimeout: Boolean = false

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setProperties(properties: util.Map[String, String]): Builder = {
      this.properties = properties
      this
    }

    def setLabels(labels: java.util.Map[String, String]): Builder = {
      this.labels = labels
      this
    }

    def setMaxSubmitTime(maxSubmitTime: Long): Builder = {
      this.maxSubmitTime = maxSubmitTime
      this
    }

    def setCreateService(createService: String): Builder = {
      this.createService = createService
      this
    }

    def setDescription(description: String): Builder = {
      this.description = description
      this
    }

    def setIgnoreTimeout(ignoreTimeout: Boolean): Builder = {
      this.ignoreTimeout = ignoreTimeout
      this
    }

    def build(): CreateEngineConnAction = {
      val action = new CreateEngineConnAction()
      if (user == null) throw new UJESJobException("user is needed!")
      if (properties == null) properties = new java.util.HashMap[String, String]
      if (labels == null) throw new UJESJobException("labels is needed!")
      action.setUser(user)
      action.addRequestPayload("properties", properties)
      action.addRequestPayload("labels", labels)
      action.addRequestPayload("createService", createService)
      action.addRequestPayload("timeOut", maxSubmitTime)
      action.addRequestPayload("description", description)
      action.addRequestPayload("ignoreTimeout", ignoreTimeout)
      action
    }

  }

}