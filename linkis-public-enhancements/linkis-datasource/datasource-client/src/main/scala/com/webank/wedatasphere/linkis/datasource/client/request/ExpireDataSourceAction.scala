package com.webank.wedatasphere.linkis.datasource.client.request


import com.webank.wedatasphere.linkis.datasource.client.exception.DataSourceClientBuilderException
import com.webank.wedatasphere.linkis.httpclient.request.PutAction

class ExpireDataSourceAction extends PutAction with DataSourceAction {
  override def getRequestPayload: String = ""

  private var user: String = _

  private var dataSourceId:String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  override def suffixURLs: Array[String] = Array("datasourcesmanager", "info",dataSourceId, "expire")
}
object ExpireDataSourceAction{
  def builder(): Builder = new Builder
  class Builder private[ExpireDataSourceAction]() {
    private var user: String = _
    private var dataSourceId:String = _

    def setUser(user:String):Builder={
      this.user = user
      this
    }

    def setDataSourceId(dataSourceId:String): Builder ={
      this.dataSourceId = dataSourceId
      this
    }
    def build(): ExpireDataSourceAction = {
      if(dataSourceId == null) throw new DataSourceClientBuilderException("dataSourceId is needed!")
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")

      val action = new ExpireDataSourceAction()
      action.dataSourceId =dataSourceId
      action.user = user
      action
    }
  }
}
