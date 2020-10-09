package com.webank.wedatasphere.linkis.engine.sqoop.codeparser

import com.webank.wedatasphere.linkis.engine.execute.CodeType.CodeType
import com.webank.wedatasphere.linkis.engine.execute.{CodeType, EngineExecutorContext, SingleCodeParser}

/**
 * @Classname SqoopCodeParser
 * @Description TODO
 * @Date 2020/8/19 17:13
 * @Created by limeng
 */
class SqoopCodeParser extends SingleCodeParser {
  override val codeType: CodeType = CodeType.Sqoop

  override def parse(code: String, engineExecutorContext: EngineExecutorContext): Array[String] = {
    Array(code)
  }
}
