package com.webank.wedatasphere.linkis.engine.sqoop.conf

import com.webank.wedatasphere.linkis.engine.execute.hook._
import com.webank.wedatasphere.linkis.engine.execute.{CodeParser, EngineHook}
import com.webank.wedatasphere.linkis.engine.sqoop.codeparser.SqoopCodeParser
import org.springframework.context.annotation.{Bean, Configuration}
/**
 * @Classname SqoopEngineSpringConf
 * @Description TODO
 * @Date 2020/8/19 17:50
 * @Created by limeng
 */
@Configuration
class SqoopEngineSpringConf {
  @Bean(Array("codeParser"))
  def createCodeParser(): CodeParser = new SqoopCodeParser()


  @Bean(Array("engineHooks"))
  def createEngineHooks(): Array[EngineHook] =
    Array(new ReleaseEngineHook, new MaxExecuteNumEngineHook)

}
