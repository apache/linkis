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

package com.webank.wedatasphere.linkis.common.errorcode;


public enum LinkisComputationGovernanceErrorCodeSummary {

    ENGINE_LAUNCH_REQUEST_USER_BLANK(20000,
            "user is null in the parameters of the request engine",
            "user is null in the parameters of the request engine",
            "EngineConnManger"),
    ENGINE_LAUNCH_REQUEST_CREATOR_BLANK(20001,
            "creator is null in the parameters of the request engine",
            "creator is null in the parameters of the request engine",
             "EngineConnManager"),
    ENGINE_INIT_FAILED(20002, "engine initialization failed", "engine initialization failed", "EngineConnManager"),

    ENGINE_REQUEST_USER_BLANK(20000, "user is null in the parameters of the request engine", "user is null in the parameters of the request engine", "EngineConnManger"),


    AM_EM_NOT_FOUND(20100, "user is null in the parameters of the request engine", "user is null in the parameters of the request engine", "EngineConnManger");
    /**
     * error code
     */
    private int errorCode;
    /**
     * error description
     */
    private String errorDesc;
    /**
     * possible causes of errors
     */
    private String comment;
    /**
     * the module of the links to which it belongs
     */
    private String module;

    LinkisComputationGovernanceErrorCodeSummary(int errorCode, String errorDesc, String comment, String module) {
        ErrorCodeUtils.validateErrorCode(errorCode, 20000, 24999);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
        this.comment = comment;
        this.module = module;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }


    @Override
    public String toString() {
        return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
    }
}
