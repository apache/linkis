/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engine.execute

import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration
import com.webank.wedatasphere.linkis.engine.exception.EngineErrorException
import com.webank.wedatasphere.linkis.engine.extension.EnginePreExecuteHook
import com.webank.wedatasphere.linkis.resourcemanager.Resource
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils

import scala.collection.mutable.ArrayBuffer

/**
  * Created by enjoyyin on 2018/9/17.
  */
abstract class EngineExecutor(outputPrintLimit: Int, isSupportParallelism: Boolean) extends AbstractExecutor(0) with Logging {

  private val resultSetFactory = ResultSetFactory.getInstance
  private var codeParser: Option[CodeParser] = None
  private var resultSetListener: Option[ResultSetListener] = None
  private var logListener: Option[JobLogListener] = None
  private var jobProgressListener: Option[JobProgressListener] = None
  private var engineInitialized = false

  private var executedNum = 0
  private var succeedNum = 0


  private val enginePreExecuteHooks:Array[EnginePreExecuteHook] = {
    val hooks = new ArrayBuffer[EnginePreExecuteHook]()
    EngineConfiguration.ENGINE_PRE_EXECUTE_HOOK_CLASSES.getValue.split(",") foreach {
      hookStr => Utils.tryCatch{
        val clazz = Class.forName(hookStr)
        val obj = clazz.newInstance()
        obj match {
          case hook:EnginePreExecuteHook => hooks += hook
          case _ => logger.warn(s"obj is not a engineHook obj is ${obj.getClass}")
        }
      }{
        case e:Exception => logger.error("failed to load class", e)
      }
    }
    hooks.toArray
  }


  def setCodeParser(codeParser: CodeParser) = this.codeParser = Some(codeParser)
  def setResultSetListener(resultSetListener: ResultSetListener) = this.resultSetListener = Some(resultSetListener)
  def getResultSetListener = resultSetListener
  def setLogListener(logListener: JobLogListener) = this.logListener = Some(logListener)
  def getLogListener = logListener
  def setJobProgressListener(jobProgressListener: JobProgressListener) = this.jobProgressListener = Some(jobProgressListener)
  def getJobProgressListener = jobProgressListener

  def init(): Unit = {}

  final def ready(): Unit = {
    transition(ExecutorState.Idle)
    if(!engineInitialized) {
      engineInitialized = true
    }
  }

  def isEngineInitialized = engineInitialized
  def getExecutedNum = executedNum
  def getSucceedNum = succeedNum
  def getFailedNum = executedNum - succeedNum

  def getName: String

  def getActualUsedResources: Resource

  def getDefaultResultSetType: String = resultSetFactory.getResultSetType(0)
  protected def createEngineExecutorContext(executeRequest: ExecuteRequest): EngineExecutorContext = {
    val engineExecutorContext = new EngineExecutorContext(this)
    executeRequest match {
      case job: JobExecuteRequest => engineExecutorContext.setJobId(job.jobId)
      case _ =>
    }
    executeRequest match {
      case store: StorePathExecuteRequest => engineExecutorContext.setStorePath(store.storePath)
      case _ =>
    }
    engineExecutorContext
  }

  protected def incompleteSplitter = "\n"

  protected def executeLine(engineExecutorContext: EngineExecutorContext, code: String): ExecuteResponse
  protected def executeCompletely(engineExecutorContext: EngineExecutorContext, code: String, completedLine: String): ExecuteResponse

  override def execute(executeRequest: ExecuteRequest): ExecuteResponse = {
    if (StringUtils.isEmpty(executeRequest.code)){
      return IncompleteExecuteResponse("Your code is incomplete, it may be that only comments are selected for execution(您的代码不完整，可能是仅仅选中了注释进行执行)")
    }
    executedNum += 1
    def ensureOp[A](f: => A): A = if(!this.engineInitialized) f
      else if(isSupportParallelism) whenAvailable(f) else ensureIdle(f)
    ensureOp {
      val engineExecutorContext = createEngineExecutorContext(executeRequest)
      Utils.tryCatch{
        enginePreExecuteHooks foreach {
          hook => logger.info(s"${hook.hookName} begins to do a hook")
            hook.callPreExecuteHook(engineExecutorContext, executeRequest)
            logger.info(s"${hook.hookName} ends to do a hook")
        }
      }{
        case e:Exception => logger.info("failed to do with hook")
      }
      var response: ExecuteResponse = null
      val incomplete = new StringBuilder
      val codes = Utils.tryCatch(codeParser.map(_.parse(executeRequest.code, engineExecutorContext)).getOrElse(Array(executeRequest.code))){
        e => warn("Your code failed to commit one line at a time, and is now ready to execute as a full commit(您的代码在进行一行一行代码提交时失败，现在准备按照全部提交的方式进行执行)",e)
          Array(executeRequest.code)
      }
      engineExecutorContext.setTotalParagraph(codes.length)
      codes.indices.foreach { index =>
        if(engineExecutorContext.isKilled) return ErrorExecuteResponse("Job is killed by user!", null)
        val code = codes(index)
        engineExecutorContext.setCurrentParagraph(index + 1)
        response = Utils.tryCatch(if(incomplete.nonEmpty) executeCompletely(engineExecutorContext, code, incomplete.toString)
        else executeLine(engineExecutorContext, code)){ t =>
          ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(t), t)
        }
        incomplete ++= code
        //engineExecutorContext.appendStdout(getName + ">> " + incomplete.toString().trim + " complete ")
        response match {
          case e: ErrorExecuteResponse =>
            error(s"execute code $code failed!", e.t)
            return response
          case SuccessExecuteResponse() =>
            engineExecutorContext.appendStdout("\n")
            incomplete.setLength(0)
          case e: OutputExecuteResponse =>
            incomplete.setLength(0)
            val output = if(StringUtils.isNotEmpty(e.getOutput) && e.getOutput.length > outputPrintLimit)
              e.getOutput.substring(0, outputPrintLimit) else e.getOutput
            engineExecutorContext.appendStdout(output)
            if(StringUtils.isNotBlank(e.getOutput)) engineExecutorContext.sendResultSet(e)
          case _: IncompleteExecuteResponse =>
            incomplete ++= incompleteSplitter
        }
      }
      Utils.tryCatch(engineExecutorContext.close()){ t =>
        response = ErrorExecuteResponse("send resultSet to entrance failed!", t)
      }
      response match {
        case _: OutputExecuteResponse =>
          succeedNum += 1
          SuccessExecuteResponse()
        case s: SuccessExecuteResponse =>
          succeedNum += 1
          s
        case _ => response
      }
    }
  }

  private[engine] def tryShutdown(): Unit = this.ensureAvailable(transition(ExecutorState.ShuttingDown))

  private[engine] def tryDead(): Unit = this.whenState(ExecutorState.ShuttingDown, transition(ExecutorState.Dead))

  override protected def callback(): Unit = {}

