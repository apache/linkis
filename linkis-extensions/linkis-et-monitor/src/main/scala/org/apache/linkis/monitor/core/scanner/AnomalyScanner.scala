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

package org.apache.linkis.monitor.core.scanner

import org.apache.linkis.monitor.core.ob.Event
import org.apache.linkis.monitor.core.pac.{DataFetcher, ScanBuffer, ScannedData, ScanRule}
import org.apache.linkis.monitor.core.pac.DataFetcher

import java.util

/**
 * A Scanner that:
 *   1. scan a datasource using [[DataFetcher]], write data into [[ScanBuffer]] 2. read data from
 *      [[ScanBuffer]] see if [[ScanRule]] is matched 3. trigger [[Event]] in [[ScanRule]] and
 *      inform observer
 */
trait AnomalyScanner {

  /**
   * Producer
   */
  def addDataFetcher(dataFetcher: DataFetcher): Unit

  def addDataFetchers(dataFetchers: util.List[DataFetcher]): Unit

  def getDataFetchers: util.List[DataFetcher]

  /**
   * directly feed data to buffer
   */
  def feedData(data: util.List[ScannedData]): Unit

  /**
   * Buffer
   */

  /**
   * add rules to scanner
   */
  def addScanRule(rule: ScanRule): Unit

  def addScanRules(rules: util.List[ScanRule]): Unit

  /**
   * Consumer
   */

  def getScanRules(): util.List[ScanRule]

  /**
   * scan and analyze for 1 iteration
   */
  def run(): Unit

  /**
   *   1. should be non-blocking 2. keeps calling scan() utils stop() is called
   */
  def start(): Unit

  def shutdown(): Unit

  /**
   *   1. should be a blocking call 2. call [[DataFetcher]] to read data 3. write result to
   *      [[ScanBuffer]]
   */
  protected def scanOneIteration(): Unit

  /**
   * Returns a buffer that allows read/write simultaneously buffer is allowed to be written by other
   * thread
   */
  protected def getBuffer(): ScanBuffer

  /**
   *   1. should be a blocking call 2. read from [[ScanBuffer]] 2. see if [[ScanRule]] is matched 3.
   *      trigger [[[[org.apache.linkis.tools.core.ob.Observer]]]]
   */
  protected def analyzeOneIteration(): Unit

}
