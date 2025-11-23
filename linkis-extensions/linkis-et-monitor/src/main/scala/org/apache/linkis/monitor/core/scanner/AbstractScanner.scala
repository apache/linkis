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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.monitor.core.ob.{Event, Observer}
import org.apache.linkis.monitor.core.pac._
import org.apache.linkis.monitor.jobhistory.exception.AnomalyScannerException

import java.util
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractScanner extends AnomalyScanner with Logging {
  private val buffer: ScanBuffer = new ScanBuffer

  private val dataFetcherIdx: AtomicInteger =
    new AtomicInteger(0) // mark next fetcher for sequentially produce data

  private val dataFetcherList: CopyOnWriteArrayList[DataFetcher] =
    new CopyOnWriteArrayList[DataFetcher]

  private val scanRuleList: CopyOnWriteArrayList[ScanRule] = new CopyOnWriteArrayList[ScanRule]

  /**
   * Producer
   */
  override def addDataFetcher(fetcher: DataFetcher): Unit = {
    if (fetcher != null) {
      dataFetcherList.add(fetcher)
    } else {
      logger.warn("ignore null DataFetcher")
    }
  }

  override def addDataFetchers(fetchers: util.List[DataFetcher]): Unit = {
    if (fetchers != null && fetchers.size != 0) {
      dataFetcherList.addAll(fetchers)
    } else {
      logger.warn("ignore null or empty DataFetcher")
    }
  }

  override def getDataFetchers: util.List[DataFetcher] = dataFetcherList

  /**
   * directly feed data to buffer
   */
  override def feedData(data: util.List[ScannedData]): Unit = {
    if (data != null && data.size != 0) {
      buffer.write(data)
    } else {
      logger.warn("Fed with null or empty data")
    }
  }

  /**
   * Returns a buffer that allows read/write simultaneously buffer is allowed to be written by other
   * thread
   */
  override def getBuffer(): ScanBuffer = buffer

  /**
   * add rules to scanner
   */
  override def addScanRule(rule: ScanRule): Unit = {
    if (rule != null) {
      scanRuleList.add(rule)
    } else {
      logger.warn("ignore null ScanRule")
    }
  }

  override def addScanRules(rules: util.List[ScanRule]): Unit = {
    if (rules != null && rules.size != 0) {
      scanRuleList.addAll(rules)
    } else {
      logger.warn("ignore null or empty ScanRule")
    }
  }

  override def getScanRules(): util.List[ScanRule] = scanRuleList

  /**
   * blocking call, scan and analyze until all dataFetchers are accessed once
   */
  override def run(): Unit = {
    if (dataFetcherList.size() == 0) {
      throw new AnomalyScannerException(21304, "attempting to run scanner with empty dataFetchers")
    }
    if (buffer == null) {
      throw new AnomalyScannerException(21304, "attempting to run scanner with null buffer")
    }
    if (scanRuleList.size == 0) {
      throw new AnomalyScannerException(21304, "attempting to run scanner with empty rules")
    }
    while (dataFetcherIdx.get() < dataFetcherList.size()) {
      scanOneIteration()
      analyzeOneIteration()
    }
  }

  /**
   *   1. scan data for 1 iteration 2. should be a blocking call 3. see if [[ScanRule]] is matched
   *      4. trigger [[Event]] and inform observer
   */
  override def scanOneIteration(): Unit = {
    val idx = dataFetcherIdx.getAndIncrement()
    val fetcher = dataFetcherList.get(idx)
    if (fetcher != null) {
      val rawData = fetcher.getData()
      logger.info("scanned " + rawData.size + " data. Rule: " + fetcher.getName);
      if (rawData != null && rawData.size != 0) {
        buffer.write(new BaseScannedData(fetcher.getName, rawData))
      }
    } else {
      logger.warn("ignored null fetcher!!")
    }
  }

  /**
   *   1. should be a blocking call 2. read from [[ScanBuffer]] 2. see if [[ScanRule]] is matched 3.
   *      trigger [[Observer]]
   */
  override def analyzeOneIteration(): Unit = {
    val dataToAnalyze = buffer.drain()
    if (dataToAnalyze != null && dataToAnalyze.size() != 0) {
      val len = scanRuleList.size()
      for (i <- 0 until len) {
        val scanRule = scanRuleList.get(i)
        if (scanRule != null) {
          logger.info("analyzing " + dataToAnalyze.size + " data. Rule: " + scanRule.getName)
          scanRule.triggerIfMatched(dataToAnalyze)
        } else {
          logger.warn("found empty or null ScanRule")
        }
      }
    } else {
      logger.info("analyzed 0 data.")
    }
  }

  /**
   *   1. should be non-blocking 2. keeps calling scanOneIteration() and analyzeOneIteration() utils
   *      stop() is called
   */
  override def start(): Unit = {}

}
