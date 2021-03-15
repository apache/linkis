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

package com.webank.wedatasphere.linkis.governance.common.entity;

public enum ExecutionNodeStatus {

    Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Timeout;

    public static boolean isRunning(ExecutionNodeStatus eventStatus) {
        return eventStatus == Running;
    }

    public static boolean isScheduled(ExecutionNodeStatus eventStatus) {
        return eventStatus != Inited;
    }

    public static boolean isCompleted(ExecutionNodeStatus eventStatus) {
        switch (eventStatus) {
            case Inited:
            case Scheduled:
            case Running:
            case WaitForRetry:
                return false;
            default:
                return true;
        }
    }

    public static boolean isSucceed(ExecutionNodeStatus eventStatus) {
        return Succeed == eventStatus;
    }


}
