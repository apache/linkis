package com.webank.wedatasphere.linkis.datasource.client.request

import com.webank.wedatasphere.linkis.httpclient.request.GetAction

class GetAllDataSourceTypesAction extends GetAction with DataSourceAction {
  override def suffixURLs: Array[String] = Array("datasource", "type", "all")
}

object GetAllDataSourceTypesAction {
  def builder(): Builder = new Builder

  class Builder private[GetAllDataSourceTypesAction]() {
    private var user: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def build(): GetAllDataSourceTypesAction = {
      val action = new GetAllDataSourceTypesAction
      action.setUser(user)

      action
    }
  }
}
