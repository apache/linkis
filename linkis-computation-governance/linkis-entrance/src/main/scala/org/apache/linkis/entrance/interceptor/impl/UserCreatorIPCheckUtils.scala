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

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.exception.EntranceErrorCode
import org.apache.linkis.entrance.interceptor.exception.UserCreatorIPCheckException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.conf.{UserIpRequest, UserIpResponse}
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.StringUtils

import java.lang
import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}

object UserCreatorIPCheckUtils extends Logging {

  private val configCache: LoadingCache[String, String] = CacheBuilder
    .newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(EntranceConfiguration.USER_PARALLEL_REFLESH_TIME.getValue, TimeUnit.MINUTES)
    .build(new CacheLoader[String, String]() {

      override def load(userCreatorLabel: String): String = {
        var cacheValue = Utils.tryAndWarn {
          val sender: Sender = Sender
            .getSender(Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)
          val user = userCreatorLabel.split("-")(0)
          val creator = userCreatorLabel.split("-")(1)
          sender.ask(UserIpRequest(user, creator)) match {
            case useripResponse: UserIpResponse => useripResponse.ip
            case _ =>
              logger.warn(s"UserIpCache user $user creator $creator data loading failed")
              ""
          }
        }
        if (StringUtils.isBlank(cacheValue)) {
          logger.warn(s"UserIpCache data loading failed , plaese check warn log")
          cacheValue = ""
        }
        cacheValue
      }

    })

  def checkUserIp(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    // Get IP address
    val jobIp = jobRequest.getSource.getOrDefault(TaskConstant.REQUEST_IP, "")
    logger.debug(s"start to checkTenantLabel $jobIp")
    if (StringUtils.isNotBlank(jobIp)) {
      jobRequest match {
        case jobRequest: JobRequest =>
          // The user information is obtained, and an error is reported if the user information is not obtained
          if (StringUtils.isBlank(jobRequest.getSubmitUser)) {
            throw UserCreatorIPCheckException(
              EntranceErrorCode.USER_NULL_EXCEPTION.getErrCode,
              EntranceErrorCode.USER_NULL_EXCEPTION.getDesc
            )
          }
          // Obtain the IP address in the cache through user creator
          var cacheIp = ""
          cacheIp = configCache.get(
            LabelUtil.getUserCreatorLabel(jobRequest.getLabels).getStringValue.toLowerCase()
          )
          if (StringUtils.isBlank(cacheIp)) {
            cacheIp = configCache.get(
              "*-" + LabelUtil.getUserCreatorLabel(jobRequest.getLabels).getCreator.toLowerCase()
            )
          }
          logger.info("get cache cacheIp:" + cacheIp + ",jobRequest:" + jobRequest.getId)
          // Judge if the cached data is not empty
          if (StringUtils.isNotBlank(cacheIp)) {
            if (!cacheIp.equals("*") && (!cacheIp.contains(jobIp))) {
              logger.warn(
                " User IP blocking failed cacheIp :{} ,jobRequest:{} ,requestIp:{}",
                cacheIp,
                jobRequest.getId,
                jobIp
              )
              throw UserCreatorIPCheckException(
                EntranceErrorCode.USER_IP_EXCEPTION.getErrCode,
                EntranceErrorCode.USER_IP_EXCEPTION.getDesc
              )
            }
          }
        case _ =>
      }
    }
    jobRequest
  }

}
