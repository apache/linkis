package com.webank.wedatasphere.linkis.entrance.scheduler.cache

import java.util.concurrent.ExecutorService

import com.google.common.collect.Lists
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.entrance.exception.CacheNotReadyException
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.entrance.persistence.PersistenceManager
import com.webank.wedatasphere.linkis.entrance.utils.JobHistoryHelper
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext
import com.webank.wedatasphere.linkis.scheduler.exception.SchedulerErrorException
import com.webank.wedatasphere.linkis.scheduler.executer.SuccessExecuteResponse
import com.webank.wedatasphere.linkis.scheduler.queue.Group
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._

class ReadCacheConsumer(schedulerContext: SchedulerContext,
                        executeService: ExecutorService, private var group: Group, persistenceManager: PersistenceManager) extends FIFOUserConsumer(schedulerContext, executeService, group) {

  override protected def loop(): Unit = {
    val event = Option(getConsumeQueue.take())
    event.foreach{
      case job: EntranceJob =>
        job.getJobRequest match {
          case jobRequest: JobRequest =>
            Utils.tryCatch {
              val engineTpyeLabel = jobRequest.getLabels.filter(l => l.getLabelKey.equalsIgnoreCase(LabelKeyConstant.ENGINE_TYPE_KEY)).headOption.getOrElse(null)
              val labelStrList = jobRequest.getLabels.map { case l => l.getStringValue}.toList
              if (null == engineTpyeLabel) {
                error("Invalid engineType null, cannot process. jobReq : " + BDPJettyServerHelper.gson.toJson(jobRequest))
                throw CacheNotReadyException(20052, "Invalid engineType null, cannot use cache.")
              }
              val readCacheBefore = TaskUtils.getRuntimeMap(job.getParams).getOrDefault(TaskConstant.READ_CACHE_BEFORE, 300L).asInstanceOf[Long]
              val cacheResult = JobHistoryHelper.getCache(jobRequest.getExecutionCode, jobRequest.getExecuteUser, labelStrList, readCacheBefore)
              if (cacheResult != null && StringUtils.isNotBlank(cacheResult.getResultLocation)) {
                val resultSets = listResults(cacheResult.getResultLocation, job.getUser)
                if (resultSets.size() > 0) {
                  for (resultSet: FsPath <- resultSets) {
                    val alias = FilenameUtils.getBaseName(resultSet.getPath)
                    val output = FsPath.getFsPath(cacheResult.getResultLocation, FilenameUtils.getName(resultSet.getPath)).getSchemaPath
//                    persistenceManager.onResultSetCreated(job, new CacheOutputExecuteResponse(alias, output))
                    throw CacheNotReadyException(20053, "Invalid resultsets, cannot use cache.")
                    // todo check
                  }
//                  persistenceManager.onResultSizeCreated(job, resultSets.size())
                }
                val runtime = TaskUtils.getRuntimeMap(job.getParams)
                runtime.put(TaskConstant.CACHE, false)
                TaskUtils.addRuntimeMap(job.getParams, runtime)
                job.transitionCompleted(SuccessExecuteResponse(), "Result found in cache")
              } else {
                info("Cache not found, submit to normal consumer.")
                submitToExecute(job)
              }
            }{ t =>
                warn("Read cache failed, submit to normal consumer: ", t)
                submitToExecute(job)
            }
          case _ =>
        }
      case _ =>
    }
  }

  private def listResults(resultLocation: String, user: String) = {
    val dirPath = FsPath.getFsPath(resultLocation)
    val fileSystem = FSFactory.getFsByProxyUser(dirPath, user).asInstanceOf[FileSystem]
    Utils.tryFinally {
      fileSystem.init(null)
      if (fileSystem.exists(dirPath)) {
        fileSystem.listPathWithError(dirPath).getFsPaths
      } else {
        Lists.newArrayList[FsPath]()
      }
    }(Utils.tryQuietly(fileSystem.close()))
  }

  private def submitToExecute(job: EntranceJob): Unit = {
    val runtime = TaskUtils.getRuntimeMap(job.getParams)
    runtime.put(TaskConstant.READ_FROM_CACHE, false)
    TaskUtils.addRuntimeMap(job.getParams, runtime)
    val groupName = schedulerContext.getOrCreateGroupFactory.getOrCreateGroup(job).getGroupName
    val consumer = schedulerContext.getOrCreateConsumerManager.getOrCreateConsumer(groupName)
    val index = consumer.getConsumeQueue.offer(job)
    //index.map(getEventId(_, groupName)).foreach(job.setId)
    if (index.isEmpty) throw new SchedulerErrorException(12001, "The submission job failed and the queue is full!(提交作业失败，队列已满！)")
  }

}
