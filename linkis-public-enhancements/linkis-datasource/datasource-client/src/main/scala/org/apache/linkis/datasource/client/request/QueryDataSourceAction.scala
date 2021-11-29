package org.apache.linkis.datasource.client.request

import org.apache.linkis.datasource.client.exception.DataSourceClientBuilderException
import org.apache.linkis.httpclient.request.GetAction

class QueryDataSourceAction extends GetAction with DataSourceAction{
  override def suffixURLs: Array[String] = Array("datasource", "info")

  private var user:String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}

object QueryDataSourceAction {
  def builder(): Builder = new Builder

  class Builder private[QueryDataSourceAction]() {
    private var system:String = _
    private var name:String = _
    private var typeId:Long = _
    private var identifies:String = _
    private var currentPage:Integer = _
    private var pageSize:Integer = _
    private var user: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setSystem(system:String):Builder={
      this.system = system
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

    def setIdentifies(identifies:String):Builder={
      this.identifies = identifies
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

    def build(): QueryDataSourceAction = {
      if(system == null) throw new DataSourceClientBuilderException("system is needed!")
      if(name == null) throw new DataSourceClientBuilderException("name is needed!")
      if(typeId == null) throw new DataSourceClientBuilderException("typeId is needed!")
      if(identifies == null) throw new DataSourceClientBuilderException("identifies is needed!")
      if(currentPage == null) throw new DataSourceClientBuilderException("currentPage is needed!")
      if(pageSize == null) throw new DataSourceClientBuilderException("pageSize is needed!")
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")

      val queryDataSourceAction = new QueryDataSourceAction
      queryDataSourceAction.setParameter("system", system)
      queryDataSourceAction.setParameter("name", name)
      queryDataSourceAction.setParameter("typeId", typeId)
      queryDataSourceAction.setParameter("identifies", identifies)
      queryDataSourceAction.setParameter("currentPage", currentPage)
      queryDataSourceAction.setParameter("pageSize", pageSize)
      queryDataSourceAction.setUser(user)

      queryDataSourceAction
    }
  }
}
