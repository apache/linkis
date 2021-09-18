package com.webank.wedatasphere.linkis.datasource.client.request

import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.POSTAction

import java.util
import scala.collection.JavaConversions._

class CreateDataSourceAction extends POSTAction with DataSourceAction{
  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

  private var user: String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  override def suffixURLs: Array[String] = Array("datasource", "info", "json")
}
object CreateDataSourceAction {
  def builder(): Builder = new Builder

  class Builder private[CreateDataSourceAction]() {
    private var user: String = _
    private var payload: util.Map[String, Any] = new util.HashMap[String, Any]()

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def addRequestPayload(key: String, value: Any): Builder = {
      if(value != null) this.payload.put(key, value)
      this
    }

    def addRequestPayloads(map: util.Map[String, Any]): Builder = {
      this.synchronized(this.payload = map)
      this
    }

    def build(): CreateDataSourceAction = {
      val action = new CreateDataSourceAction
      action.setUser(user)
      this.payload.foreach(k=>{
        action.addRequestPayload(k._1, k._2)
      })
      action
    }
  }
}