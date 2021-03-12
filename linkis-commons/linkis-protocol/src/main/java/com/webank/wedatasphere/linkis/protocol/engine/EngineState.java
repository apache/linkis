/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
package com.webank.wedatasphere.linkis.protocol.engine;


public enum EngineState {

    /**
     * 引擎的各种状态
     */
    Starting, Idle, Busy, ShuttingDown, Error, Dead, Success;

    public int id() {
        return this.ordinal();
    }

    public static boolean isCompleted(EngineState engineState) {
        switch (engineState) {
            case Error:
            case Dead:
            case Success:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAvailable(EngineState engineState) {
        switch (engineState) {
            case Idle:
            case Busy:
                return true;
            default:
                return false;
        }
    }
}
