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

package org.apache.linkis.bml.errorcode;

public enum BmlServerErrorCodeSummary {
  CANNOT_ACCESSED_EXPIRED(
      60036, "Store cannot be accessed without login or expired login(未登录或登录过期，无法访问物料库)"),
  FIRST_UPLOAD_FAILED(60050, "The first upload of the resource failed(首次上传资源失败)"),
  FAILED_UPDATE_RESOURCES(60051, "Failed to update resources(更新资源失败)"),
  BML_QUERY_FAILEXCEPTION(70068, ""),
  FAILED_DOWNLOAD_RESOURCE(70068, "Failed to download the resource(下载资源失败)"),
  QUERY_VERSION_FAILED(
      70068, "Sorry, the query for version information failed(抱歉，查询版本信息失败)(下载资源失败)"),
  FAILED_ALL_INFORMATION(70068, "Failed to get all system resource information(获取系统所有资源信息失败)"),
  DELETE_VERSION_FAILED(70068, "Failed to delete the resource version(删除资源版本失败)"),
  DELETE_OPERATION_FAILED(70068, "Delete resource operation failed(删除资源操作失败)"),
  BULK_DELETE_FAILED(70068, "The batch delete resource operation failed(批量删除资源操作失败)"),
  RESOURCEID_BEEN_DELETED(
      70068, "ResourceID:{0} is empty, illegal or has been deleted (resourceId{0}:为空,非法或者已被删除!)"),
  VERSION_BEEN_DELETED(
      70068, "version:{0} is empty, illegal or has been deleted (version:{0} 为空,非法或者已被删除!)"),
  FAILED_RESOURCE_BASIC(70068, "Failed to obtain resource basic information (获取资源基本信息失败)"),
  EXPIRED_CANNOT_DOWNLOADED(78531, "has expired and cannot be downloaded(已经过期,不能下载)"),
  NOT_HAVE_PERMISSION(75569, "You do not have permission to download this resource (您没有权限下载此资源)"),
  NOT_PROJECT_PERMISSION(
      75570,
      "{0} does not have edit permission on project {1}, upload resource failed ({0} 对工程 {1} 没有编辑权限, 上传资源失败)"),
  BML_SERVER_PARA_ID(78361, ""),

  MATERIAL_NOTUPLOADED_DELETED(
      78361,
      "If the material has not been uploaded or has been deleted, please call the upload interface first(resourceId:{}之前未上传物料,或物料已被删除,请先调用上传接口.!)"),
  PARAMETERS_IS_NULL(
      78361, "Batch deletion of resourceIDS parameters is null(批量删除资源操作传入的resourceIds参数为空)"),
  BULK_DELETION_PARAMETERS(
      78361, "Batch deletion of unpassed resourceIDS parameters(批量删除未传入resourceIds参数)"),
  NOT_BALID_RESOURCEID(78361, "You did not pass a valid ResourceID(您未传入有效的resourceId)"),
  ILLEGAL_OR_DELETED(
      78361,
      "The passed ResourceID or version is illegal or has been deleted(传入的resourceId或version非法,或已删除)"),
  DELETE_SPECIFIED_VERSION(
      78361,
      "ResourceID and version are required to delete the specified version(删除指定版本，需要指定resourceId 和 version)"),
  SUBMITTED_INVALID(78361, "The ResourceID you submitted is invalid (您提交的resourceId无效)"),

  PARAMETER_IS_ILLEGAL(
      78361,
      "The basic information of the resource is not passed into the ResourceId parameter or the parameter is illegal(获取资源基本信息未传入resourceId参数或参数非法)");

  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;

  BmlServerErrorCodeSummary(int errorCode, String errorDesc) {
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
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

  @Override
  public String toString() {
    return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
  }
}
