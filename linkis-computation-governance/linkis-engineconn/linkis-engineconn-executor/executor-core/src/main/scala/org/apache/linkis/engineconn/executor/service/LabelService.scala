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

package org.apache.linkis.engineconn.executor.service

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.executor.entity.Executor
import org.apache.linkis.manager.label.entity.Label

import java.util

trait LabelService {

  /* def labelUpdate(labelUpdateRequest: LabelUpdateRequest): Unit */

  def labelReport(labels: util.List[Label[_]], executor: Executor): Unit

}

class DefaultLabelService extends LabelService with Logging {

  /* override def labelUpdate(labelUpdateRequest: LabelUpdateRequest): Unit = ??? */

  override def labelReport(labels: util.List[Label[_]], executor: Executor): Unit = {
    logger.info(s"executor ${executor.getId} prepare to report Labels ")
    ManagerService.getManagerService.labelReport(labels)
    logger.info(s"executor ${executor.getId} end to report Labels ")
  }

}

object LabelService {

  private val labelService = new DefaultLabelService

  def getLabelService: LabelService = this.labelService

}
