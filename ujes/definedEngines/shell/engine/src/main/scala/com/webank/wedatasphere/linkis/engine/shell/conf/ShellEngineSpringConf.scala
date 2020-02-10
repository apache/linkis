package com.webank.wedatasphere.linkis.engine.shell.conf

import com.webank.wedatasphere.linkis.engine.execute.hook._
import com.webank.wedatasphere.linkis.engine.execute.{CodeParser, EngineHook}
import com.webank.wedatasphere.linkis.engine.shell.codeparser.ShellCodeParser
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * created by cooperyang on 2019/5/17
  * Description:
  */
@Configuration
class ShellEngineSpringConf {
  @Bean(Array("codeParser"))
  def createCodeParser(): CodeParser = new ShellCodeParser()


  @Bean(Array("engineHooks"))
  def createEngineHooks(): Array[EngineHook] =
    Array(new ReleaseEngineHook, new MaxExecuteNumEngineHook)
}
