package org.apache.linkis.datasource.client.request

import org.apache.linkis.datasource.client.config.DatasourceClientConfig.METADATA_SERVICE_MODULE
import org.apache.linkis.datasource.client.exception.DataSourceClientBuilderException
import org.apache.linkis.httpclient.request.GetAction


class MetadataGetTablesAction extends GetAction with DataSourceAction {
  private var dataSourceId: Long = _
  private var database: String = _

  override def suffixURLs: Array[String] = Array(METADATA_SERVICE_MODULE.getValue, "tables", dataSourceId.toString, "db", database)

  private var user:String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}

object MetadataGetTablesAction {
  def builder(): Builder = new Builder

  class Builder private[MetadataGetTablesAction]() {
    private var dataSourceId: Long = _
    private var database: String = _
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

    def setDatabase(database: String): Builder = {
      this.database = database
      this
    }

    def setSystem(system: String): Builder = {
      this.system = system
      this
    }

    def build(): MetadataGetTablesAction = {
      if(dataSourceId == null) throw new DataSourceClientBuilderException("dataSourceId is needed!")
      if(database == null) throw new DataSourceClientBuilderException("database is needed!")
      if(system == null) throw new DataSourceClientBuilderException("system is needed!")
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")

      val metadataGetTablesAction = new MetadataGetTablesAction
      metadataGetTablesAction.dataSourceId = this.dataSourceId
      metadataGetTablesAction.database = this.database
      metadataGetTablesAction.setParameter("system", system)
      metadataGetTablesAction.setUser(user)
      metadataGetTablesAction
    }
  }

}
