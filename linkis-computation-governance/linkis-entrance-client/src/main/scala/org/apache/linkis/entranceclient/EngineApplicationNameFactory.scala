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

import org.apache.linkis.entranceclient.conf.ClientConfiguration.{CLIENT_ENGINE_MANAGER_SPRING_APPLICATION_NAME, CLIENT_ENGINE_SPRING_APPLICATION_NAME}

trait EngineApplicationNameFactory {
  private var engineApplicationName: String = CLIENT_ENGINE_SPRING_APPLICATION_NAME.getValue

  def setEngineApplicationName(engineApplicationName: String): Unit = this.engineApplicationName = engineApplicationName
  def getEngineApplicationName: String = engineApplicationName

  def isEngineApplicationNameEmpty: Boolean = engineApplicationName == CLIENT_ENGINE_SPRING_APPLICATION_NAME.getValue

}
trait EngineManagerApplicationNameFactory {
  private var engineManagerApplicationName: String = CLIENT_ENGINE_MANAGER_SPRING_APPLICATION_NAME.getValue
  def setEngineManagerApplicationName(engineManagerApplicationName: String): Unit = this.engineManagerApplicationName = engineManagerApplicationName
  def getEngineManagerApplicationName: String = engineManagerApplicationName
  def isEngineManagerApplicationNameEmpty: Boolean = engineManagerApplicationName == CLIENT_ENGINE_MANAGER_SPRING_APPLICATION_NAME.getValue
}