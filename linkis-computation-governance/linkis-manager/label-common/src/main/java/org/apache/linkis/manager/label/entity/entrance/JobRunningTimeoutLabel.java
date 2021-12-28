/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.label.entity.entrance;

import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.GenericLabel;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;

import java.util.HashMap;

public class JobRunningTimeoutLabel extends GenericLabel {

    public JobRunningTimeoutLabel() {
        setLabelKey(LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY);
    }

    public Long getRunningTimeout() {
        if (null == getValue()) {
            return -1L;
        }
        return Long.parseLong(
                getValue().getOrDefault(LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY, "-1"));
    }

    @ValueSerialNum(0)
    public void setJobRunningTimeout(String runningTimeout) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put(LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY, runningTimeout);
    }
}
