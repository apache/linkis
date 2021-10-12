/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.computation.client.interactive

import com.webank.wedatasphere.linkis.computation.client.AbstractLinkisJobBuilder
import com.webank.wedatasphere.linkis.computation.client.utils.LabelKeyUtils
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType
import com.webank.wedatasphere.linkis.ujes.client.UJESClient
import com.webank.wedatasphere.linkis.ujes.client.request.JobSubmitAction
import org.apache.commons.lang.StringUtils


class InteractiveJobBuilder private[interactive]()
  extends AbstractLinkisJobBuilder[SubmittableInteractiveJob] {

  private var creator: String = _

  override def addExecuteUser(executeUser: String): this.type = super.addExecuteUser(executeUser)

  def setEngineType(engineType: String): this.type = addLabel(LabelKeyUtils.ENGINE_TYPE_LABEL_KEY, engineType)

  def setCreator(creator: String): this.type = {
    this.creator = creator
    this
  }

  def setCode(code: String): this.type = addJobContent("code", code)

  def setRunType(runType: RunType): this.type= addJobContent("runType", runType.toString)

  def setRunTypeStr(runType: String): this.type = addJobContent("runType", runType)

  override protected def validate(): Unit = {
    if(labels != null && !labels.containsKey(LabelKeyUtils.USER_CREATOR_LABEL_KEY)
      && StringUtils.isNotBlank(creator))
      addLabel(LabelKeyUtils.USER_CREATOR_LABEL_KEY, executeUser + "-" + creator)
    super.validate()
  }

  override protected def createLinkisJob(ujesClient: UJESClient,
                                         jobSubmitAction: JobSubmitAction): SubmittableInteractiveJob = new SubmittableInteractiveJob(ujesClient, jobSubmitAction)

}
