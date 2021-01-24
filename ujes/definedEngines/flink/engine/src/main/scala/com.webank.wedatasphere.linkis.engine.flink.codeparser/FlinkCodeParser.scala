package com.webank.wedatasphere.linkis.engine.flink.codeparser


import com.webank.wedatasphere.linkis.engine.execute.CodeType.CodeType
import com.webank.wedatasphere.linkis.engine.execute.{CodeType, EngineExecutorContext, SingleCodeParser}
import org.apache.commons.lang.StringUtils

import scala.collection.mutable.ArrayBuffer


 class FlinkCodeParser extends SingleCodeParser{

  override val codeType: CodeType = CodeType.Flink
  val separator = ";"

  override def parse(code: String, engineExecutorContext: EngineExecutorContext): Array[String] = {
    val codeBuffer = new ArrayBuffer[String]()
    def appendStatement(sqlStatement: String): Unit ={
      codeBuffer.append(sqlStatement)
    }
    if (StringUtils.contains(code, separator)) StringUtils.split(code, ";").foreach{
      case s if StringUtils.isBlank(s) =>
      case s => appendStatement(s);
    } else code match {
      case s if StringUtils.isBlank(s) =>
      case s => appendStatement(s);
    }
    codeBuffer.toArray
  }

}
