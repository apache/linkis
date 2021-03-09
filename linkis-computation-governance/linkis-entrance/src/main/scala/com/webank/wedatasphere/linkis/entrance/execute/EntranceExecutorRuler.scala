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

package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorException
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.scheduler.exception.WaitForNextAskExecutorException
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
  * Created by enjoyyin on 2018/9/26.
  */
trait EntranceExecutorRuler {

  def setEngineManager(engineManager: EngineManager): Unit
  def getEngineManager: EngineManager
  def rule(engines: Array[EntranceEngine], job: Job): Array[EntranceEngine]
  def cloneNew(): EntranceExecutorRuler

}
trait AbstractEntranceExecutorRuler extends EntranceExecutorRuler {
  private var engineManager: EngineManager = _
  protected val specialKey: String

  override def setEngineManager(engineManager: EngineManager): Unit = this.engineManager = engineManager

  override def getEngineManager: EngineManager = engineManager

  override def rule(engines: Array[EntranceEngine], job: Job): Array[EntranceEngine] = job match {
    case entranceJob: EntranceJob =>
      val specialMap = TaskUtils.getSpecialMap(entranceJob.getParams)
      val specialValue = specialMap.get(specialKey)
      if(specialValue != null && StringUtils.isNotBlank(specialValue.toString)) {
        val log = new java.lang.StringBuilder
        val es = rule(engines, specialValue, log)
        if(log.length() > 0) job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateSystemInfo(log.toString)))
        es
      }
      else if(specialMap.containsKey(specialKey)) throw new EntranceErrorException(28000, "非法的special configuration: " + specialKey + ", 参数值不能为空！")
      else engines
    case _ => engines
  }

  protected def rule(engines: Array[EntranceEngine], specialValue: Any, log: java.lang.StringBuilder): Array[EntranceEngine]

  override def toString: String = specialKey
}
@Component
class FixedInstanceEntranceExecutorRuler extends AbstractEntranceExecutorRuler with Logging {
  override val specialKey: String = FixedInstanceEntranceExecutorRuler.FIXED_INSTANCE
  override def rule(engines: Array[EntranceEngine], specialValue: Any, log: java.lang.StringBuilder): Array[EntranceEngine] = {
    log.append(s"You have enabled the fixed execution engine function! This time will be submitted to the engine $specialValue.(您开启了固定执行引擎功能！本次将提交给引擎$specialValue。)")
    val es = engines.filter(_.getModuleInstance.getInstance == specialValue)
    if(es.isEmpty && getEngineManager.get(specialValue.toString).isEmpty)
      throw new EntranceErrorException(28001, s"The last used engine $specialValue no longer exists, and this fixed execution engine operation cannot be completed!(沿用上次的引擎$specialValue 已不存在，无法完成本次固定执行引擎操作！)")
    else if(es.isEmpty) {
      throw new WaitForNextAskExecutorException(s"Please note: the execution engine ${specialValue} is currently not idle, waiting for the state to flip...(请注意：执行引擎${specialValue}目前不处于空闲状态，等待状态翻转...)")
    }
    es
  }
  override def cloneNew(): FixedInstanceEntranceExecutorRuler = new FixedInstanceEntranceExecutorRuler
}
object FixedInstanceEntranceExecutorRuler {
  val FIXED_INSTANCE = "fixedInstance"
}
@Component
class ExceptInstanceEntranceExecutorRuler extends AbstractEntranceExecutorRuler {
  override val specialKey: String = ExceptInstanceEntranceExecutorRuler.EXCEPT_INSTANCES
  override def rule(engines: Array[EntranceEngine], specialValue: Any, log: java.lang.StringBuilder): Array[EntranceEngine] = {
    log.append("Please note: This submitted script will exclude the following engines(请注意：本次提交的脚本，将排除以下引擎)：" + specialValue)
    val exceptEngines = ExceptInstanceEntranceExecutorRuler.deserializable(specialValue.toString).toSet
    engines.filterNot(e => exceptEngines.contains(e.getModuleInstance.getInstance))
  }
  override def cloneNew(): ExceptInstanceEntranceExecutorRuler = this
}
object ExceptInstanceEntranceExecutorRuler {
  val EXCEPT_INSTANCES = "exceptInstances"
  def serializable(instances: Array[String]): String = instances.mkString(",")
  def deserializable(instances: String): Array[String] = instances.split(",")
}