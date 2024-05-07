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

package org.apache.linkis.entrance.interceptor.impl

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.conf.EntranceConfiguration.VALIDATOR_PARTITION_CHECK_ENABLE
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.interceptor.exception.{LabelCheckException, PartitionCheckException}
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.conf.{PartitionCheckConfRequest, PartitionCheckConfResponse}
import org.apache.linkis.governance.common.utils.LoggerUtils
import org.apache.linkis.rpc.Sender

/**
 * Description: For partition type check(用于分区类型校验)
 */
class PartitionTypeCheckInterceptor extends EntranceInterceptor with Logging{

  @throws[ErrorException]
  override def apply(jobRequest: JobRequest, logAppender: java.lang.StringBuilder): JobRequest = {
    jobRequest match {
      case jobRequest: JobRequest =>
        if (VALIDATOR_PARTITION_CHECK_ENABLE.getValue) {
          Utils.tryAndWarn{
            val sender: Sender = Sender.getSender(EntranceConfiguration.VALIDATOR_APPLICATION_NAME.getValue)
            val request = new PartitionCheckConfRequest(jobRequest.getExecutionCode)
            val response: PartitionCheckConfResponse = sender.ask(request).asInstanceOf[PartitionCheckConfResponse]
            if (!response.isCheckStatus) {
              throw PartitionCheckException(50082, "partition column type checkfailed!(分区字段类型校验失败！): " + response.getDesc)
            }
          }
        }
        jobRequest
      case _ => jobRequest
    }
  }

}
