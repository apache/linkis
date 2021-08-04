/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.label.entity

import java.util
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum


class JobLabel extends GenericLabel {

  setLabelKey("job")

  def getJobId: String = Option(getValue).map(_.get("jobId")).orNull

  @ValueSerialNum(0)
  def setJobId(jobId: String): Unit = {
    if (null == getValue) setValue(new util.HashMap[String, String])
    getValue.put("jobId", jobId)
  }

}
