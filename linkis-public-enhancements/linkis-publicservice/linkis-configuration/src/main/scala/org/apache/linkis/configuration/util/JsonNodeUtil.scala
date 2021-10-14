package org.apache.linkis.configuration.util

import com.fasterxml.jackson.databind.JsonNode

object JsonNodeUtil {

  def getStringValue(node: JsonNode): String = {
    if(node == null || node.asText().toLowerCase().equals("null")){
      null
    }else{
      node.asText()
    }
  }

}