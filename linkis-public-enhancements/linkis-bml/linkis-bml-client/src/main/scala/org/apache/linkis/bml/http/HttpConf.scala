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

package org.apache.linkis.bml.http

import org.apache.linkis.bml.conf.BmlConfiguration
import org.apache.linkis.common.conf.Configuration

object HttpConf {

  val gatewayInstance: String = Configuration.getGateWayURL()

  val urlPrefix: String = if (BmlConfiguration.URL_PREFIX.getValue.endsWith("/")) {
    BmlConfiguration.URL_PREFIX.getValue
      .substring(0, BmlConfiguration.URL_PREFIX.getValue.length - 1)
  } else BmlConfiguration.URL_PREFIX.getValue

  val uploadURL: String = urlPrefix + "/upload"
  val downloadURL: String = urlPrefix + "/download"
  val downloadShareURL: String = urlPrefix + "/" + "downloadShareResource"
  val updateProjectUrl: String = urlPrefix + "/" + "updateProjectUsers"
  val deleteURL: String = urlPrefix + "/deleteResource"
  val batchDeleteURL: String = urlPrefix + "/deleteResources"
  val updateVersionURL: String = urlPrefix + "/updateVersion"
  val relateHdfsURL: String = gatewayInstance + urlPrefix + "/relateHdfs"
  val relateStorageURL: String = gatewayInstance + urlPrefix + "/relateStorage"
  val getResourcesUrl: String = gatewayInstance + urlPrefix + "/getResourceMsg"
  val updateBasicUrl: String = gatewayInstance + urlPrefix + "/updateBasic"
  val getVersionsUrl: String = gatewayInstance + urlPrefix + "/getVersions"
  val getBasicUrl: String = gatewayInstance + urlPrefix + "/getBasic"
  val createProjectUrl: String = urlPrefix + "/" + "createBmlProject"
  val uploadShareResourceUrl: String = urlPrefix + "/" + "uploadShareResource"
  val updateShareResourceUrl: String = urlPrefix + "/" + "updateShareResource"
  val attachUrl: String = urlPrefix + "/" + "attachResourceAndProject"
  val changeOwnerUrl: String = urlPrefix + "/" + "changeOwner"
  val rollbackVersionUrl: String = urlPrefix + "/" + "rollbackVersion"
  val copyResourceUrl: String = urlPrefix + "/" + "copyResourceToAnotherUser"

}
