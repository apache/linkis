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

import com.webank.wedatasphere.linkis.protocol.engine.RequestTask
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteRequest, JobExecuteRequest, RunTypeExecuteRequest}

/**
  * Created by allenlliu on 2019/4/8.
  */
class SparkEngineJob extends CommonEngineJob {
  private val runType:String = "runType"
  override def jobToExecuteRequest: ExecuteRequest = {
    if (request.getProperties.containsKey(runType) && request.getProperties.containsKey(RequestTask.RESULT_SET_STORE_PATH))
      new ExecuteRequest with JobExecuteRequest with StorePathExecuteRequest with RunTypeExecuteRequest{
      override val code: String = request.getCode
      override val jobId: String = SparkEngineJob.this.getId
      override val storePath: String = request.getProperties.get(RequestTask.RESULT_SET_STORE_PATH).toString
      override val runType: String = request.getProperties.containsKey(runType).toString
    }
    if(request.getProperties.containsKey(RequestTask.RESULT_SET_STORE_PATH)) new ExecuteRequest with JobExecuteRequest with StorePathExecuteRequest {
      override val code: String = request.getCode
      override val jobId: String = SparkEngineJob.this.getId
      override val storePath: String = request.getProperties.get(RequestTask.RESULT_SET_STORE_PATH).toString
    } else new ExecuteRequest with JobExecuteRequest {
      override val code: String = request.getCode
      override val jobId: String = SparkEngineJob.this.getId
    }
  }
}
