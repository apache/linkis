package com.webank.wedatasphere.linkis.datasource.client.request

import com.webank.wedatasphere.linkis.datasource.client.exception.DataSourceClientBuilderException
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.PutAction

import java.util
import scala.collection.JavaConversions._

class UpdateDataSourceAction extends PutAction with DataSourceAction{
  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

  private var user: String = _
  private var dataSourceId:String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  override def suffixURLs: Array[String] = Array("datasourcesmanager", "info", dataSourceId, "json")
}
object UpdateDataSourceAction {
  def builder(): Builder = new Builder

  class Builder private[UpdateDataSourceAction]() {
    private var user: String = _
    private var dataSourceId:String=_
    private var payload: util.Map[String, Any] = new util.HashMap[String, Any]

    def setUser(user:String):Builder={
      this.user = user
      this
    }

    def setDataSourceId(dataSourceId:String): Builder ={
      this.dataSourceId = dataSourceId
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

    def build(): UpdateDataSourceAction = {
      if(dataSourceId == null) throw new DataSourceClientBuilderException("dataSourceId is needed!")
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")

      val action = new UpdateDataSourceAction()
      action.dataSourceId =dataSourceId
      action.user = user

      this.payload.foreach(k=>{
        action.addRequestPayload(k._1, k._2)
      })
      action
    }
  }
}
