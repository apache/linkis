package com.webank.wedatasphere.linkis.datasource.client.request

import com.webank.wedatasphere.linkis.datasource.client.exception.DataSourceClientBuilderException
import com.webank.wedatasphere.linkis.httpclient.request.GetAction

class QueryDataSourceEnvAction extends GetAction with DataSourceAction{
  override def suffixURLs: Array[String] = Array("datasource", "env")

  private var user:String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}


object QueryDataSourceEnvAction {
  def builder(): Builder = new Builder

  class Builder private[QueryDataSourceEnvAction]() {
    private var name:String = _
    private var typeId:Long = _
    private var currentPage:Integer = _
    private var pageSize:Integer = _
    private var user: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setName(name:String):Builder={
      this.name = name
      this
    }

    def setTypeId(typeId:Long):Builder={
      this.typeId = typeId
      this
    }

    def setCurrentPage(currentPage:Integer):Builder={
      this.currentPage = currentPage
      this
    }

    def setPageSize(pageSize:Integer):Builder={
      this.pageSize = pageSize
      this
    }

    def build(): QueryDataSourceEnvAction = {
      if(name == null) throw new DataSourceClientBuilderException("name is needed!")
      if(typeId == null) throw new DataSourceClientBuilderException("typeId is needed!")
      if(currentPage == null) throw new DataSourceClientBuilderException("currentPage is needed!")
      if(pageSize == null) throw new DataSourceClientBuilderException("pageSize is needed!")
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")


      val queryDataSourceEnvAction = new QueryDataSourceEnvAction
      queryDataSourceEnvAction.setParameter("name", name)
      queryDataSourceEnvAction.setParameter("typeId", typeId)
      queryDataSourceEnvAction.setParameter("currentPage", currentPage)
      queryDataSourceEnvAction.setParameter("pageSize", pageSize)
      queryDataSourceEnvAction.setUser(user)

      queryDataSourceEnvAction
    }
  }
}