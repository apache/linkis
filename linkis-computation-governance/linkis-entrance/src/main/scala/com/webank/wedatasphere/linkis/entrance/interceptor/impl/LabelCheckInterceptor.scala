package com.webank.wedatasphere.linkis.entrance.interceptor.impl

import java.lang
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.entrance.interceptor.exception.LabelCheckException
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest
import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.protocol.task.Task
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._

/**
  * @author peacewong
  * @date 2020/12/1 21:07
  */
class LabelCheckInterceptor extends EntranceInterceptor {

  /**
    * The apply function is to supplement the information of the incoming parameter task, making the content of this task more complete.
    *     * Additional information includes: database information supplement, custom variable substitution, code check, limit limit, etc.
    * apply函数是对传入参数task进行信息的补充，使得这个task的内容更加完整。
    * 补充的信息包括: 数据库信息补充、自定义变量替换、代码检查、limit限制等
    *
    * @param jobRequest
    * @param logAppender Used to cache the necessary reminder logs and pass them to the upper layer(用于缓存必要的提醒日志，传给上层)
    * @return
    */
  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    jobRequest match {
      case requestPersistTask: JobRequest =>
        val labels = requestPersistTask.getLabels
        checkEngineTypeLabel(labels)
        checkUserCreatorLabel(labels)
        jobRequest
      case _ => jobRequest
    }
  }

  private def checkEngineTypeLabel(labels: java.util.List[Label[_]]): Unit = {
    val engineTypeLabelOption = labels.find(_.isInstanceOf[EngineTypeLabel])
    if (engineTypeLabelOption.isDefined) {
      val engineLabel = engineTypeLabelOption.get.asInstanceOf[EngineTypeLabel]
      if (StringUtils.isNotBlank(engineLabel.getEngineType)) {
        return
      }
    }
    throw LabelCheckException(50079, "engineTypeLabel must be need")
  }


  private def checkUserCreatorLabel(labels: java.util.List[Label[_]]): Unit = {
    val userCreatorLabelOption = labels.find(_.isInstanceOf[UserCreatorLabel])
    if (userCreatorLabelOption.isDefined) {
      val userCreator = userCreatorLabelOption.get.asInstanceOf[UserCreatorLabel]
      if (StringUtils.isNotBlank(userCreator.getUser)) {
        return
      }
    }
    throw LabelCheckException(50079, "UserCreatorLabel must be need")
  }


}
