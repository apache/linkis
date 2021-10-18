package com.webank.wedatasphere.linkis.datasource.client.request

import com.webank.wedatasphere.linkis.datasource.client.exception.DataSourceClientBuilderException
import com.webank.wedatasphere.linkis.httpclient.request.DeleteAction

class DeleteDataSourceAction extends DeleteAction with DataSourceAction  {
  private var user: String = _

  private var resourceId:String=_

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  override def suffixURLs: Array[String] = Array("datasourcesmanager", "info", resourceId)
}
object DeleteDataSourceAction{
  def builder(): Builder = new Builder
  class Builder private[DeleteDataSourceAction]() {
    private var user: String = _
    private var resourceId:String=_

    def setUser(user: String): Builder ={
      this.user = user
      this
    }

    def setResourceId(resourceId:String): Builder ={
      this.resourceId = resourceId
      this
    }

    def builder():DeleteDataSourceAction={
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")
      if(resourceId == null) throw new DataSourceClientBuilderException("resourceId is needed!")

      val action = new DeleteDataSourceAction
      action.user = user
      action.resourceId = resourceId
      action
    }
  }
}