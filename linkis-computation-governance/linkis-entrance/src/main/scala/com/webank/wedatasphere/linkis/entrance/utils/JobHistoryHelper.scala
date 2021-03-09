package com.webank.wedatasphere.linkis.entrance.utils

import java.util

import com.google.gson.Gson
import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.JobHistoryFailedException
import com.webank.wedatasphere.linkis.protocol.query.cache.{CacheTaskResult, RequestReadCache}
import com.webank.wedatasphere.linkis.protocol.query.{RequestPersistTask, RequestQueryTask, RequestUpdateTask, ResponsePersist}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState

/**
 * created by cooperyang on 2020/1/2
 * Description:
 */
object JobHistoryHelper extends Logging{

  private val sender = Sender.getSender(EntranceConfiguration.QUERY_PERSISTENCE_SPRING_APPLICATION_NAME.getValue)

  private val SUCCESS_FLAG = 0
  private val TASK_MAP_KEY = "task"

  def getCache(executionCode: String, engineType: String, user: String, readCacheBefore: Long): CacheTaskResult ={
    val requestReadCache = new RequestReadCache(executionCode, engineType, user, readCacheBefore)
    sender.ask(requestReadCache) match {
      case  c: CacheTaskResult => c
      case _ => null
    }
  }

  def getStatusByTaskID(taskID:Long):String = {
    val task = getTaskByTaskID(taskID)
    if (task == null) SchedulerEventState.Cancelled.toString else task.getStatus
  }

  /**
   * 对于一个在内存中找不到这个任务的话，可以直接干掉
   * @param taskID
   */
  def forceKill(taskID:Long):Unit = {
    val requestUpdateTask = new RequestUpdateTask
    requestUpdateTask.setTaskID(taskID)
    requestUpdateTask.setStatus("Cancelled")
    sender.ask(requestUpdateTask)
  }

  private def getTaskByTaskID(taskID:Long):RequestPersistTask = {
    val requestQueryTask = new RequestQueryTask()
    requestQueryTask.setTaskID(taskID)
    requestQueryTask.setSource(null)
    val task = Utils.tryCatch{
      val taskResponse = sender.ask(requestQueryTask)
      taskResponse match {
        case responsePersist:ResponsePersist => val status = responsePersist.getStatus
          if (status != SUCCESS_FLAG){
            logger.error(s"query from jobHistory status failed, status is $status")
            throw JobHistoryFailedException("query from jobHistory status failed")
          }else{
            val data = responsePersist.getData
            data.get(TASK_MAP_KEY) match {
              case tasks:util.List[util.Map[String, Object]] => tasks.get(0) match {
                case map:util.Map[String, Object] => val gson = new Gson()
                  val json = gson.toJson(map)
                  val requestPersistTask = gson.fromJson(json, classOf[RequestPersistTask])
                  requestPersistTask
                case _ => throw JobHistoryFailedException(s"query from jobhistory not a correct RequestPersistTask type taskId is $taskID")
              }
              case _ => throw JobHistoryFailedException(s"query from jobhistory not a correct List type taskId is $taskID")
            }
          }
        case _ => logger.error("get query response incorrectly")
          throw JobHistoryFailedException("get query response incorrectly")
      }
    }{
      case errorException:ErrorException => throw errorException
      case e:Exception => val e1 = JobHistoryFailedException(s"query taskId $taskID error")
        e1.initCause(e)
        throw e
    }
    task
  }


}
