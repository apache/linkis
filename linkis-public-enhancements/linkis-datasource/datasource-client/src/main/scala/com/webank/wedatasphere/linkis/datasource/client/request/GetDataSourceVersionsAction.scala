package com.webank.wedatasphere.linkis.datasource.client.request

import com.webank.wedatasphere.linkis.datasource.client.exception.DataSourceClientBuilderException
import com.webank.wedatasphere.linkis.httpclient.request.GetAction

class GetDataSourceVersionsAction extends GetAction with DataSourceAction {
  private var user: String = _
  private var resourceId:String=_

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  override def suffixURLs: Array[String] = Array("datasourcesmanager", resourceId, "versions")
}
object GetDataSourceVersionsAction {
  def builder(): Builder = new Builder

  class Builder private[GetDataSourceVersionsAction]() {
    private var user: String = _
    private var resourceId:String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setResourceId(resourceId:String): Builder ={
      this.resourceId = resourceId
      this
    }

    def build(): GetDataSourceVersionsAction = {
      if(resourceId == null) throw new DataSourceClientBuilderException("resourceId is needed!")
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")

      val action = new GetDataSourceVersionsAction
      action.user = user
      action.resourceId = resourceId

      action
    }
  }
}