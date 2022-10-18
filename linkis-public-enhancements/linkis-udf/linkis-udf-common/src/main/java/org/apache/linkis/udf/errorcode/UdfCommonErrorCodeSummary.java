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

package org.apache.linkis.udf.errorcode;

public enum UdfCommonErrorCodeSummary {
  UDF_EXCEPTION_ID(202011, "", ""),
  USER_NAME_EMPTY(202011, "UserName is Empty(用户名为空)", "UserName is Empty(用户名为空)"),
  UDFID_EMPTY(202011, "UdfId is empty(udfId为空)", "UdfId is empty(udfId为空)"),
  ONLY_MANAGER_SHARE(
      202011,
      "Only manager can share udf(只有manager可以共享 udf)",
      "Only manager can share udf(只有manager可以共享 udf)"),
  ONLY_MANAGER_PUBLISH(
      202011,
      "Only manager can publish udf(只有manager可以发布 udf)",
      "Only manager can publish udf(只有manager可以发布 udf)"),
  NOT_FIND_UDF(
      202011,
      "Can't find udf by this id(通过这个 id 找不到 udf)",
      "Can't find udf by this id(通过这个 id 找不到 udf)"),

  USERLIST_NOT_NULL(
      202011,
      "UserList cat not be null(userList cat 不为空)",
      "UserList cat not be null(userList cat 不为空)"),
  HANDOVER_NOT_NULL(
      202011,
      "The handover user can't be null(切换用户不能为空)",
      "The handover user can't be null(切换用户不能为空)"),
  ADMIN_NOT_HANDOVER_UDF(
      202011,
      "Admin users cannot hand over UDFs to regular users.(管理员用户不能移交UDF给普通用户！)",
      "Admin users cannot hand over UDFs to regular users.(管理员用户不能移交UDF给普通用户！)"),
  MUST_OPERATION_USER(
      202011,
      "CreateUser must be consistent with the operation user(创建用户必须和操作用户一致)",
      "CreateUser must be consistent with the operation user(创建用户必须和操作用户一致)"),
  UDF_SAME_NAME(
      202011,
      "The name of udf is the same name. Please rename it and rebuild it.(udf的名字重名，请改名后重建)",
      "The name of udf is the same name. Please rename it and rebuild it.(udf的名字重名，请改名后重建)"),
  CATEGORY_NOT_EMPTY(
      202011,
      "Category name cannot be empty!(分类名不能为空！)",
      "Category name cannot be empty!(分类名不能为空！)"),
  FILE_READ_EXCEPTION(202011, "File read exception(文件读取异常):", "File read exception(文件读取异常):"),
  UPLOAD_BML_FAILED(
      202011,
      "Upload to bml failed, file path is(上传到bml失败，文件路径为):",
      "Upload to bml failed, file path is(上传到bml失败，文件路径为):"),
  UPLOAD_BML_FAILED_MSG(
      202011,
      "Upload to bml failed, msg(上传到bml异常！msg):",
      "Upload to bml failed, msg(上传到bml异常！msg):"),
  JAR_SAME_NAME(
      202011,
      "A jar with the same name already exists in the user's udf!(用户的udf已存在同名jar！)",
      "A jar with the same name already exists in the user's udf!(用户的udf已存在同名jar！)"),
  ID_NOT_EMPTY(202011, "id Can not be empty(不能为空)", "id Can not be empty(不能为空)"),
  NOT_FUNCTION_DIRECTORY(
      202011,
      "This user does not have a personal function directory!(该用户没有个人函数目录!)",
      "This user does not have a personal function directory!(该用户没有个人函数目录!)"),
  OLD_UDF_FOUND(
      202011,
      "No old UDF found by this ID(此 ID 未找到旧 UDF)",
      "No old UDF found by this ID(此 ID 未找到旧 UDF)"),
  CURRENT_MUST_MODIFIED(
      202011,
      "Current user must be consistent with the modified user(当前用户必须和修改用户一致)",
      "Current user must be consistent with the modified user(当前用户必须和修改用户一致)"),
  CURRENT_MUST_MODIFIED_CREATED(
      202011,
      "Current user must be consistent with the user created(当前用户必须和创建用户一致)",
      "Current user must be consistent with the user created(当前用户必须和创建用户一致)"),
  UDF_MODIFICATION_NOT_ALLOWED(
      202011,
      "UDF type modification is not allowed(不允许修改 UDF 类型)",
      "UDF type modification is not allowed(不允许修改 UDF 类型)"),
  NOT_ALLOWED_MODIFIED(
      202011,
      "The name of udf is not allowed modified.(udf的名字禁止修改！)",
      "The name of udf is not allowed modified.(udf的名字禁止修改！)"),
  NOT_FOUND_VERSION(
      202011,
      "Can't found latestVersion for the udf(找不到 udf 的最新版本)",
      "Can't found latestVersion for the udf(找不到 udf 的最新版本)"),
  HANDOVER_SAME_NAME_UDF(
      202011,
      "The handoverUser has same name udf.(被移交用户包含重名udf)",
      "The handoverUser has same name udf.(被移交用户包含重名udf)"),
  OPERATION_NOT_SUPPORTED(
      202011,
      "The publish operation is not supported for non-shared udfs!(非共享udf不支持发布操作！)",
      "The publish operation is not supported for non-shared udfs!(非共享udf不支持发布操作！)"),
  ROLLBACK_VERSION_ABNORMAL(
      202011,
      "Bml rollback version abnormal(bml回滚版本异常)",
      "Bml rollback version abnormal(bml回滚版本异常)"),
  JAR_NOT_DOWNLOADING(
      202011,
      "The udf of jar type content does not support downloading and viewing(jar类型内容的udf不支持下载查看)",
      "The udf of jar type content does not support downloading and viewing(jar类型内容的udf不支持下载查看)"),
  CANNOT_DELETED(
      202011,
      "The shared udf is loaded by the user and cannot be deleted(该共享udf被用户加载，不能删除)",
      "The shared udf is loaded by the user and cannot be deleted(该共享udf被用户加载，不能删除)"),
  SAME_NAME_FUNCTION(
      202011,
      "There is a Jar package function with the same name(存在同名的Jar包函数)： ",
      "There is a Jar package function with the same name(存在同名的Jar包函数)： "),
  NOT_SHARING_YOURSELF(
      202011,
      "Do not support sharing to yourself!(不支持分享给自己!) ",
      "Do not support sharing to yourself!(不支持分享给自己!)"),
  DUPLICATE_FILE_NAME(202011, "Duplicate file name(文件名重复)", "Duplicate file name(文件名重复)"),
  FAILED_COPY_RESOURCE(
      202011,
      "Failed to copy resource to anotherUser(无法将资源复制到另一个用户):",
      "Failed to copy resource to anotherUser(无法将资源复制到另一个用户):"),
  SHARED_USERS_NOT_EMPTY(
      202011,
      "Shared users cannot contain empty usernames(共享用户不能包含空用户名)",
      "Shared users cannot contain empty usernames(共享用户不能包含空用户名)"),
  CONTAIN_NUMERIC_SCORES(
      202011,
      "Username can only contain alphanumeric underscores(用户名只能包含字母数字下划线)",
      "Username can only contain alphanumeric underscores(用户名只能包含字母数字下划线)"),
  CONTAINS_SAME_NAME(
      202011,
      "User contains the same name udf!(用户包含同名udf！)",
      "User contains the same name udf!(用户包含同名udf！)"),
  ALREADY_EXISTS_SHARE(
      202011,
      "Shared udf name Already exists, please edit the name and re-share(分享的udf的名字已存在，请修改名字后重新进行分享)",
      "Shared udf name Already exists, please edit the name and re-share(分享的udf的名字已存在，请修改名字后重新进行分享)"),
  SHARED_USER_EXPIRE(
      202011,
      "Only the udf loaded by the shared user can be set to expire(只有被共享用户加载的udf可以设置过期)",
      "Only the udf loaded by the shared user can be set to expire(只有被共享用户加载的udf可以设置过期)"),
  DIRECTORY_DIRECTORIES(
      202011,
      "User There are two root directory directories,user(用户存在两个根目录目录,用户):",
      "User There are two root directory directories,user(用户存在两个根目录目录,用户):"),
  DIRECTORY_PARENT(
      202011,
      "User the parent directory is not yours,user(用户父目录不是你的,用户):",
      "User the parent directory is not yours,user(用户父目录不是你的,用户):");

  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;
  /** Possible reasons for the error(错误可能出现的原因) */
  private String comment;

  UdfCommonErrorCodeSummary(int errorCode, String errorDesc, String comment) {
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
