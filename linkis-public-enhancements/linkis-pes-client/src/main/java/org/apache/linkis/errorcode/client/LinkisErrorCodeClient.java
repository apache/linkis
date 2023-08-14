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

package org.apache.linkis.errorcode.client;

import org.apache.linkis.errorcode.client.action.ErrorCodeGetAllAction;
import org.apache.linkis.errorcode.client.result.ErrorCodeGetAllResult;
import org.apache.linkis.errorcode.common.LinkisErrorCode;
import org.apache.linkis.httpclient.dws.DWSHttpClient;
import org.apache.linkis.httpclient.response.Result;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisErrorCodeClient {

  private DWSHttpClient dwsHttpClient;

  private static final Logger LOGGER = LoggerFactory.getLogger(LinkisErrorCodeClient.class);

  public LinkisErrorCodeClient() {}

  public LinkisErrorCodeClient(DWSHttpClient dwsHttpClient) {
    this();
    this.dwsHttpClient = dwsHttpClient;
  }

  public List<LinkisErrorCode> getErrorCodesFromServer() {
    ErrorCodeGetAllAction errorCodeGetAllAction = new ErrorCodeGetAllAction();
    Result result = null;
    List<LinkisErrorCode> errorCodes = new ArrayList<>();
    try {
      result = dwsHttpClient.execute(errorCodeGetAllAction);
    } catch (Exception e) {
      LOGGER.error("Failed to get ErrorCodes from server", e);
    }
    if (result instanceof ErrorCodeGetAllResult) {
      ErrorCodeGetAllResult errorCodeGetAllResult = (ErrorCodeGetAllResult) result;
      errorCodes = errorCodeGetAllResult.getErrorCodes();
    } else if (result != null) {
      LOGGER.error(
          "result is not type of ErrorCodeGetAllResult it is {}", result.getClass().getName());
    } else {
      LOGGER.error("failde to get errorcodes from server and result is null");
    }
    return errorCodes;
  }
}
