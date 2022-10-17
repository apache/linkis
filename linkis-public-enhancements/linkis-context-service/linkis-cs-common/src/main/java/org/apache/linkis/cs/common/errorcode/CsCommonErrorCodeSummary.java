/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.common.errorcode;

public enum CsCommonErrorCodeSummary {
  INVALID_INSTANCE_ALIAS(70010, "", ""),
  INVALID_NULL_INSTANCE(70010, "Invalid null instance(无效的实例)", "Invalid null instance(无效的实例)"),
  HACONTEXTID_CANNOT_NULL(
      70011,
      "HaContextID cannot be null.(HaContextID 不能为空.)",
      "HaContextID cannot be null.(HaContextID 不能为空.)"),
  GET_CONTEXTID_METHON(
      70011,
      "GetContextID method{} returns invalid haContextID :{} (GetContextID 方法{}返回无效的 haContextID ：{})",
      "GetContextID method{} returns invalid haContextID :{} (GetContextID 方法{}返回无效的 haContextID ：{}):"),
  INVALID_HACONTEXTID(
      70011,
      "Invalid contextID in haContextID (haContextID 中的 contextID 无效):",
      "Invalid contextID in haContextID (haContextID 中的 contextID 无效):"),
  INVALID_HACONTEXTID_CONTEXTID(
      70011,
      "Invalid haContextId. contextId (haContextId 无效,上下文 ID):",
      "Invalid haContextId. contextId (haContextId 无效,上下文 ID):"),
  INVALID_CONTEXTID(
      70013,
      "ContextId of HAContextID instance cannot be numberic, contextId (HAContextID 实例的 ContextId 不能是数字,上下文标识):",
      "ContextId of HAContextID instance cannot be numberic, contextId (HAContextID 实例的 ContextId 不能是数字,上下文标识):"),
  GENERATE_BACKUP_INSTANCE_ERROR(
      70014,
      "Generate backupInstance cannot be null.(生成备份实例不能为空.)",
      "Generate backupInstance cannot be null.(生成备份实例不能为空.)"),
  INVALID_INSTANCE(
      70015,
      "MainInstance alias cannot be null.(MainInstance 别名不能为空)",
      "MainInstance alias cannot be null.(MainInstance 别名不能为空)"),
  INVALID_INSTANCE_CODE(70015, "", ""),
  INVAID_HA_CONTEXTID(
      70016, "Invalid HAContextID (无效的 HAContextID):", "Invalid HAContextID (无效的 HAContextID):"),
  CS_RPC_ERROR(70017, "", ""),
  INVALID_NULL_STRING(
      70102,
      "ContextID or nodeName or contextValueType cannot be null(ContextID 或 nodeName 或 contextValueType 不能为空)",
      "ContextID or nodeName or contextValueType cannot be null(ContextID 或 nodeName 或 contextValueType 不能为空)"),
  HAIDKEY_CANNOT_EMPTY(
      70107, "HAIDKey cannot be empty.(HAIDKey 不能为空.)", "HAIDKey cannot be empty.(HAIDKey 不能为空.)"),
  CANNOT_ENCODEHAIDKEY(
      70107,
      "Cannot encodeHAIDKey, contextID(无法编码HAIDKey、contextID):",
      "Cannot encodeHAIDKey, contextID(无法编码HAIDKey、contextID):"),

  INVALID_HAID(70108, "Invalid haid(无效的 haid):", "Invalid haid(无效的 haid):"),
  INVALID_CONTEXT_TYPE(70109, "Invalid Context Type(无效的上下文类型):", "Invalid Context Type(无效的上下文类型):"),
  GET_CONTEXT_VALUE_ERROR(
      70110, "Search context value error(搜索上下文值错误):", "Search context value error(搜索上下文值错误):"),
  GET_CONTEXTVALUE_FAILED(
      70110,
      "Failed to get ContextValue(获取 ContextValue 失败):",
      "Failed to get ContextValue(获取 ContextValue 失败):"),
  SEARCH_CONTEXTMAP_ERROR(
      70110,
      "SearchUpstreamContextMap error(searchUpstreamContextMap 错误)",
      "SearchUpstreamContextMap error(searchUpstreamContextMap 错误)"),

  SEARCH_UPSTREAM_ERROR(
      70110,
      "Search Upstream KeyValue error (搜索上游KeyValue错误):",
      "Search Upstream KeyValue error(搜索上游KeyValue错误):"),

