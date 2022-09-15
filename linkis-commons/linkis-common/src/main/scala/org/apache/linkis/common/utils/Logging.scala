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

import org.slf4j.LoggerFactory

/**
 * use the class's method will miss the real info line number, so deprecated all the method and this
 * trait will be deprecated too
 */
trait Logging {

  protected lazy implicit val logger = LoggerFactory.getLogger(getClass)

  @deprecated
  def trace(message: => String): Unit = {
    if (logger.isTraceEnabled) {
      logger.trace(message)
    }
  }

  @deprecated
  def debug(message: => String): Unit = {
    if (logger.isDebugEnabled) {
      logger.debug(message)
    }
  }

  @deprecated
  def info(message: => String): Unit = {
    if (logger.isInfoEnabled) {
      logger.info(message)
    }
  }

  @deprecated
  def info(message: => String, t: Throwable): Unit = {
    logger.info(message, t)
  }

  @deprecated
  def warn(message: => String): Unit = {
    logger.warn(message)
  }

  @deprecated
  def warn(message: => String, t: Throwable): Unit = {
    logger.warn(message, t)
  }

  @deprecated
  def error(message: => String, t: Throwable): Unit = {
    logger.error(message, t)
  }

  @deprecated
  def error(message: => String): Unit = {
    logger.error(message)
  }

}
