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

package org.apache.linkis.engineplugin.hive.log

import java.util

import org.slf4j.LoggerFactory

object LogHelper {

  private val logPattern =
    """Stage-(\d+)\s+map\s+=\s+(\d+)%,\s+reduce\s+=\s+(\d+)%""".r.unanchored

  private val logger = LoggerFactory.getLogger(getClass)
  private val appidPattern = """The url to track the job: http://(.*)/proxy/(.*)/""".r.unanchored
  private val successPattern = """change state Running => Succeed""".r.unanchored
  private val failedPattern = """change state Running => Failed""".r.unanchored

  def checkPattern(log: String): java.util.List[HiveProgress] = {
    val hiveProgresses = new util.ArrayList[HiveProgress]()
    logPattern findAllIn log foreach {
      case logPattern(stage, map, reduce) =>
        val hiveProgress =
          new HiveProgress(Integer.parseInt(stage), Integer.parseInt(map), Integer.parseInt(reduce))
        hiveProgresses.add(hiveProgress)
      case _ => logger.warn(s"log $log pattern can not be matched")
    }
    hiveProgresses
  }

  def getYarnAppid(log: String): String = {
    log match {
      case appidPattern(ip, appid) => appid
      case _ => null
    }
  }

  /**
   * this method is to check one single sql has run completed or not via log matching
   * @param log
   *   logs
   * @return
   *   true is completed, false is not
   */
  def matchCompletedPattern(log: String): Boolean = {
    log match {
      case successPattern() | failedPattern() => true
      case _ => false
    }
  }

  def main(args: Array[String]): Unit = {
    val log = "ssssx"
    // scalastyle:off println
    println(matchCompletedPattern(log))
    // scalastyle:on println
  }

}
