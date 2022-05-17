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
 
package org.apache.linkis.common.utils

import org.slf4j.LoggerFactory


class JavaLog {
  //  if(!JavaLog.initialized) {
  //    JavaLog.initLock synchronized {
  //      if(!JavaLog.initialized) {
  //        JavaLog.initializeLogging
  //        JavaLog.initialized = true
  //      }
  //    }
  //  }
  protected implicit lazy val logger = LoggerFactory.getLogger(getClass)

  def debug(message: Object): Unit = if (logger.isDebugEnabled && message != null) logger.debug(message.toString)

  def info(message: Object): Unit = if(logger.isInfoEnabled && message != null) logger.info(message.toString)

  def info(message: Object, t: Throwable): Unit = if(logger.isInfoEnabled && message != null) logger.info(message.toString, t)

  def warn(message: Object): Unit = if(logger.isWarnEnabled && message != null) logger.warn(message.toString)

  def warn(message: Object, t: Throwable): Unit = if(logger.isWarnEnabled && message != null) logger.warn(message.toString, t)

  def error(message: Object): Unit = if(logger.isErrorEnabled && message != null) logger.error(message.toString)

  def error(message: Object, t: Throwable): Unit = if(logger.isErrorEnabled && message != null) logger.error(message.toString, t)

}
//private[utils] object JavaLog {
//  @volatile private var initialized = false
//  private val initLock = new Array[Byte](0)
//  Utils.tryQuietly {
//    val bridgeClass = Class.forName("org.slf4j.bridge.SLF4JBridgeHandler")
//    bridgeClass.getMethod("removeHandlersForRootLogger").invoke(null)
//    val installed = bridgeClass.getMethod("isInstalled").invoke(null).asInstanceOf[Boolean]
//    if (!installed) {
//      bridgeClass.getMethod("install").invoke(null)
//    }
//  }
//  if(!initialized) {
//    initLock synchronized {
//      if(!initialized) {
//        initializeLogging
//        initialized = true
//      }
//    }
//  }
//  private def initializeLogging: Unit = {
//    val binderClass = StaticLoggerBinder.getSingleton.getLoggerFactoryClassStr
//    val usingLog4j12 = "org.slf4j.impl.Log4jLoggerFactory".equals(binderClass)
//    if (usingLog4j12) {
//      val log4j12Initialized = LogManager.getRootLogger.getAllAppenders.hasMoreElements
//      if (!log4j12Initialized) {
//        val defaultLogProps = System.getProperty("log4j.configuration", "log4j.properties")
//        var url = Thread.currentThread.getContextClassLoader.getResource(defaultLogProps)
//        if(url == null) {
//          if(!new File(defaultLogProps).exists()) {
//            System.err.println(s"BDP Server was unable to load $defaultLogProps.")
//            return
//          }
//          url = if(defaultLogProps.startsWith("file:/")) new URL(defaultLogProps)
//            else new URL("file:///" + defaultLogProps)
//        }
//        PropertyConfigurator.configure(url)
//        System.err.println(s"Using BDP Server's log4j profile: $defaultLogProps.")
//      }
//    }
//  }
//}