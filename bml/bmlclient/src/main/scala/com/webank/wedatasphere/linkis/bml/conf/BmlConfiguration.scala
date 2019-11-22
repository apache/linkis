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
package com.webank.wedatasphere.linkis.bml.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * created by cooperyang on 2019/5/15
  * Description:
  */
object BmlConfiguration {
  val GATEWAY_IP:CommonVars[String] =
    CommonVars[String]("wds.linkis.gateway.ip", "ip", "DWS gateway的ip地址")

  val GATEWAY_PORT:CommonVars[Int] =
    CommonVars[Int]("wds.linkis.gateway.port", 1000, "DWS gateway的端口")

  val DWS_VERSION:CommonVars[String] = CommonVars[String]("wds.linkis.bml.dws.version", "v1")

  val URL_PREFIX:CommonVars[String] =
    CommonVars[String]("wds.linkis.bml.url.prefix", "/api/rest_j/v1/bml", "bml服务的url前缀")

  val UPLOAD_URL:CommonVars[String] = CommonVars[String]("wds.linkis.bml.upload.url","upload")

  val UPDATE_VERSION_URL:CommonVars[String] = CommonVars[String]("wds.linkis.bml.updateVersion.url", "updateVersion","更新版本的url")

  val UPDATE_BASIC_URL:CommonVars[String] = CommonVars[String]("wds.linkis.bml.updateBasic.url", "updateBasic","更新基本信息的url")

  val RELATE_HDFS:CommonVars[String] = CommonVars[String]("wds.linkis.bml.relateHdfs.url", "relateHdfs", "关联hdfs资源的url")

  val RELATE_STORAGE:CommonVars[String] = CommonVars[String]("wds.linkis.bml.relateStorage.url", "relateStorage", "关联共享存储的url")

  val GET_RESOURCES:CommonVars[String] = CommonVars[String]("wds.linkis.bml.getResourceMsg.url","getResourceMsg", "获取资源的信息")

  val DOWNLOAD_URL:CommonVars[String] = CommonVars[String]("wds.linkis.bml.download.url", "download")

  val GET_VERSIONS_URL:CommonVars[String] = CommonVars[String]("wds.linkis.bml.getVersions.url", "getVersions")

  val GET_BASIC_URL:CommonVars[String] = CommonVars[String]("wds.linkis.bml.getBasic.url","getBasic")

  val AUTH_TOKEN_KEY:CommonVars[String] = CommonVars[String]("wds.linkis.bml.auth.token.key", "Validation-Code")

  val AUTH_TOKEN_VALUE:CommonVars[String] = CommonVars[String]("wds.linkis.bml.auth.token.value", "BML-AUTH")


}
