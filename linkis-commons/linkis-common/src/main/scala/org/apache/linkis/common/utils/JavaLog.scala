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
 * every define the logger by self this class is deprecated
 */
@deprecated("do not use this class for log")
class JavaLog {

  protected implicit lazy val logger = LoggerFactory.getLogger(getClass)

  def debug(message: Object): Unit =
    if (logger.isDebugEnabled && message != null) logger.debug(message.toString)

  def info(message: Object): Unit =
    if (logger.isInfoEnabled && message != null) logger.info(message.toString)

  def info(message: Object, t: Throwable): Unit =
    if (logger.isInfoEnabled && message != null) logger.info(message.toString, t)

  def warn(message: Object): Unit =
    if (logger.isWarnEnabled && message != null) logger.warn(message.toString)

  def warn(message: Object, t: Throwable): Unit =
    if (logger.isWarnEnabled && message != null) logger.warn(message.toString, t)

  def error(message: Object): Unit =
    if (logger.isErrorEnabled && message != null) logger.error(message.toString)

  def error(message: Object, t: Throwable): Unit =
    if (logger.isErrorEnabled && message != null) logger.error(message.toString, t)

}
