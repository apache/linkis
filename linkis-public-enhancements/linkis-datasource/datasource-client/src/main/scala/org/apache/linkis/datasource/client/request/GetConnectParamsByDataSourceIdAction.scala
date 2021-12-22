package org.apache.linkis.datasource.client.request

import org.apache.linkis.datasource.client.config.DatasourceClientConfig.DATA_SOURCE_SERVICE_MODULE
import org.apache.linkis.datasource.client.exception.DataSourceClientBuilderException
import org.apache.linkis.httpclient.request.GetAction


class GetConnectParamsByDataSourceIdAction extends GetAction with DataSourceAction {
  private var dataSourceId: Long = _

  override def suffixURLs: Array[String] = Array(DATA_SOURCE_SERVICE_MODULE.getValue, dataSourceId.toString, "connect_params")

  private var user:String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}


object GetConnectParamsByDataSourceIdAction {
  def builder(): Builder = new Builder

  class Builder private[GetConnectParamsByDataSourceIdAction]() {
    private var dataSourceId: Long = _
    private var system:String = _
    private var user: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setDataSourceId(dataSourceId: Long): Builder = {
      this.dataSourceId = dataSourceId
      this
    }

    def setSystem(system: String): Builder = {
      this.system = system
      this
    }

    def build(): GetConnectParamsByDataSourceIdAction = {
      if(dataSourceId == null) throw new DataSourceClientBuilderException("dataSourceId is needed!")
      if(system == null) throw new DataSourceClientBuilderException("system is needed!")
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")

      val getConnectParamsByDataSourceIdAction = new GetConnectParamsByDataSourceIdAction
      getConnectParamsByDataSourceIdAction.dataSourceId = this.dataSourceId
      getConnectParamsByDataSourceIdAction.setParameter("system", system)
      getConnectParamsByDataSourceIdAction.setUser(user)
      getConnectParamsByDataSourceIdAction
    }
  }

}

