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

package org.apache.linkis.monitor.core.pac

import org.apache.linkis.monitor.core.ob.{Event, Observer}
import org.apache.linkis.monitor.core.ob.Observer

import java.util

trait ScanRule {

  def getName(): String

  /**
   * register an observer to trigger if this rule is matched
   *
   * @param observer
   */
  def addObserver(observer: Observer): Unit

  /**
   * return registered event
   *
   * @return
   */
  def getHitEvent(): Event

  /**
   * if data match the pattern, return true and trigger observer should call isMatched()
   *
   * @param data
   * @return
   */
  def triggerIfMatched(data: util.List[ScannedData]): Boolean
}
