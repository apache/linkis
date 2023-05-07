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
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.exception.EntranceErrorCode
import org.apache.linkis.entrance.interceptor.exception.SetTenantLabelException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.conf.{TenantRequest, TenantResponse}
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.TenantLabel
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.StringUtils

import java.{lang, util}
import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}

object TenantLabelSetUtils extends Logging {

  private val userCreatorTenantCache: LoadingCache[String, String] = CacheBuilder
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
          logger.info(s"load tenant data user $user creator $creator data")
          sender.ask(TenantRequest(user, creator)) match {
            case tenantResponse: TenantResponse => tenantResponse.tenant
            case _ =>
              logger.warn(s"TenantCache user $user creator $creator data loading failed")
              ""
          }
        }
        if (StringUtils.isBlank(cacheValue)) {
          logger.warn(s"TenantCache data loading failed , plaese check warn log")
          cacheValue = ""
        }
        cacheValue
      }

    })

  def checkTenantLabel(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    jobRequest match {
      case requestPersistTask: JobRequest =>
        val labels = requestPersistTask.getLabels
        // Determine whether the tenant exists. If it exists, it will be released; if it does not exist, it will be backfilled
        logger.debug("check labels contains tenant :{} ", labels)
        if (!labels.contains(LabelKeyConstant.TENANT_KEY)) {
          // Get user information
          val userName = jobRequest.getSubmitUser
          // Error reported when user information is not obtained
          if (StringUtils.isBlank(userName)) {
            throw SetTenantLabelException(
              EntranceErrorCode.USER_NULL_EXCEPTION.getErrCode,
              EntranceErrorCode.USER_NULL_EXCEPTION.getDesc
            )
          }
          // Get the tenant in the cache through user creator
          var tenant = ""
          tenant = userCreatorTenantCache.get(
            LabelUtil.getUserCreatorLabel(labels).getStringValue.toLowerCase()
          )
          if (StringUtils.isBlank(tenant)) {
            tenant = userCreatorTenantCache.get(
              "*-" + LabelUtil.getUserCreatorLabel(jobRequest.getLabels).getCreator.toLowerCase()
            )
          }
          if (StringUtils.isBlank(tenant)) {
            tenant = userCreatorTenantCache.get(
              LabelUtil.getUserCreatorLabel(jobRequest.getLabels).getUser.toLowerCase() + "-*"
            )
          }
          logger.info("get cache tenant:" + tenant + ",jobRequest:" + jobRequest.getId)
          // Add cached data if it is not empty
          if (StringUtils.isNotBlank(tenant)) {
            val tenantLabel = LabelBuilderFactoryContext.getLabelBuilderFactory
              .createLabel[TenantLabel](LabelKeyConstant.TENANT_KEY)
            tenantLabel.setTenant(tenant)
            labels.add(tenantLabel)
            logAppender.append(
              LogUtils.generateInfo(s"Your task should be to set tenant label $tenant") + "\n"
            )
          }
        }
      case _ =>
    }
    jobRequest
  }

}
