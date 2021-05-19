package com.webank.wedatasphere.linkis.datasource.client.request

import com.webank.wedatasphere.linkis.datasource.client.exception.DataSourceClientBuilderException
import com.webank.wedatasphere.linkis.httpclient.request.GetAction

class GetKeyDefinitionsByTypeAction  extends GetAction with DataSourceAction{
  private var typeId: Long = _

  override def suffixURLs: Array[String] = Array("datasource", "key_define", "type", typeId.toString)
}


object GetKeyDefinitionsByTypeAction {
  def builder(): Builder = new Builder

  class Builder private[GetKeyDefinitionsByTypeAction]() {
    private var typeId: Long = _

    def setTypeId(typeId: Long): Builder = {
      this.typeId = typeId
      this
    }

    def build(): GetKeyDefinitionsByTypeAction = {
      if(typeId == null) throw new DataSourceClientBuilderException("typeId is needed!")

      val getKeyDefinitionsByTypeAction = new GetKeyDefinitionsByTypeAction
      getKeyDefinitionsByTypeAction.typeId = this.typeId
      getKeyDefinitionsByTypeAction
    }
  }

}