  SEARCH_CONTEXT_VALUE_ERROR(70111, "Search value (搜索值):", "Search value (搜索值):"),
  HAID_CANNOT_ECCODEED(
      70111,
      "Incomplete HAID Object cannot be encoded. mainInstance(无法对不完整的 HAID 对象进行编码主实例):",
      "Incomplete HAID Object cannot be encoded. mainInstance(无法对不完整的 HAID 对象进行编码主实例):"),
  CONVER_ERROT_INVALD_HAID(
      70111,
      "ConvertHAIDToHAKey error, invald HAID (ConvertHAIDToHAKey 错误，无效的 HAID):",
      "ConvertHAIDToHAKey error, invald HAID (ConvertHAIDToHAKey 错误，无效的 HAID):"),
  INVALID_HAIDKEY(70111, "Invalid haIDKey (haIDKey 无效):", "Invalid haIDKey (haIDKey 无效):"),

  DESERIALIZD_FAILED(
      70112,
      "Deserialize failed, invalid contextId(反序列化失败，无效的 contextId):",
      "Deserialize failed, invalid contextId(反序列化失败，无效的 contextId):"),
  CONTEXTID_ERROR(
      70112,
      "Deserialize contextID error. contextID(反序列化上下文ID错误,上下文ID):",
      "Deserialize contextID error. contextID (反序列化上下文ID错误,上下文ID):"),
  DESERIALIZD_ERROR(70112, "Deserialize error.(反序列化错误)", "Deserialize error.(反序列化错误)"),
  FAILED_CONSTRUCT(
      70112,
      "Failed to construct contextSerializer(构造 contextSerializer 失败)",
      "Failed to construct contextSerializer(构造 contextSerializer 失败)"),
  CANNOT_DUOLICATED(
      70112,
      "ContextSerializer Type cannot be duplicated(contextSerializer 类型不能重复)",
      "ContextSerializer Type cannot be duplicated(contextSerializer 类型不能重复)"),
  GET_UPSTREAM_TABLE_ERROR(
      70112, "Get Upstream Tables error(获取上游表错误)", "Get Upstream Tables error(获取上游表错误)"),
  GET_SUITABLE_ERROR(
      70112,
      "Get UpstreamSuitableTable error(获取 UpstreamSuitableTable 错误)",
      "Get UpstreamSuitableTable error(获取 UpstreamSuitableTable 错误)"),
  FAILED_SEARCH(
      70112,
      "Failed to searchUpstreamTableKeyValue(搜索UpstreamTableKeyValue 失败)",
      "Failed to searchUpstreamTableKeyValue(搜索UpstreamTableKeyValue 失败)"),
  PUTCSTABLE_ERROR(70112, "PutCSTable error(putCSTable 错误)", "PutCSTable error(putCSTable 错误)"),
  GETCSTABLE_ERROR(70112, "GetCSTable error(getCSTable 错误)", "GetCSTable error(getCSTable 错误)"),
  FAILED_REGISTER(
      70112,
      "Failed to register cs tmp table(注册cs tmp表失败)",
      "Failed to register cs tmp table(注册cs tmp表失败)"),
  CREATE_HISTORY_ERROR(
      70112, "CreateHistory error(createHistory 错误)", "CreateHistory error(createHistory 错误)"),
  INITCONTEXTINFO_ERROR(
      70112,
      "InitContextInfo error. contextIDStr(初始化上下文信息错误。上下文IDStr):",
      "InitContextInfo error. contextIDStr(初始化上下文信息错误。上下文IDStr):"),
  REQUEST_FAILED_ERRORCODE(80015, "", ""),
  DWSRESULT_NOT_INSTANCE(
      80015,
      "Resulet is not instance of DWSResult(resulet 不是 DWSResult 的实例)",
      "Resulet is not instance of DWSResult(resulet 不是 DWSResult 的实例)"),
  INVALID_RESULT_TYPE(80017, "Invalid result type (结果类型无效):", "Invalid result type (结果类型无效):"),
  INVALID_NULL_RESULT(80017, "Invalid null result (无效的结果)", "Invalid null result (无效的结果)"),
  CANNOT_ALL_BLANK(
      97000,
      "CreateTimeStart,  createTimeEnd,  updateTimeStart,  updateTimeEnd,  accessTimeStart,  accessTimeEnd cannot all be blank.(createTimeStart、createTimeEnd、updateTimeStart、updateTimeEnd、accessTimeStart、accessTimeEnd 不能全部为空.)",
      "CreateTimeStart,  createTimeEnd,  updateTimeStart,  updateTimeEnd,  accessTimeStart,  accessTimeEnd cannot all be blank.(createTimeStart、createTimeEnd、updateTimeStart、updateTimeEnd、accessTimeStart、accessTimeEnd 不能全部为空.)"),
  FAILED_SERIALIZE_CONTEXTKEYVALUE(
      97000,
      "Failed to serialize contextKeyValue(无法序列化 contextKeyValue)",
      "Failed to serialize contextKeyValue(无法序列化 contextKeyValue)"),
  FAILED_SERIALIZE_CONTEXTVALUE(
      97000,
      "Failed to serialize contextValue(无法序列化 contextValue)",
      "Failed to serialize contextValue(无法序列化 contextValue)"),
  HISTORY_CANNOT_EMPTY(
      97000, "History source cannot be empty(历史源不能为空)", "History source cannot be empty(历史源不能为空)"),
  EXECUTE_FAILED(97000, "Execute failed,reason(执行失败，原因):", "Execute failed,reason(执行失败，原因):"),
  TRANSFER_BEAN(97000, "Transfer bean failed(传输 bean 失败):", "Transfer bean failed(传输 bean 失败):"),
  CONTEXTID_CANNOT_EMPTY(
      97000, "ContxtId cannot be empty(contxtId 不能为空)", "ContxtId cannot be empty(contxtId 不能为空)"),
  IDLIST_CANNOT_EMPTY(
      97000, "The idList cannot be empty(idList 不能为空)", "The idList cannot be empty(idList 不能为空)"),
  TYPE_SCOPE_EMPTY(
      97000,
      "Try to create context ,type or scope cannot be empty(尝试创建上下文，类型或范围不能为空)",
      "Try to create context ,type or scope cannot be empty(尝试创建上下文，类型或范围不能为空)"),
  CANNOT_ALL_BLANK_FOUR(
      97000,
      "CreateTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd cannot be all null(createTimeStart、createTimeEnd、updateTimeStart、updateTimeEnd 不能全部为空)",
      "CreateTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd cannot be all null(createTimeStart、createTimeEnd、updateTimeStart、updateTimeEnd 不能全部为空)"),
  CANNOT_FIND_METHOD(
      97000,
      "Can not find a method to invoke(找不到调用的方法)",
      "Can not find a method to invoke(找不到调用的方法)"),
  CREATECONTEXTID_EXCEPTION(97000, "", ""),
  DESCRIBE_ID(97001, "", ""),
  FAILED_FIND_SERIALIZER(
      97001, "Failed to find Serializer of(找不到序列化器)", "Failed to find Serializer of(找不到序列化器)"),
  OBJ_NOT_NULL(97001, "The obj not null(obj 不为空)", "The obj not null(obj 不为空)"),
  FAILED_FIND_DESERIALIZE(
      97001,
      "Failed to find deserialize of (找不到反序列化的)",
      "Failed to find deserialize of (找不到反序列化的)"),
  JSON_NOT_NULL(97001, "The json not null(json 不为空)", "The json not null(json 不为空)"),
  FAILED_GET_PROXY(
      97001,
      "Failed to get proxy of contextMapPersistence(获取 contextMapPersistence 代理失败)",
      "Failed to get proxy of contextMapPersistence(获取 contextMapPersistence 代理失败)"),
  CANNOT_DESERIZED_SOURCE(
      97001,
      "There are {} persistenceContextID that cannot be deserized from source(有 {} 个 persistenceContextID 无法从源中反序列化)",
      "There are {} persistenceContextID that cannot be deserized from source(有 {} 个 persistenceContextID 无法从源中反序列化)"),
  SET_CONTEXTID_REPEATEDLY(
      97001,
      "Do not set contextID repeatedly(不要重复设置 contextID)",
      "Do not set contextID repeatedly(不要重复设置 contextID)"),
  ONLY_ADMINS_ALLOWED(
      97018,
      "Only station admins are allowed(只允许站管理员)",
      "Only station admins are allowed(只允许站管理员)");

  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;
  /** Possible reasons for the error(错误可能出现的原因) */
  private String comment;

  CsCommonErrorCodeSummary(int errorCode, String errorDesc, String comment) {
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
    this.comment = comment;
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

  @Override
  public String toString() {
    return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
  }
}
