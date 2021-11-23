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
 
package org.apache.linkis.entrance.event

import org.apache.linkis.common.listener.{Event, EventListener}


trait EntranceEventListener extends EventListener {
  def onEvent(event: EntranceEvent): Unit

  override def onEventError(event: Event, t: Throwable): Unit = event match {
    case e: EntranceEvent => onEventError(e, t)
    case _ =>
  }

  def onEventError(event: EntranceEvent, t: Throwable): Unit
}
