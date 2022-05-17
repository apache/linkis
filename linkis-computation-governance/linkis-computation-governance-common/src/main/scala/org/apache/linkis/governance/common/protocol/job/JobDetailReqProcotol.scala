/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.governance.common.protocol.job

import java.util

import org.apache.linkis.governance.common.entity.job.{SubJobDetail, SubJobInfo}
import org.apache.linkis.protocol.message.RequestProtocol
import java.util.Date

import scala.beans.BeanProperty


trait JobDetailReq extends RequestProtocol

case class JobDetailReqInsert(jobInfo: SubJobInfo) extends JobDetailReq

case class JobDetailReqUpdate(jobInfo: SubJobInfo) extends JobDetailReq

case class JobDetailReqBatchUpdate(jobInfo: util.ArrayList[SubJobInfo]) extends JobDetailReq

case class JobDetailReqQuery(jobReq: SubJobDetail) extends JobDetailReq

case class JobDetailReqReadAll(jobReq: SubJobDetail) extends JobDetailReq

class RequestOneJobDetail extends JobDetailReq {

  @BeanProperty
  var jobDetail: SubJobDetail = _
  @BeanProperty
  var startTime: Date = _
  @BeanProperty
  var endTime: Date = _
}

case class RequestAllJobDetail(instance: String) extends JobDetailReq

case class ResponseOneJobDetail(jobDetail: SubJobDetail)