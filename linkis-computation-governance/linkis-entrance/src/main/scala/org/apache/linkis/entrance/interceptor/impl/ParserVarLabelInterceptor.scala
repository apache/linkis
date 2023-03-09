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

import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.TenantLabel
import org.apache.linkis.protocol.utils.TaskUtils

import java.lang

class ParserVarLabelInterceptor extends EntranceInterceptor {

  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    jobRequest match {
      case requestPersistTask: JobRequest =>
        val variableMap = TaskUtils.getVariableMap(requestPersistTask.getParams)
        val labels = requestPersistTask.getLabels
        if (variableMap.containsKey(LabelKeyConstant.TENANT_KEY)) {
          val tenantLabel = LabelBuilderFactoryContext.getLabelBuilderFactory
            .createLabel[TenantLabel](LabelKeyConstant.TENANT_KEY)
          tenantLabel.setTenant(variableMap.get(LabelKeyConstant.TENANT_KEY).toString)
          labels.add(tenantLabel)
        }
      case _ =>
    }
    jobRequest
  }

}
