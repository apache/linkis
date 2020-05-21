package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.entrance.executor.EsEngineExecutor
import com.webank.wedatasphere.linkis.entrance.executor.esclient.{EsClient, EsClientFactory}
import com.webank.wedatasphere.linkis.entrance.executor.impl.EsEngineExecutorImpl
import com.webank.wedatasphere.linkis.entrance.persistence.EntranceResultSetEngine
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.engine.{JobProgressInfo, RequestTask}
import com.webank.wedatasphere.linkis.scheduler.executer.{AliasOutputExecuteResponse, ErrorExecuteResponse, ExecuteRequest, ExecuteResponse, IncompleteExecuteResponse, SingleTaskInfoSupport, SingleTaskOperateSupport, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.server.JMap
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils

/**
 *
 * @author wang_zh
 * @date 2020/5/11
 */
class EsEntranceEngine(id: Long, properties: JMap[String, String], resourceRelease: () => Unit) extends EntranceEngine(id) with SingleTaskOperateSupport with SingleTaskInfoSupport {

  private var client: EsClient = _
  private var engineExecutor: EsEngineExecutor = _
  private var runType: String = _
  private var storePath: String = _
  private val persistEngine = new EntranceResultSetEngine()
  private var totalCodeLineNumber: Int = 0
  private var codeLine = 0


  override def getModuleInstance: ServiceInstance = ServiceInstance("EsEntranceEngine", "")

  private var job: EntranceJob = _
  def setJob(job: EntranceJob) = this.job = job

  override def execute(executeRequest: ExecuteRequest): ExecuteResponse =  {
    if (StringUtils.isEmpty(executeRequest.code)) {
      return IncompleteExecuteResponse("execute codes can not be empty)")
    }

    this.storePath = executeRequest match {
      case storePathExecuteRequest: StorePathExecuteRequest => storePathExecuteRequest.storePath
      case _ => ""
    }

    val codes = this.engineExecutor.parse(executeRequest.code)
    if (!codes.isEmpty) {
      totalCodeLineNumber = codes.length
      codeLine = 0
      codes.foreach { code =>
        try {
          val executeRes = executeLine(code)
          executeRes match {
            case aliasOutputExecuteResponse: AliasOutputExecuteResponse =>
              persistEngine.persistResultSet(job, aliasOutputExecuteResponse)
            case SuccessExecuteResponse() =>
              info(s"execute execute successfully : ${code}")
            case incompleteResponse: IncompleteExecuteResponse =>
              warn(s"execute execute failed, code: ${code}, msg: ${incompleteResponse.message}")
              val msg = if(StringUtils.isNotEmpty(incompleteResponse.message)) incompleteResponse.message else "incomplete code."
              job.getLogListener.foreach(_.onLogUpdate(job,  LogUtils.generateWarn( s"execute incomplete code, code: ${code}, msg: ${msg}")))
            case errorResponse: ErrorExecuteResponse =>
              error(s"execute code $code failed!", errorResponse.t)
              job.getLogListener.foreach(_.onLogUpdate(job,  LogUtils.generateERROR( s"execute code $code failed!" + ExceptionUtils.getFullStackTrace(errorResponse.t))))
              return errorResponse
            case _ =>
              warn("no matching exception")
              job.getLogListener.foreach(_.onLogUpdate(job,  LogUtils.generateERROR( s"execute code $code failed! no matching exception")))
              return ErrorExecuteResponse("no matching exception", null)
          }
          codeLine = codeLine + 1
          // update progress
          job.getProgressListener.map(_.onProgressUpdate(job, progress, getProgressInfo))
        } catch {
          case t: Throwable =>
            return ErrorExecuteResponse("EsEntranceEngine execute exception. ", t)
        } finally {

        }
      }
    }
    this.close()
    SuccessExecuteResponse()
  }

  protected def executeLine(code: String): ExecuteResponse = this.engineExecutor.executeLine(code, storePath, codeLine.toString)

  override protected def callExecute(request: RequestTask): EngineExecuteAsynReturn = ???

  def init(): Unit = {
    this.client = EsClientFactory.getRestClient(properties)
    this.runType = this.properties.getOrDefault(TaskConstant.RUNTYPE, "esjson")
    this.engineExecutor = new EsEngineExecutorImpl(this.runType, this.client, properties)
    this.engineExecutor.open
  }


  override def progress(): Float = if (totalCodeLineNumber != 0) {
    codeLine / totalCodeLineNumber.asInstanceOf[Float]
  } else {
    0.0f
  }

  override def getProgressInfo: Array[JobProgressInfo] =  Array.empty[JobProgressInfo]

  override def log(): String = "Es Engine is running"

  override def kill(): Boolean = {
    this.close()
    true
  }

  override def pause(): Boolean = ???

  override def resume(): Boolean = ???


  override def toString: String = s"EsEntranceEngine($id)"

  // used by EsEngineManager to correct EntranceEngine used resources(用于 EsEngineManager 修正 Engine 使用的资源)
  @volatile var isClose = false

  override def close(): Unit = {
    try {
      this.job.setResultSize(0)
      this.engineExecutor.close
      // 释放资源
      resourceRelease()
    } catch {
      case _: Throwable =>
    } finally {
      this.isClose = true
    }
  }

}
