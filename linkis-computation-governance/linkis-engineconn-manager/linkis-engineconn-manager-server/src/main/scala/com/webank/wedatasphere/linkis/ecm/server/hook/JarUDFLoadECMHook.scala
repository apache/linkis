package com.webank.wedatasphere.linkis.ecm.server.hook
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.ecm.core.engineconn.EngineConn
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process.{LaunchConstants, ProcessEngineConnLaunchRequest}
import com.webank.wedatasphere.linkis.udf.UDFClient
import org.apache.commons.lang.StringUtils

class JarUDFLoadECMHook extends ECMHook with Logging {

  override def beforeLaunch(request: EngineConnLaunchRequest, conn: EngineConn): Unit = {
    request match {
      case pel: ProcessEngineConnLaunchRequest =>
        info("start loading UDFs")
        val udfInfos = UDFClient.getUdfInfos(request.user).filter{ info => info.getUdfType == 0 && info.getExpire == false && StringUtils.isNotBlank(info.getPath) && info.getLoad == true }
        udfInfos.foreach{ udfInfo =>
          LaunchConstants.addPathToClassPath(pel.environment, udfInfo.getPath)
        }
    }
  }

  override def afterLaunch(conn: EngineConn): Unit = {

  }

  override def getName: String = "JarUDFLoadECMHook"
}
