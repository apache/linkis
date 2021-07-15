package com.webank.wedatasphere.linkis.configuration.util

import org.codehaus.jackson.JsonNode

object JsonNodeUtil {

  def getStringValue(node: JsonNode): String = {
    if(node == null || node.asText().toLowerCase().equals("null")){
      null
    }else{
      node.asText()
    }
  }

}