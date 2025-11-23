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

package org.apache.linkis.filesystem.exception;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class WorkspaceExceptionManager {

  private static final Map<String, String> desc =
      new HashMap<String, String>(32) {
        {
          put(
              "80001",
              "Requesting IO-Engine to initialize fileSystem failed(请求IO-Engine初始化fileSystem失败)!");
          put(
              "80002",
              "The user has obtained the filesystem for more than {0} ms. Please contact the administrator(用户获取filesystem的时间超过{0} ms，请联系管理员)");
          put(
              "80003",
              "User local root directory:{0} does not exist, please contact administrator to add(用户本地根目录:{0}不存在,请联系管理员添加)");
          put("80004", "The path:{0} is empty(路径:{0} 为空)!");
          put("80005", "The created folder name:{0} is duplicated(创建的文件夹名:{0} 重复)");
          put("80006", "The file name:{0} created is duplicated(创建的文件名:{0} 重复)");
          put("80007", "The renamed name:{0} is repeated,(重命名的名字:{0} 重复)");
          put("80008", "The deleted file or folder does not exist(删除的文件or文件夹不存在)!");
          put(
              "80009",
              "This user does not have permission to delete this file or folder(该用户无权删除此文件或文件夹)!");
          put(
              "80010",
              "The user: {0} has no permission to view the contents of the directory:{1}(该用户:{0}无权限查看该目录:{1}的内容).");
          put("80011", "Downloaded file: {0} does not exist(下载的文件:{0}不存在)");
          put("80012", "This user has no permission to read this file(该用户无权读取该文件)!");
          put("80013", "File: {0} does not exist(文件:{0}不存在)");
          put(
              "80014",
              "The user has no permission to modify the contents of this file and cannot save it(该用户无权限对此文件内容进行修改，无法保存)!");
          put("80015", "Unsupported resultset output type(不支持的结果集输出类型)");
          put("80016", "The file content is empty and cannot be imported(文件内容为空，不能进行导入操作)!");
          put(
              "80017",
              "The header of the file has no qualifiers. Do not check the first behavior header or set no qualifier(该文件的表头没有限定符，请勿勾选首行为表头或者设置无限定符)!");
          put("80018", "This user has no permission to read this log(该用户无权限读取此日志)!");
          put("80019", "ScriptContent is empty,this is normal(scriptContent 为空，这是正常的)!");
          put("80021", "Upload failed(上传失败)");
          put("80022", "Update failed(更新失败)");
          put("80023", "Download failed(下载失败)");
          put(
              "80024",
              "Non-tabular result sets cannot be downloaded as excel(非table类型的结果集不能下载为excel)");
          put(
              "80028",
              "The path exist special char,only support numbers, uppercase letters, underscores, Chinese(路径存在特殊字符,只支持数字,字母大小写,下划线,中文)");
          put("80029", "Empty dir(空目录)!");
          put("80030", "Creating user path: {0} failed(创建用户路径:{0}失败)");
          put("80031", "User: {0} not initialized(用户:{0}未初始化)");
          put(
              "80032",
              "The file size exceeds 30M and page viewing is currently not supported. Please download to view or view in a shared directory(文件大小超过30M，暂不支持页面查看。请下载查看或在共享目录中查看)");
          put(
              "80033",
              "The log file exceeds 30MB and is too large and cannot be opened, path : {0} (日志文件超过30M，文件太大暂不支持打开查看，文件地址：{0})");
          put(
              "80034",
              "The result set exceeds {0} rows and page viewing is currently not supported. Please download to view or view in the shared directory(结果集行数超过{0}行，暂不支持页面查看。请下载查看或在共享目录中查看)");
          put(
              "80035",
              "Parameter error, column index order is incorrect, please pass parameters in ascending order (参数错误，列索引顺序不正确或范围错误，请传入非复数并按升序传参)");
          put(
              "80036",
              "Parameter error, page size is incorrect, please pass in a number within [1-500] (分页参数错误，页码从1开始，页大小需在[1-500]范围内，获取的列索引需在实际结果集列数范围内)");
          put(
              "80037",
              "Parameter error, page size is incorrect, please pass in a number within [1-500] (参数错误，列筛选最多支持筛选50列)");
          put(
              "80038",
              "The name directory {0} specified by PKG-INFO does not exist. Please confirm that the {0} specified by PKG-INFO in the package matches the actual folder name (PKG-INFO指定Name目录{0}不存在，请确认包中PKG-INFO指定{0}和实际文件夹名称一致)");
          put("80039", "File upload failed, error message: {0} (文件上传失败，错误信息：{0})");
          put("80040", "{0} file not found in the archive ({0}文件不存在，请确认包中包含{0}文件)");
        }
      };

  public static WorkSpaceException createException(int errorCode, Object... format) {
    return new WorkSpaceException(
        errorCode, MessageFormat.format(desc.get(String.valueOf(errorCode)), format));
  }
}
