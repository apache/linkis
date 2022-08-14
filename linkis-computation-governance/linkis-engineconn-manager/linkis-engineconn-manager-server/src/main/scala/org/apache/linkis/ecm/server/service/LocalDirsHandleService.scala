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

package org.apache.linkis.ecm.server.service

trait LocalDirsHandleService {

  def getEngineConnManagerHomeDir: String

  def getEngineConnWorkDir(user: String, ticketId: String, engineType: String): String

  def getEngineConnPublicDir: String

  def getEngineConnLogDir(user: String, ticketId: String, engineType: String): String

  def getEngineConnTmpDir(user: String, ticketId: String, engineType: String): String

  def cleanup(): Unit

}
