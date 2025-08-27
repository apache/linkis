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

package org.apache.linkis.instance.label.service

import org.apache.linkis.protocol.label._
import org.apache.linkis.rpc.Sender

trait InsLabelRpcService {

  /**
   * Attach labels
   * @param insLabelAttachRequest
   *   request
   */
  def attachLabelsToInstance(sender: Sender, insLabelAttachRequest: InsLabelAttachRequest): Unit =
    ???

  /**
   * Refresh labels
   * @param insLabelRefreshRequest
   *   request
   */
  def refreshLabelsToInstance(
      sender: Sender,
      insLabelRefreshRequest: InsLabelRefreshRequest
  ): Unit = ???

  /**
   * Remove labels
   * @param insLabelRemoveRequest
   *   request
   */
  def removeLabelsFromInstance(sender: Sender, insLabelRemoveRequest: InsLabelRemoveRequest): Unit =
    ???

  def queryLabelsFromInstance(
      sender: Sender,
      insLabelQueryRequest: InsLabelQueryRequest
  ): InsLabelQueryResponse

  def queryInstanceFromLabels(
      sender: Sender,
      labelInsQueryRequest: LabelInsQueryRequest
  ): LabelInsQueryResponse

}
