package com.webank.wedatasphere.linkis.datasource.client.request

import com.webank.wedatasphere.linkis.datasource.client.config.DatasourceClientConfig._
import com.webank.wedatasphere.linkis.datasource.client.exception.DataSourceClientBuilderException
import com.webank.wedatasphere.linkis.httpclient.request.GetAction

class GetMetadataSourceAllSizeAction extends GetAction with DataSourceAction{

  private var user:String = _
  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  override def suffixURLs: Array[String] = Array(LINKIS_METADATA_SERVICE_MODULE.getValue, "all", "size")
}
object GetMetadataSourceAllSizeAction{
  def builder(): Builder = new Builder

  class Builder private[GetMetadataSourceAllSizeAction]() {
    private var user: String = _
    private var database:String=_
    private var table:String=_

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setDatabase(database:String): Builder ={
      this.database = database
      this
    }

    def setTable(table:String):Builder={
      this.table = table
      this
    }

    def build(): GetMetadataSourceAllSizeAction = {
      val action = new GetMetadataSourceAllSizeAction
      action.setUser(user)
      if(database == null) throw new DataSourceClientBuilderException("database is needed!")
      if(table == null) throw new DataSourceClientBuilderException("table is needed!")
      action.setParameter("database", this.database)
      action.setParameter("table", this.table)
      action
    }
  }
}
