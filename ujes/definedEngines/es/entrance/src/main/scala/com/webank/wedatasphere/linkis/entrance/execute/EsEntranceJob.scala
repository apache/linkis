package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest

/**
 *
 * @author wang_zh
 * @date 2020/5/11
 */
class EsEntranceJob extends EntranceExecutionJob {

  override def jobToExecuteRequest(): ExecuteRequest = super.jobToExecuteRequest()

}
