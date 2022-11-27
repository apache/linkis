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

package org.apache.linkis.common.utils

import java.math.RoundingMode
import java.text.DecimalFormat

import oshi.SystemInfo

object HardwareUtils {

  private val systemInfo = new SystemInfo

  private val hardware = systemInfo.getHardware

  private val THREE_DECIMAL = "0.000"

  def getAvailableMemory(): Long = {
    val globalMemory = hardware.getMemory
    globalMemory.getAvailable
  }

  def getMaxMemory(): Long = {
    val globalMemory = hardware.getMemory
    globalMemory.getTotal
  }

  def getMaxLogicalCore(): Int = {
    val globalProcessor = hardware.getProcessor
    globalProcessor.getLogicalProcessorCount
  }

  /**
   * 1 total 2 available
   *
   * @return
   */
  def getTotalAndAvailableMemory(): (Long, Long) = {
    val globalMemory = hardware.getMemory
    (globalMemory.getTotal, globalMemory.getAvailable)
  }

  /**
   * Get memory usage percentage Keep 3 decimal
   *
   * @return
   *   percent
   */
  def memoryUsage(): Double = {
    val memory = hardware.getMemory
    val memoryUsage = (memory.getTotal - memory.getAvailable) * 1.0 / memory.getTotal
    val df = new DecimalFormat(THREE_DECIMAL)
    df.setRoundingMode(RoundingMode.HALF_UP)
    df.format(memoryUsage).toDouble
  }

  /**
   * load average
   *
   * @return
   *   percent
   */
  def loadAverageUsage(): Double = {
    val loadAverage = Utils.tryCatch {
      OverloadUtils.getOSBean.getSystemLoadAverage
    } { case e: Exception =>
      val loadAverage = hardware.getProcessor.getSystemLoadAverage(1)(0)
      if (loadAverage.isNaN) -1 else loadAverage
    }

    val df = new DecimalFormat(THREE_DECIMAL)
    df.setRoundingMode(RoundingMode.HALF_UP)
    if (loadAverage <= 0) 0 else df.format(loadAverage / 100d).toDouble
  }

}
