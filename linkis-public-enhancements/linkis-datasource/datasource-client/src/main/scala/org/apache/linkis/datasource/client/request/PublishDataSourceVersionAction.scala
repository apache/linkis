package org.apache.linkis.datasource.client.request

import org.apache.linkis.datasource.client.config.DatasourceClientConfig.DATA_SOURCE_SERVICE_MODULE
import org.apache.linkis.datasource.client.exception.DataSourceClientBuilderException
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.request.POSTAction


class PublishDataSourceVersionAction extends POSTAction with DataSourceAction{
  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)
  private var user: String = _
  private var dataSourceId:String=_
  private var versionId:String=_

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  override def suffixURLs: Array[String] = Array(DATA_SOURCE_SERVICE_MODULE.getValue, "publish", dataSourceId, versionId)
}
object PublishDataSourceVersionAction {
  def builder(): Builder = new Builder

  class Builder private[PublishDataSourceVersionAction]() {
    private var user: String = _
    private var dataSourceId:String=_
    private var versionId:String=_

    def setUser(user:String):Builder={
      this.user = user
      this
    }

    def setDataSourceId(dataSourceId:String): Builder ={
      this.dataSourceId = dataSourceId
      this
    }

    def setVersion(versionId:String): Builder ={
      this.versionId = versionId
      this
    }

    def build(): PublishDataSourceVersionAction = {
      if(dataSourceId == null) throw new DataSourceClientBuilderException("dataSourceId is needed!")
      if(versionId == null) throw new DataSourceClientBuilderException("versionId is needed!")
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")

      val action = new PublishDataSourceVersionAction()
      action.dataSourceId =dataSourceId
      action.versionId = versionId
      action.user = user

      action
    }
  }
}