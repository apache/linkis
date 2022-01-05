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
 
package org.apache.linkis.ecm.server.service.impl

import java.io.File
import java.nio.file.Paths

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.ecm.server.conf.ECMConfiguration._
import org.apache.linkis.ecm.server.service.LocalDirsHandleService


class DefaultLocalDirsHandleService extends LocalDirsHandleService {

  // TODO: 检测当前磁盘的健康状态，如果目录满了，需要上报am

  override def cleanup(): Unit = ???


  override def getEngineConnManagerHomeDir: String = ECM_HOME_DIR

  override def getEngineConnWorkDir(user: String, ticketId: String): String = new FsPath(Paths.get(ENGINECONN_ROOT_DIR, user, "workDir", ticketId).toFile.getPath).getPath

  override def getEngineConnPublicDir: String = ENGINECONN_PUBLIC_DIR

  override def getEngineConnLogDir(user: String, ticketId: String): String = s"${getEngineConnWorkDir(user, ticketId)}${File.separator}logs"

  override def getEngineConnTmpDir(user: String, ticketId: String): String = s"${getEngineConnWorkDir(user, ticketId)}${File.separator}tmp"


}


