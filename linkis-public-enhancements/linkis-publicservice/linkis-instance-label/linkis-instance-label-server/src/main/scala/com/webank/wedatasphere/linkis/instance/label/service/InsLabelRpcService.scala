/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.instance.label.service

import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.protocol.label._


trait InsLabelRpcService {

  /**
   * Attach labels
   * @param insLabelAttachRequest request
   */
  def attachLabelsToInstance(context: ServiceMethodContext, insLabelAttachRequest: InsLabelAttachRequest): Unit = ???

  /**
   * Refresh labels
   * @param insLabelRefreshRequest request
   */
  def refreshLabelsToInstance(context: ServiceMethodContext, insLabelRefreshRequest: InsLabelRefreshRequest): Unit = ???

  /**
   * Remove labels
   * @param insLabelRemoveRequest request
   */
  def removeLabelsFromInstance(context: ServiceMethodContext, insLabelRemoveRequest: InsLabelRemoveRequest): Unit = ???

  def queryLabelsFromInstance(context: ServiceMethodContext, insLabelQueryRequest: InsLabelQueryRequest): InsLabelQueryResponse

  def queryInstanceFromLabels(context: ServiceMethodContext, labelInsQueryRequest: LabelInsQueryRequest): LabelInsQueryResponse
}
