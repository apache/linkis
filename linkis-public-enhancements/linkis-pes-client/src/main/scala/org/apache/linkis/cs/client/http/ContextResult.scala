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

package org.apache.linkis.cs.client.http

import org.apache.linkis.cs.listener.callback.imp.ContextKeyValueBean
import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult

import java.util

abstract class ContextResult extends DWSResult

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/createContextID")
class ContextCreateResult extends ContextResult {
  var contextId: String = _
  def setContextId(contextId: String): Unit = this.contextId = contextId
  def getContextId: String = this.contextId
}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/resetValue")
class ContextResetResult extends ContextResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/setValueByKey")
class ContextUpdateResult extends ContextResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/getContext")
class ContextGetResult extends ContextResult {
  var contextId: String = _
  var contextKeyValues: util.Map[String, String] = _

  def setContextId(contextId: String): Unit = this.contextId = contextId
  def getContextId: String = this.contextId

  def setContextKeyValues(kvs: util.Map[String, String]): Unit = this.contextKeyValues = kvs
  def getContextKeyValues: util.Map[String, String] = this.contextKeyValues

}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/removeValue")
class ContextRemoveResult extends ContextResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/heartbeat")
class ContextHeartBeatResult extends ContextResult {
  var contextKeyValueBeans: util.List[ContextKeyValueBean] = _

  def setContextKeyValueBeans(kvs: util.List[ContextKeyValueBean]): Unit =
    this.contextKeyValueBeans = kvs

  def getContextKeyValueBeans: util.List[ContextKeyValueBean] = this.contextKeyValueBeans;
}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/setValue")
class ContextSetKeyValueResult extends ContextResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/removeAllValue")
class ContextResetIDResult extends ContextResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/removeValue")
class ContextRemoveValueResult extends ContextResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/onBindIDListener")
class ContextBindIDResult extends ContextResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/onBindKeyListener")
class ContextBindKeyResult extends ContextResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/searchContextValue")
class ContextSearchResult extends ContextResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/getContextValue")
class ContextGetValueResult extends ContextResult {}

// TODO: 用来匹配所有的void 不需要处理返回值的result
@DWSHttpMessageResult(
  "/api/rest_j/v\\d+/contextservice/(createHistory|removeHistory|removeAllValueByKeyPrefix|removeAllValueByKeyPrefixAndContextType)"
)
class VoidResult extends ContextResult {}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/(getHistories|searchHistory)")
class ContextHistoriesGetResult extends ContextResult {
  var contextHistory: util.List[String] = _
  def getContextHistory: util.List[String] = this.contextHistory

  def setContextHistory(contextHistory: util.List[String]): Unit = this.contextHistory =
    contextHistory

}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/getHistory")
class ContextHistoryGetResult extends ContextResult {
  var contextHistory: String = _
  def getContextHistory: String = this.contextHistory
  def setContextHistory(contextHistory: String): Unit = this.contextHistory = contextHistory
}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/searchContextIDByTime")
class ContextSearchIDByTimeResult extends ContextResult {
  var contextIDs: util.List[String] = _
  def getContextIDs: util.List[String] = contextIDs
  def setContextIDs(idList: util.List[String]): Unit = this.contextIDs = idList
}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/clearAllContextByID")
class ContextClearByIDResult extends ContextResult {
  var num: Int = _
  def getNum: Int = num
  def setNum(num: Int): Unit = this.num = num
}

@DWSHttpMessageResult("/api/rest_j/v\\d+/contextservice/clearAllContextByTime")
class ContextClearByTimeResult extends ContextResult {
  var num: Int = _
  def getNum: Int = num
  def setNum(num: Int): Unit = this.num = num
}