//  class EngineExecutorContext {
//    private var jobId: Option[String] = None
//    private val aliasNum = new AtomicInteger(0)
//    protected var storePath: Option[String] = None
//
//    def sendResultSet(resultSetWriter: ResultSetWriter[_ <: MetaData, _ <: Record]): Unit = {
//      val fileName = new File(resultSetWriter.toFSPath.getPath).getName
//      val index = if(fileName.indexOf(".") < 0) fileName.length else fileName.indexOf(".")
//      val alias = if(fileName.startsWith("_")) fileName.substring(1, index) else fileName.substring(0, fileName.indexOf("_"))
//      resultSetWriter.flush()
//      Utils.tryFinally(sendResultSet(resultSetWriter.toString(), alias))(IOUtils.closeQuietly(resultSetWriter))
//    }
//
//    def sendResultSet(output: String): Unit = sendResultSet(output, "_" + aliasNum.getAndIncrement())
//
//    private def sendResultSet(output: String, alias: String): Unit =
//      if(resultSetFactory.isResultSetPath(output))
//        resultSetListener.foreach(l => jobId.foreach(l.onResultSetCreated(_, output, alias)))
//      else if(resultSetFactory.isResultSet(output))
//        resultSetListener.foreach(l => jobId.foreach(l.onResultSetCreated(_, output, alias)))
//      else throw new EngineErrorException(50050, "unknown resultSet: " + output)
//
//    def setJobId(jobId: String) = this.jobId = Option(jobId)
//    def getJobId = jobId
//    def setStorePath(storePath: String) = this.storePath = Option(storePath)
//
//    def sendResultSet(outputExecuteResponse: OutputExecuteResponse): Unit = outputExecuteResponse match {
//      case AliasOutputExecuteResponse(alias, output) => sendResultSet(output, alias)
//      case output: OutputExecuteResponse => sendResultSet(output.getOutput, "_" + aliasNum.getAndIncrement())
//    }
//
//    protected def getDefaultStorePath: String = {
//      val path = ENGINE_RESULT_SET_STORE_PATH.getValue
//      (if(path.endsWith("/")) path else path + "/") + "user" + "/" +
//        DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd") + "/" + Sender.getThisModuleInstance.getApplicationName +
//        "/" + Sender.getThisModuleInstance.getInstance + "/" + System.nanoTime
//    }
//
//    def createDefaultResultSetWriter(): ResultSetWriter[_ <: MetaData, _ <: Record] = createResultSetWriter(resultSetFactory.getResultSetByType(getDefaultResultSetType))
//
//    def createDefaultResultSetWriter(alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
//      createResultSetWriter(resultSetFactory.getResultSetByType(getDefaultResultSetType), alias)
//
//    def createResultSetWriter(resultSetType: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
//      createResultSetWriter(resultSetFactory.getResultSetByType(resultSetType), null)
//
//    def createResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record]): ResultSetWriter[_ <: MetaData, _ <: Record] =
//      createResultSetWriter(resultSet, null)
//
//    def createResultSetWriter(resultSetType: String, alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
//      createResultSetWriter(resultSetFactory.getResultSetByType(resultSetType), alias)
//
//    def createResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record], alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] = {
//      val filePath = storePath.getOrElse(getDefaultStorePath)
//      val fileName = if(StringUtils.isEmpty(alias)) "_" + aliasNum.getAndIncrement() else alias + "_" + aliasNum.getAndIncrement()
//      val resultSetPath = resultSet.getResultSetPath(new FsPath(filePath), fileName)
//      val resultSetWriter = ResultSetWriter.getResultSetWriter(resultSet, ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath)
//      resultSetWriter
//    }
//
//    def appendStdout(log: String): Unit = if(!engineInitialized)
//      info(log) else logListener.foreach(ll => jobId.foreach(ll.onLogUpdate(_, log)))
//  }
}