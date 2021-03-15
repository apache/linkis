/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.common.utils

import org.slf4j.LoggerFactory

/**
  * Created by enjoyyin on 2016/8/22.
  */
trait Logging {

  protected lazy implicit val logger = LoggerFactory.getLogger(getClass)

  def trace(message: => String) = {
    if (logger.isTraceEnabled) {
      logger.trace(message.toString)
    }
  }

  def debug(message: => String): Unit = {
    if (logger.isDebugEnabled) {
      logger.debug(message.toString)
    }
  }

  def info(message: => String): Unit = {
    if (logger.isInfoEnabled) {
      logger.info(message.toString)
    }
  }

  def info(message: => String, t: Throwable): Unit = {
    logger.info(message.toString, t)
  }

  def warn(message: => String): Unit = {
    logger.warn(message.toString)
  }

  def warn(message: => String, t: Throwable): Unit = {
    logger.warn(message.toString, t)
  }

  def error(message: => String, t: Throwable): Unit = {
    logger.error(message.toString, t)
  }

  def error(message: => String): Unit = {
    logger.error(message.toString)
  }
}