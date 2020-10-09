package com.webank.wedatasphere.linkis.engine.sqoop.executor

import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorContext}
import com.webank.wedatasphere.linkis.engine.sqoop.conf.SqoopEngineConfiguration
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.resourcemanager.Resource
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteResponse, SingleTaskInfoSupport, SingleTaskOperateSupport}

/**
 * @Classname SqoopEngineExecutor
 * @Description TODO
 * @Date 2020/8/19 18:11
 * @Created by limeng
 */
class SqoopEngineExecutor(user:String) extends EngineExecutor(SqoopEngineConfiguration.OUTPUT_LIMIT.getValue, isSupportParallelism = false) with SingleTaskOperateSupport with SingleTaskInfoSupport {
  override def getName: String = ???

  override def getActualUsedResources: Resource = ???

  override protected def executeLine(engineExecutorContext: EngineExecutorContext, code: String): ExecuteResponse = ???

  override protected def executeCompletely(engineExecutorContext: EngineExecutorContext, code: String, completedLine: String): ExecuteResponse = ???

  override def kill(): Boolean = ???

  override def pause(): Boolean = ???

  override def resume(): Boolean = ???

  override def progress(): Float = ???

  override def getProgressInfo: Array[JobProgressInfo] = ???

  override def log(): String = ???

  override def close(): Unit = ???
}
