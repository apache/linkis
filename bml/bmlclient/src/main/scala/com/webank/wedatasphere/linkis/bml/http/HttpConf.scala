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
package com.webank.wedatasphere.linkis.bml.http

import com.webank.wedatasphere.linkis.bml.conf.BmlConfiguration

/**
  * created by cooperyang on 2019/5/15
  * Description:
  */
object HttpConf {
  val ip:String = BmlConfiguration.GATEWAY_IP.getValue
  val port:Int = BmlConfiguration.GATEWAY_PORT.getValue
  val schema:String = "http://"
  val gatewayInstance:String = schema + ip + ":" + port
  val urlPrefix:String = if (BmlConfiguration.URL_PREFIX.getValue.endsWith("/")) {
    BmlConfiguration.URL_PREFIX.getValue.substring(0, BmlConfiguration.URL_PREFIX.getValue.length - 1)
  }else BmlConfiguration.URL_PREFIX.getValue

  val uploadURL:String = urlPrefix + "/" + BmlConfiguration.UPLOAD_URL.getValue
  val downloadURL:String =  urlPrefix + "/" + BmlConfiguration.DOWNLOAD_URL.getValue
  val updateVersionURL:String = urlPrefix + "/" + BmlConfiguration.UPDATE_VERSION_URL.getValue
  val relateHdfsURL:String = gatewayInstance + urlPrefix + "/" + BmlConfiguration.RELATE_HDFS.getValue
  val relateStorageURL:String = gatewayInstance + urlPrefix + "/" + BmlConfiguration.RELATE_STORAGE.getValue
  val getResourcesUrl:String = gatewayInstance + urlPrefix + "/" + BmlConfiguration.GET_RESOURCES.getValue
  val updateBasicUrl:String = gatewayInstance + urlPrefix + "/" + BmlConfiguration.UPDATE_BASIC_URL.getValue
  val getVersionsUrl:String = gatewayInstance + urlPrefix + "/" + BmlConfiguration.GET_VERSIONS_URL.getValue
  val getBasicUrl:String = gatewayInstance + urlPrefix + "/" + BmlConfiguration.GET_BASIC_URL.getValue
  def main(args: Array[String]): Unit = {
    println(uploadURL)
    println(downloadURL)
    println(updateVersionURL)
    println(relateHdfsURL)
    println(relateStorageURL)
    println(getResourcesUrl)
    println(updateBasicUrl)
  }


}
