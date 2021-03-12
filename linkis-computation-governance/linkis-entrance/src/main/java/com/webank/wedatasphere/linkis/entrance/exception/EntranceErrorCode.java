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

/**
 * author: enjoyyin
 * date: 2018/9/17
 * time: 12:15
 * Description:
 */
package com.webank.wedatasphere.linkis.entrance.exception;

public enum EntranceErrorCode {
    /**
     *
     */
    CACHE_NOT_READY(200, "shared cache not ready"),
    ENTRANCE_CAST_FAIL(20002, "class cast failed"),
    PARAM_CANNOT_EMPTY(20008, "params cannot be empty "),
    LABEL_PARAMS_INVALID(20009, "Label params invalid. ")

    ;
    private int errCode;
    private String desc;
    EntranceErrorCode(int errCode, String desc){
        this.errCode = errCode;
        this.desc = desc;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getDesc() {
        return desc;
    }

}
