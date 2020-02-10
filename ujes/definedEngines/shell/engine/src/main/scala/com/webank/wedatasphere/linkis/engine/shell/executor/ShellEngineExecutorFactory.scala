package com.webank.wedatasphere.linkis.engine.shell.executor

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorFactory}
import com.webank.wedatasphere.linkis.server.JMap
import com.webank.wedatasphere.linkis.engine.shell.exception.NoCorrectUserException
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
  * created by cooperyang on 2019/5/14
  * Description:
  */
@Component
class ShellEngineExecutorFactory extends EngineExecutorFactory with Logging{
  override def createExecutor(options: JMap[String, String]): EngineExecutor = {
    //todo 可能会有一些设置环境变量的操作
    import scala.collection.JavaConverters._
    options.asScala foreach {
      case (k, v) => info(s"key is $k, value is $v")
    }
    val user:String = System.getProperty("user.name")
    if (StringUtils.isEmpty(user)) throw NoCorrectUserException()

    new ShellEngineExecutor(user)
  }
}
