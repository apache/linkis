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

package org.apache.linkis.errorcode.client.handler;

import java.io.Closeable;

public interface ErrorCodeHandler extends Closeable {
  String ERROR_CODE_PRE = "正在根据您任务的错误信息为您诊断并生成错误码,大约需要几秒钟,请您稍等...";
  String ERROR_CODE_OK = "您好,成功为您诊断出任务的错误信息,请您查看";
  String ERROR_CODE_FAILED = "您好,很抱歉,未能为您诊断出错误信息,我们会完善我们的错误码库";
  String NEW_LINE = System.lineSeparator();
}
