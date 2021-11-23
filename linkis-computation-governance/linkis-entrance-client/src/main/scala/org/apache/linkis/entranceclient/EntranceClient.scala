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
 
package org.apache.linkis.entranceclient

import org.apache.linkis.entranceclient.execute.ClientJob

trait EntranceClient {

  def getEntranceClientName: String

  def execute(code: String, user: String, creator: String): Boolean

  def execute(code: String, user: String, creator: String,
              params: java.util.Map[String, Any]): Boolean

  def executeJob(code: String, user: String, creator: String): String

  def executeJob(code: String, user: String, creator: String,
                 params: java.util.Map[String, Any]): String

  def getJob(jobId: String): Option[ClientJob]

  def executeResult(code: String, user: String, creator: String): Array[String]

  def executeResult(code: String, user: String, creator: String,
                    params: java.util.Map[String, Any]): Array[String]

}
