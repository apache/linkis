package org.apache.linkis.datasource.client.request

import org.apache.linkis.httpclient.request.GetAction

class GetAllDataSourceTypesAction extends GetAction with DataSourceAction {
  override def suffixURLs: Array[String] = Array("datasource", "type", "all")

  private var user:String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
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
