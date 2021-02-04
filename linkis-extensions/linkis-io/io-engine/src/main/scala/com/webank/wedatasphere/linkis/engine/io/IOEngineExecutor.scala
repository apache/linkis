package com.webank.wedatasphere.linkis.engine.io

import java.util
import java.util.concurrent.atomic.AtomicLong

import com.webank.wedatasphere.linkis.common.io.{Fs, FsPath}
import com.webank.wedatasphere.linkis.common.utils.{Logging, OverloadUtils, Utils}
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorContext}
import com.webank.wedatasphere.linkis.engine.io.domain.FSInfo
import com.webank.wedatasphere.linkis.engine.io.service.FsProxyService
import com.webank.wedatasphere.linkis.engine.io.utils.{IOEngineConfiguration, IOHelp, ReflectionUtils}
import com.webank.wedatasphere.linkis.protocol.engine.EngineState
import com.webank.wedatasphere.linkis.resourcemanager.{LoadResource, Resource}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.{AliasOutputExecuteResponse, ConcurrentTaskOperateSupport, ExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.domain.{MethodEntity, MethodEntitySerializer}
import com.webank.wedatasphere.linkis.storage.exception.StorageErrorException
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import org.apache.commons.io.IOUtils
import org.json4s.DefaultFormats

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
/**
  * Created by johnnwang on 2018/10/30.
  */
class IOEngineExecutor extends EngineExecutor(outputPrintLimit = 10, true) with ConcurrentTaskOperateSupport with Logging{

  implicit val formats = DefaultFormats

  override def getName: String = "IOEngineExecutor"

  val fsIdCount = new AtomicLong()
  val FS_ID_LIMIT = IOEngineConfiguration.IO_FS_ID_LIMIT.getValue
  //TODO 去掉ArrayBuffer:其中key为用户，value为用户申请到的FS数组
  private val userFSInfos = new util.HashMap[String, ArrayBuffer[FSInfo]]()

  private val fsProxyService = new FsProxyService


  /**
    * 定时清理空闲的FS
    */
  private val cleanupThread = new Thread("IOEngineExecutor-Cleanup-Scanner") {
    setDaemon(true)
    var continue = true
    override def run(): Unit = {
      while(continue) {
        userFSInfos.filter(_._2.exists(_.timeout)).foreach { case (_, list) =>
          list synchronized list.filter(_.timeout).foreach{s => s.fs.close();list -= s}
        }
        Utils.tryQuietly(Thread.sleep(IOEngineConfiguration.IO_FS_CLEAR_TIME.getValue))
      }
    }
  }

  override def init(): Unit = {
    transition(EngineState.Idle)
    super.init()
    cleanupThread.start()
  }

  /**
    * 产生engine唯一的ID
    * @return
    */
  def getFSId():Long ={
    if (fsIdCount.get() == FS_ID_LIMIT){
      fsIdCount.getAndSet(0)
    } else {
      fsIdCount.getAndIncrement()
    }
  }

  override def getActualUsedResources: Resource = {
    new LoadResource(OverloadUtils.getProcessMaxMemory, 1)
  }

  private def existsUserFS(methodEntity: MethodEntity): Boolean = {
    val proxyUser = methodEntity.proxyUser
    if(!userFSInfos.containsKey(proxyUser)) return false
    userFSInfos(proxyUser) synchronized {
      val userFsInfo = userFSInfos.get(proxyUser).find(fsInfo => fsInfo != null && fsInfo.id == methodEntity.id)
      userFsInfo.foreach(_.lastAccessTime = System.currentTimeMillis())
      userFsInfo.isDefined
    }
  }

  protected def getUserFS(methodEntity: MethodEntity): Fs = {
    val fsType = methodEntity.fsType
    val proxyUser = methodEntity.proxyUser
    if(!userFSInfos.containsKey(proxyUser)) {
      throw new StorageErrorException(53001,s"not exist storage $fsType, please init first.")
    }
    userFSInfos(proxyUser) synchronized {
      val userFsInfo = userFSInfos(proxyUser).find(fsInfo => fsInfo != null && fsInfo.id == methodEntity.id)
        .getOrElse(throw new StorageErrorException(53001,s"not exist storage $fsType, please init first."))
      userFsInfo.lastAccessTime = System.currentTimeMillis()
      userFsInfo.fs
    }
  }

  private def createUserFS(methodEntity: MethodEntity): Long = {
    info(s"Creator ${methodEntity.creatorUser}准备为用户${methodEntity.proxyUser}初始化FS：$methodEntity")
    var fsId = methodEntity.id
    val properties = methodEntity.params(0).asInstanceOf[Map[String, String]]
    val proxyUser = methodEntity.proxyUser
    if(!fsProxyService.canProxyUser(methodEntity.creatorUser,proxyUser,methodEntity.fsType)){
      throw new StorageErrorException(52002,s"FS Can not proxy to：$proxyUser")
    }
    if(! userFSInfos.containsKey(proxyUser)) {
      userFSInfos synchronized {
        if(!userFSInfos.containsKey(proxyUser)) userFSInfos += proxyUser -> ArrayBuffer[FSInfo]()
      }
    }
    val userFsInfo = userFSInfos(proxyUser)
    userFsInfo synchronized {
      if(! userFsInfo.exists(fsInfo => fsInfo != null && fsInfo.id == methodEntity.id)) {
        val fs = FSFactory.getFs(methodEntity.fsType)
        fs.init(properties)
        fsId = getFSId()
        userFsInfo +=  new FSInfo(fsId, fs)
      }
    }
    info(s"Creator ${methodEntity.creatorUser}为用户${methodEntity.proxyUser}初始化结束 fsId=$fsId")
    fsId
  }

  private def closeUserFS(methodEntity: MethodEntity): Unit = {
    info(s"Creator ${methodEntity.creatorUser}为用户${methodEntity.proxyUser} close FS：$methodEntity")
    val proxyUser = methodEntity.proxyUser
    if(!userFSInfos.containsKey(proxyUser)) return
    val userFsInfo = userFSInfos(proxyUser)
    userFsInfo synchronized {
      val fsInfo = userFsInfo.find(fsInfo => fsInfo != null && fsInfo.id == methodEntity.id)
      if (fsInfo.isDefined){
        Utils.tryFinally(fsInfo.get.fs.close()){
          userFsInfo -= fsInfo.get
        if(userFsInfo.isEmpty) {
          info(s"Prepare to clear userFsInfo:$methodEntity")
          userFSInfos.remove(proxyUser)
        }
        }
      }
    }
  }

  override protected def executeLine(engineExecutorContext: EngineExecutorContext, code: String): ExecuteResponse = {

    val method = MethodEntitySerializer.deserializer(code)
    val methodName = method.methodName
    val jobID = engineExecutorContext.getJobId.get
    info(s"jobID($jobID):creator ${method.creatorUser} proxy user: ${method.proxyUser} to execute a method: ${method.methodName}.,fsId=${method.id}")
    methodName match {
      case "init" =>
        val fsId:Long =  if(! existsUserFS(method)) {
          createUserFS(method)
        } else {
          method.id
        }
        info(s"jobID($jobID),user(${method.proxyUser}) execute init and fsID($fsId)")
        AliasOutputExecuteResponse(fsId.toString, StorageUtils.serializerStringToResult(fsId.toString))
      case "close" => closeUserFS(method);SuccessExecuteResponse()
      case "read" =>
        val fs = getUserFS(method)
        AliasOutputExecuteResponse(method.id.toString, IOHelp.read(fs, method))
      case "available" =>
        val fs = getUserFS(method)
        if (method.params == null || method.params.length != 2) throw new StorageErrorException(53003, "Unsupported parameter calls")
        val dest = MethodEntitySerializer.deserializerToJavaObject(method.params(0).asInstanceOf[String],classOf[FsPath])
        val position = if (method.params(1).toString.toInt < 0) 0 else method.params(1).toString.toInt
        val inputStream = fs.read(dest)
        Utils.tryFinally(AliasOutputExecuteResponse(method.id.toString, StorageUtils.serializerStringToResult((inputStream.available() - position).toString)))(IOUtils.closeQuietly(inputStream))
      case "write" =>
        val fs = getUserFS(method)
        IOHelp.write(fs, method)
        SuccessExecuteResponse()
      case "renameTo" =>
        val fs = getUserFS(method)
        if (method.params == null || method.params.length != 2) throw new StorageErrorException(53003, "Unsupported parameter calls")
        fs.renameTo(MethodEntitySerializer.deserializerToJavaObject(method.params(0).asInstanceOf[String],classOf[FsPath]),MethodEntitySerializer.deserializerToJavaObject(method.params(1).asInstanceOf[String],classOf[FsPath]))
        SuccessExecuteResponse()
      case "list" =>
        if (method.params == null || method.params.length != 1) throw new StorageErrorException(53003, "Unsupported parameter calls")
        val fs = getUserFS(method)
        val dest = MethodEntitySerializer.deserializerToJavaObject(method.params(0).asInstanceOf[String],classOf[FsPath])
        AliasOutputExecuteResponse(method.id.toString,StorageUtils.serializerStringToResult(MethodEntitySerializer.serializerJavaObject(fs.list(dest))))
      case "listPathWithError" =>
        if (method.params == null || method.params.length != 1) throw new StorageErrorException(53003, "Unsupported parameter calls")
        val fs = getUserFS(method).asInstanceOf[FileSystem]
        val dest = MethodEntitySerializer.deserializerToJavaObject(method.params(0).asInstanceOf[String],classOf[FsPath])
        AliasOutputExecuteResponse(method.id.toString,StorageUtils.serializerStringToResult(MethodEntitySerializer.serializerJavaObject(fs.listPathWithError(dest))))
      case "get" | "create" =>
        if (method.params == null || method.params.length != 1) throw new StorageErrorException(53003, "Unsupported parameter calls")
        val fs = getUserFS(method)
        val dest = MethodEntitySerializer.deserializerToJavaObject(method.params(0).asInstanceOf[String],classOf[String])
        AliasOutputExecuteResponse(method.id.toString,StorageUtils.serializerStringToResult(MethodEntitySerializer.serializerJavaObject(fs.get(dest))))
      case _ =>
        val fs = getUserFS(method)
        val parameterSize = if(method.params == null) 0 else method.params.length
        val realMethod = fs.getClass.getMethods.filter(_.getName == methodName).find(_.getGenericParameterTypes.length == parameterSize)
        if (realMethod.isEmpty) throw new StorageErrorException(53003, s"not exists method $methodName in fs ${fs.getClass.getSimpleName}.")
        if(parameterSize > 0) method.params(0) = MethodEntitySerializer.deserializerToJavaObject(method.params(0).asInstanceOf[String], classOf[FsPath])
        val res = MethodEntitySerializer.serializerJavaObject(ReflectionUtils.invoke(fs, realMethod.get, method.params))
        if("exists" == methodName) {
          info(s"jobID($jobID),user(${method.proxyUser}) execute exists get res($res) and input code($code)")
        }
        AliasOutputExecuteResponse(method.id.toString,StorageUtils.serializerStringToResult(res))
    }
  }

  override protected def executeCompletely(engineExecutorContext: EngineExecutorContext, code: String, completedLine: String): ExecuteResponse = null

  override def kill(jobId: String): Boolean = true

  override def killAll(): Boolean = true

  override def pause(jobId: String): Boolean = true

  override def pauseAll(): Boolean = true

  override def resume(jobId: String): Boolean = true

  override def resumeAll(): Boolean = true

  override def close(): Unit = {
    userFSInfos.foreach{ case (_, list) => list.foreach(m => Utils.tryQuietly(m.fs.close()))}
  }
}
