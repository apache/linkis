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

package com.webank.wedatasphere.linkis.manager.common.entity.enumeration;


public enum NodeStatus {

    /**
     * em 中管理的engineConn状态: Starting running Failed, Success
     * <p>
     * manager中管理的engineConn状态：Starting ShuttingDown Unlock Idle Busy
     */
    Starting, ShuttingDown, Failed, Success,
    Idle, Busy,
    Locked, Unlock,
    Running;

    public static Boolean isAvailable(NodeStatus status) {
        if (Idle == status || Busy == status || Locked == status || Unlock == status || Running == status) {
            return true;
        }
        return false;
    }

    public static Boolean isLocked(NodeStatus status) {
        if (Busy == status || Locked == status || Idle == status) {
            return true;
        }
        return false;
    }

    public static Boolean isIdle(NodeStatus status) {
        if (Idle == status || Unlock == status) {
            return true;
        }
        return false;
    }

    public static Boolean isCompleted(NodeStatus status) {
        if (Success == status || Failed == status || ShuttingDown == status) {
            return true;
        }
        return false;
    }

    public static NodeStatus toNodeStatus(String status) throws IllegalArgumentException {
        if (null == status || "".equals(status)) {
            throw new IllegalArgumentException("Invalid status : " + status + " cannot be matched in NodeStatus");
        }
        switch (status) {
            case "Starting": return NodeStatus.Starting;
            case "ShuttingDown": return NodeStatus.ShuttingDown;
            case "Failed": return NodeStatus.Failed;
            case "Success": return NodeStatus.Success;
            case "Idle": return NodeStatus.Idle;
            case "Busy": return NodeStatus.Busy;
            case "Locked": return NodeStatus.Locked;
            case "Unlock": return NodeStatus.Unlock;
            case "Running": return NodeStatus.Running;
            default:
                throw new IllegalArgumentException("Invalid status : " + status + " in all values in NodeStatus");
        }
    }
}

