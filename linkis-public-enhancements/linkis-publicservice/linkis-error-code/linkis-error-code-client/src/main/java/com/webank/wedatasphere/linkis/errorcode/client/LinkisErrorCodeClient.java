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

package com.webank.wedatasphere.linkis.errorcode.client;

import com.webank.wedatasphere.linkis.errorcode.client.action.ErrorCodeGetAllAction;
import com.webank.wedatasphere.linkis.errorcode.client.result.ErrorCodeGetAllResult;
import com.webank.wedatasphere.linkis.errorcode.common.LinkisErrorCode;
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient;
import com.webank.wedatasphere.linkis.httpclient.response.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class LinkisErrorCodeClient {


    private DWSHttpClient dwsHttpClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkisErrorCodeClient.class);

    public LinkisErrorCodeClient(){

    }


    public LinkisErrorCodeClient(DWSHttpClient dwsHttpClient){
        this();
        this.dwsHttpClient = dwsHttpClient;
    }


    public List<LinkisErrorCode> getErrorCodesFromServer(){
        ErrorCodeGetAllAction errorCodeGetAllAction = new ErrorCodeGetAllAction();
        Result result = null;
        List<LinkisErrorCode> errorCodes = new ArrayList<>();
        try{
            result = dwsHttpClient.execute(errorCodeGetAllAction);
        }catch(Exception e){
            LOGGER.error("Failed to get ErrorCodes from server", e);
        }
        if (result instanceof ErrorCodeGetAllResult){
            ErrorCodeGetAllResult errorCodeGetAllResult = (ErrorCodeGetAllResult)result;
            errorCodes = errorCodeGetAllResult.getErrorCodes();
        } else if (result != null){
            LOGGER.error("result is not type of ErrorCodeGetAllResult it is {}", result.getClass().getName());
        } else {
            LOGGER.error("failde to get errorcodes from server and result is null");
        }
        return errorCodes;
    }




}
