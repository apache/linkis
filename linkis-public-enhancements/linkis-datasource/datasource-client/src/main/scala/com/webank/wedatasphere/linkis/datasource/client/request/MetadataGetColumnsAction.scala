package com.webank.wedatasphere.linkis.datasource.client.request

import com.webank.wedatasphere.linkis.datasource.client.exception.DataSourceClientBuilderException
import com.webank.wedatasphere.linkis.httpclient.request.GetAction

class MetadataGetColumnsAction extends GetAction with DataSourceAction {
  private var dataSourceId: Long = _
  private var database: String = _
  private var table: String = _

  override def suffixURLs: Array[String] = Array("metadata", "columns", dataSourceId.toString, "db", database, "table", table)

  private var user:String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}


object MetadataGetColumnsAction {
  def builder(): Builder = new Builder

  class Builder private[MetadataGetColumnsAction]() {
    private var dataSourceId: Long = _
    private var database: String = _
    private var table: String = _
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

    def setTable(table: String): Builder = {
      this.table = table
      this
    }

    def setSystem(system: String): Builder = {
      this.system = system
      this
    }

    def build(): MetadataGetColumnsAction = {
      if(dataSourceId == null) throw new DataSourceClientBuilderException("dataSourceId is needed!")
      if(database == null) throw new DataSourceClientBuilderException("database is needed!")
      if(table == null) throw new DataSourceClientBuilderException("table is needed!")
      if(system == null) throw new DataSourceClientBuilderException("system is needed!")
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")


      val metadataGetColumnsAction = new MetadataGetColumnsAction
      metadataGetColumnsAction.dataSourceId = this.dataSourceId
      metadataGetColumnsAction.database = this.database
      metadataGetColumnsAction.table = this.table
      metadataGetColumnsAction.setParameter("system", system)
      metadataGetColumnsAction.setUser(user)
      metadataGetColumnsAction
    }
  }

}
