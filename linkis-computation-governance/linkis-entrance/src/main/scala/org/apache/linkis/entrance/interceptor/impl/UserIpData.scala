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

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import org.apache.commons.lang3.StringUtils
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.exception.EntranceErrorCode
import org.apache.linkis.entrance.interceptor.exception.{TenantCheckException, UserIpCheckException}
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.conf.{TenantRequest, TenantResponse, UserIpRequest, UserIpResponse}
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.rpc.Sender
import java.lang
import java.util.concurrent.TimeUnit

object UserIpData extends Logging {

  private val configCache: LoadingCache[String, String] = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterAccess(1, TimeUnit.HOURS)
    .refreshAfterWrite(EntranceConfiguration.USER_PARALLEL_REFLESH_TIME.getValue, TimeUnit.MINUTES)
    .build(new CacheLoader[String, String]() {
      override def load(userCreator: String): String = {
        val sender: Sender = Sender.getSender(Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)
        val user = userCreator.split("_")(0)
        val creator = userCreator.split("_")(1)
        sender.ask(UserIpRequest(user, creator)) match {
          case UserIpResponse =>
            UserIpResponse.ip
          case _ => logger.warn("UserIpCache user {} creator {} data loading failed", user, creator)
        }
      }
    })

  def checkUserIp(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    // 获取IP地址
    val jobIp = jobRequest.getSource.get(TaskConstant.REQUEST_IP)
    if (StringUtils.isNotBlank(jobIp)) {
      jobRequest match {
        case jobRequest: JobRequest =>
          // 获取user信息,未获取到用户信息报错
          if (StringUtils.isBlank(jobRequest.getSubmitUser)) {
            throw TenantCheckException(EntranceErrorCode.USER_NULL_EXCEPTION.getErrCode, EntranceErrorCode.USER_NULL_EXCEPTION.getDesc)
          }
          // 通过user-creator  获取缓存中的 ip
          val cacheIp = configCache.get(LabelUtil.getUserCreatorLabel(jobRequest.getLabels).getStringValue)
          // 缓存获取数据不为空则进行判断
          if (StringUtils.isNotBlank(cacheIp) && (!cacheIp.contains(jobIp))) {
            throw new UserIpCheckException(EntranceErrorCode.USER_IP_EXCEPTION.getErrCode, EntranceErrorCode.USER_IP_EXCEPTION.getDesc)
          }
        case _ => jobRequest
      }
    }
    jobRequest
  }

}
