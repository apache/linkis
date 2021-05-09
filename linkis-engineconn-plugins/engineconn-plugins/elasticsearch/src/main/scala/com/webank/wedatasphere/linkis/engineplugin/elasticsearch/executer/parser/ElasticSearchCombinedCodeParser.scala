package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.parser

import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.engineconn.computation.executor.parser.{CombinedEngineCodeParser, JsonCodeParser, SQLCodeParser, SingleCodeParser}

class ElasticSearchCombinedCodeParser extends CombinedEngineCodeParser {

  override val parsers: Array[SingleCodeParser] = Array(new SQLCodeParser, new JsonCodeParser)

  override def getCodeType(code: String, engineExecutionContext: EngineExecutionContext): String = {
    if (engineExecutionContext.getProperties.get("runType") != null) engineExecutionContext.getProperties.get("runType").asInstanceOf[String]
    else "esjson"
  }

}
