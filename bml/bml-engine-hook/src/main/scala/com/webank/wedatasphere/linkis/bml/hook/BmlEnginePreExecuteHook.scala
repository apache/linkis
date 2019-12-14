package com.webank.wedatasphere.linkis.bml.hook

import java.io.File
import java.util

import com.webank.wedatasphere.linkis.bml.client.{BmlClient, BmlClientFactory}
import com.webank.wedatasphere.linkis.bml.exception.BmlHookDownloadException
import com.webank.wedatasphere.linkis.bml.utils.BmlHookUtils
import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engine.ResourceExecuteRequest
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.extension.EnginePreExecuteHook
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._
/**
  * created by cooperyang on 2019/9/23
  * Description:
  */
class BmlEnginePreExecuteHook extends EnginePreExecuteHook with Logging{
  override val hookName: String = "BmlEnginePreExecuteHook"

  val RESOURCES_STR = "resources"

  val RESOURCE_ID_STR = "resourceId"

  val VERSION_STR = "version"

  val FILE_NAME_STR = "fileName"

  val processUser:String = System.getProperty("user.name")

  val defaultUser:String = "hadoop"

  val bmlClient:BmlClient = if (StringUtils.isNotEmpty(processUser))
    BmlClientFactory.createBmlClient(processUser) else BmlClientFactory.createBmlClient(defaultUser)

  val seperator:String = File.separator

  val pathType:String = "file://"

  override def callPreExecuteHook(engineExecutorContext: EngineExecutorContext, executeRequest: ExecuteRequest): Unit = {
    //1.删除工作目录以前的资源文件
    //2.下载资源到当前进程的工作目录

    val workDir = BmlHookUtils.getCurrentWorkDir
    val jobId = engineExecutorContext.getJobId
    executeRequest match {
      case resourceExecuteRequest:ResourceExecuteRequest => val resources = resourceExecuteRequest.resources
        resources foreach {
          case resource:util.Map[String, Object] => val fileName = resource.get(FILE_NAME_STR).toString
            val resourceId = resource.get(RESOURCE_ID_STR).toString
            val version = resource.get(VERSION_STR).toString
            val fullPath = if (workDir.endsWith(seperator)) pathType + workDir + fileName else
              pathType + workDir + seperator + fileName
            val response = Utils.tryCatch{
              bmlClient.downloadResource(processUser, resourceId, version, fullPath, true)
            }{
              case error:ErrorException => logger.error("download resource for {} failed", error)
                throw error
              case t:Throwable => logger.error(s"download resource for $jobId failed", t)
                val e1 = BmlHookDownloadException(t.getMessage)
                e1.initCause(t)
                throw t
            }
            if (response.isSuccess){
              logger.info(s"for job $jobId resourceId $resourceId version $version download to path $fullPath ok")
            }else{
              logger.warn(s"for job $jobId resourceId $resourceId version $version download to path $fullPath Failed")
            }
          case _ => logger.warn("job resource cannot download")
        }
      case _ =>
    }
  }
}
