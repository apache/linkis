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

package org.apache.linkis.entrance.exception;

import org.apache.linkis.common.exception.ErrorException;

/**
 * Description: When the entity submits a request for a database query to the query module, the
 * sender's send and ask operations are abnormal, and the exception is thrown. Description:
 * 当entrance 向 query 模块提交数据库查询的请求，sender的send和ask操作出现异常，就抛出该异常
 */
public class EntranceRPCException extends ErrorException {
  public EntranceRPCException(int errCode, String desc) {
    super(errCode, desc);
  }

  public EntranceRPCException(int errCode, String desc, Exception e) {
    super(errCode, desc + " " + e.getMessage());
    this.initCause(e);
  }
}
