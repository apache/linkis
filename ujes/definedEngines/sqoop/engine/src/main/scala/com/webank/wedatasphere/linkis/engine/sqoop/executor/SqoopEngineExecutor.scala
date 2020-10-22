package com.webank.wedatasphere.linkis.engine.sqoop.executor

import java.io.BufferedReader

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorContext}
import com.webank.wedatasphere.linkis.engine.sqoop.conf.SqoopEngineConfiguration
import com.webank.wedatasphere.linkis.engine.sqoop.exception.SqoopCodeErrorException
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.resourcemanager.{LoadInstanceResource, Resource}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, SingleTaskInfoSupport, SingleTaskOperateSupport, SuccessExecuteResponse}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

/**
 * @Classname SqoopEngineExecutor
 * @Description TODO
 * @Date 2020/8/19 18:11
 * @Created by limeng
 */
class SqoopEngineExecutor(user:String) extends EngineExecutor(SqoopEngineConfiguration.OUTPUT_LIMIT.getValue, isSupportParallelism = false) with SingleTaskOperateSupport with SingleTaskInfoSupport {
  private val name:String = Sender.getThisServiceInstance.getInstance

  override def getName: String = name

  private var process:Process = _

  override def getActualUsedResources: Resource = new LoadInstanceResource(Runtime.getRuntime.totalMemory() - Runtime.getRuntime.freeMemory(),2,1 )

  override protected def executeLine(engineExecutorContext: EngineExecutorContext, code: String): ExecuteResponse = {
    val trimCode = code.trim
    info(s"user $user begin to run code $trimCode")
    var bufferedReader:BufferedReader = null
    var errorsReader:BufferedReader = null
    try {
      val processBuilder:ProcessBuilder = new ProcessBuilder(code)
      processBuilder.redirectErrorStream(true)
      process = processBuilder.start()
      var line:String = null
      while({line = bufferedReader.readLine(); line != null}){
        info(line)
        engineExecutorContext.appendStdout(line)
      }

      val errorLog = Stream.continually(errorsReader.readLine).takeWhile(_ != null).mkString("\n")
      val exitCode = process.waitFor()
      if(StringUtils.isNotEmpty(errorLog) || exitCode != 0){
        error(s"exitCode is $exitCode")
        error(errorLog)
        engineExecutorContext.appendStdout("sqoop执行失败")
        engineExecutorContext.appendStdout(errorLog)
        ErrorExecuteResponse("run sqoop failed",SqoopCodeErrorException())
      }else{
        SuccessExecuteResponse()
      }
    }catch {
      case e:Exception => {
        error("Execute sqoop code failed, reason:", e)
        ErrorExecuteResponse("run sqoop failed" ,e)
      }
      case t:Throwable => ErrorExecuteResponse("start sqoop failed", t)
    }finally {
      IOUtils.closeQuietly(bufferedReader)
      IOUtils.closeQuietly(errorsReader)
    }


  }

  override protected def executeCompletely(engineExecutorContext: EngineExecutorContext, code: String, completedLine: String): ExecuteResponse = {
    val completeCode = code + completedLine
    executeLine(engineExecutorContext,completeCode)
  }

  override def kill(): Boolean = {
    try {
      process.destroy()
      true
    } catch {
      case e:Exception => error(s"kill process ${process.toString} failed ", e)
        false
      case t:Throwable => error(s"kill process ${process.toString} failed " ,t)
        false
    }
  }

  override def pause(): Boolean = true

  override def resume(): Boolean = true

  override def progress(): Float = 0.5f

  override def getProgressInfo: Array[JobProgressInfo] = Array()

  override def log(): String = ""

  override def close(): Unit = {

  }
}
