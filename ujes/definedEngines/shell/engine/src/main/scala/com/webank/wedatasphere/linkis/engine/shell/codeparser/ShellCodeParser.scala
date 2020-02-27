package com.webank.wedatasphere.linkis.engine.shell.codeparser

import com.webank.wedatasphere.linkis.engine.execute.CodeType.CodeType
import com.webank.wedatasphere.linkis.engine.execute.{CodeType, EngineExecutorContext, SingleCodeParser}

/**
  * created by cooperyang on 2019/5/16
  * Description: shell的代码解析器，shell代码不能进行切分，因为shell代码需要进入到
  */
class ShellCodeParser extends SingleCodeParser{
  override val codeType: CodeType = CodeType.Shell

  override def parse(code: String, engineExecutorContext: EngineExecutorContext): Array[String] = {
    Array(code)
  }

}
