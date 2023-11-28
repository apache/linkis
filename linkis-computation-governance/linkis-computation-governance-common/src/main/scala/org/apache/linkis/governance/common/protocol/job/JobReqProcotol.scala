/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.governance.common.protocol.job

import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.protocol.message.RequestProtocol

import java.util
import java.util.Date

import scala.beans.BeanProperty

trait JobReq extends RequestProtocol

case class JobReqInsert(jobReq: JobRequest) extends JobReq

case class JobReqUpdate(jobReq: JobRequest) extends JobReq

case class JobReqBatchUpdate(jobReq: util.ArrayList[JobRequest]) extends JobReq

case class JobReqQuery(jobReq: JobRequest) extends JobReq

case class JobReqReadAll(jobReq: JobRequest) extends JobReq

class RequestOneJob extends JobReq {

  @BeanProperty
  var jobReq: JobRequest = _

  @BeanProperty
  var startTime: Date = _

  @BeanProperty
  var endTime: Date = _

}

case class RequestAllJob(instance: String) extends JobReq

case class RequestFailoverJob(
    reqMap: util.Map[String, java.lang.Long],
    statusList: util.List[String],
    startTimestamp: Long,
    limit: Int = 10
) extends JobReq
