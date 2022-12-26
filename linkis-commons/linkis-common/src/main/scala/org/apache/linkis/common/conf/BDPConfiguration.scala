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

package org.apache.linkis.common.conf

import org.apache.linkis.common.utils.{Logging, Utils}

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import java.io.{File, FileInputStream, InputStream, IOException}
import java.util.Properties
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

private[conf] object BDPConfiguration extends Logging {

  val DEFAULT_PROPERTY_FILE_NAME = "linkis.properties"

  val DEFAULT_SERVER_CONF_FILE_NAME = "linkis-server.properties"

  val DEFAULT_CONFIG_HOT_LOAD_DELAY_MILLS = 3 * 60 * 1000L

  private val extractConfig = new Properties
  private val config = new Properties
  private val sysProps = sys.props
  private val env = sys.env

  private val configList = new ArrayBuffer[String]
  private val configReload = new Properties
  private val lock = new ReentrantReadWriteLock()

  private def init: Unit = {

    // load pub linkis conf
    val propertyFile = sysProps.getOrElse("wds.linkis.configuration", DEFAULT_PROPERTY_FILE_NAME)
    val configFileURL = getClass.getClassLoader.getResource(propertyFile)
    if (configFileURL != null && new File(configFileURL.getPath).exists) {
      logger.info(
        s"******************* Notice: The Linkis configuration file is $propertyFile ! *******************"
      )
      initConfig(config, configFileURL.getPath)
      configList.append(configFileURL.getPath)
    } else {
      logger.warn(
        s"************ Notice: The Linkis configuration file $propertyFile does not exist! *******************"
      )
    }

    // load pub linkis conf
    val serverConf = sysProps.getOrElse("wds.linkis.server.conf", DEFAULT_SERVER_CONF_FILE_NAME)
    val serverConfFileURL = getClass.getClassLoader.getResource(serverConf)
    if (serverConfFileURL != null && new File(serverConfFileURL.getPath).exists) {
      logger.info(
        s"*********************** Notice: The Linkis serverConf file is $serverConf ! ******************"
      )
      initConfig(config, serverConfFileURL.getPath)
      configList.append(serverConfFileURL.getPath)
    } else {
      logger.warn(
        s"**************** Notice: The Linkis serverConf file $serverConf does not exist! *******************"
      )
    }

    // load  server confs
    val propertyFileOptions = sysProps.get("wds.linkis.server.confs")
    if (propertyFileOptions.isDefined) {
      val propertyFiles = propertyFileOptions.get.split(",")
      propertyFiles.foreach { propertyF =>
        val configFileURL = getClass.getClassLoader.getResource(propertyF)
        if (configFileURL != null && new File(configFileURL.getPath).exists) {
          logger.info(
            s"************** Notice: The Linkis server.confs  is file $propertyF ****************"
          )
          initConfig(config, configFileURL.getPath)
          configList.append(configFileURL.getPath)
        } else {
          logger.warn(
            s"********** Notice: The Linkis server.confs file $propertyF does not exist! **************"
          )
        }
      }
    }

    // init hot-load config task
    val hotLoadTask = new Runnable {
      override def run(): Unit = {
        var tmpConfigPath = ""
        var tmpConfig = new Properties()
        Utils.tryCatch {
          // refresh configuration
          configList.foreach(configPath => {
            if (logger.isDebugEnabled()) {
              logger.debug(s"reload config file : ${configPath}")
            }
            tmpConfigPath = configPath
            initConfig(tmpConfig, configPath)
          })
        } { case e: Exception =>
          logger.error(s"reload config file : ${tmpConfigPath} failed, because : ${e.getMessage}")
          logger.warn("Will reset config to origin config.")
          tmpConfig = config
        }
        lock.writeLock().lock()
        tmpConfig.asScala.foreach(keyValue => configReload.setProperty(keyValue._1, keyValue._2))
        lock.writeLock().unlock()
      }
    }
    Utils.defaultScheduler.scheduleWithFixedDelay(
      hotLoadTask,
      3000L,
      DEFAULT_CONFIG_HOT_LOAD_DELAY_MILLS,
      TimeUnit.MILLISECONDS
    )
    logger.info("hotload config task inited.")
  }

  Utils.tryCatch {
    init
  } { e: Throwable =>
    logger.warn("Failed to init conf", e)
  }

  private def initConfig(config: Properties, filePath: String) {
    var inputStream: InputStream = null

    Utils.tryFinally {
      Utils.tryCatch {
        inputStream = new FileInputStream(filePath)
        config.load(inputStream)
      } { case e: IOException =>
        logger.error("Can't load " + filePath, e)
      }
    } {
      IOUtils.closeQuietly(inputStream)
    }
  }

  def getOption(key: String, hotload: Boolean = false): Option[String] = {
    if (extractConfig.containsKey(key)) {
      return Some(extractConfig.getProperty(key))
    }
    var value = ""
    if (hotload) {
      lock.readLock().lock()
      value = configReload.getProperty(key)
      lock.readLock().unlock()
    } else {
      value = config.getProperty(key)
    }
    if (StringUtils.isNotEmpty(value)) {
      return Some(value)
    }
    val propsValue = sysProps.get(key).orElse(sys.props.get(key))
    if (propsValue.isDefined) {
      return propsValue
    }
    env.get(key)
  }

  def properties: Properties = {
    val props = new Properties
    mergePropertiesFromMap(props, env)
    mergePropertiesFromMap(props, sysProps.toMap)
    mergePropertiesFromMap(props, config.asScala.toMap)
    mergePropertiesFromMap(props, extractConfig.asScala.toMap)
    props
  }

  def hotProperties(): Properties = {
    val props = new Properties
    mergePropertiesFromMap(props, env)
    mergePropertiesFromMap(props, sysProps.toMap)
    lock.readLock().lock()
    mergePropertiesFromMap(props, configReload.asScala.toMap)
    lock.readLock().unlock()
    mergePropertiesFromMap(props, extractConfig.asScala.toMap)
    props
  }

  def mergePropertiesFromMap(props: Properties, mapProps: Map[String, String]): Unit = {
    mapProps.foreach { case (k, v) => props.put(k, v) }
  }

  def getOption[T](commonVars: CommonVars[T], hotload: Boolean): Option[T] = {
    if (hotload) {
      val value = BDPConfiguration.getOption(commonVars.key, hotload = true)
      if (value.isEmpty) Option(commonVars.defaultValue)
      else formatValue(commonVars.defaultValue, value)
    } else {
      if (commonVars.value != null) {
        Option(commonVars.value)
      } else {
        val value = BDPConfiguration.getOption(commonVars.key)
        if (value.isEmpty) Option(commonVars.defaultValue)
        else formatValue(commonVars.defaultValue, value)
      }
    }
  }

  def getOption[T](commonVars: CommonVars[T]): Option[T] = {
    if (commonVars.value != null) {
      Option(commonVars.value)
    } else {
      val value = BDPConfiguration.getOption(commonVars.key)
      if (value.isEmpty) Option(commonVars.defaultValue)
      else formatValue(commonVars.defaultValue, value)
    }
  }

  private[common] def formatValue[T](defaultValue: T, value: Option[String]): Option[T] = {
    if (value.isEmpty || value.exists(StringUtils.isEmpty)) return Option(defaultValue)
    val formattedValue = defaultValue match {
      case _: String => value
      case _: Byte => value.map(_.toByte)
      case _: Short => value.map(_.toShort)
      case _: Char => value.map(_.toCharArray.apply(0))
      case _: Int => value.map(_.toInt)
      case _: Long => value.map(_.toLong)
      case _: Float => value.map(_.toFloat)
      case _: Double => value.map(_.toDouble)
      case _: Boolean => value.map(_.toBoolean)
      case _: TimeType => value.map(new TimeType(_))
      case _: ByteType => value.map(new ByteType(_))
      case null => value
    }
    formattedValue.asInstanceOf[Option[T]]
  }

  def set(key: String, value: String): AnyRef = extractConfig.setProperty(key, value)

  def setIfNotExists(key: String, value: String): Any =
    if (!config.containsKey(key)) set(key, value)

  def getBoolean(key: String, default: Boolean, hotload: Boolean = false): Boolean =
    getOption(key, hotload).map(_.toBoolean).getOrElse(default)

  def getBoolean(commonVars: CommonVars[Boolean]): Option[Boolean] = getOption(commonVars)

  def get(key: String, default: String): String =
    getOption(key, false).getOrElse(default)

  def get(key: String, default: String, hotload: Boolean): String = {
    getOption(key, hotload).getOrElse(default)
  }

  def get(commonVars: CommonVars[String]): Option[String] = getOption(commonVars)

  def get(key: String, hotload: Boolean = false): String =
    getOption(key, hotload).getOrElse(throw new NoSuchElementException(key))

  def get(key: String): String =
    getOption(key).getOrElse(throw new NoSuchElementException(key))

  def getInt(key: String, default: Int, hotload: Boolean = false): Int =
    getOption(key, hotload).map(_.toInt).getOrElse(default)

  def getInt(commonVars: CommonVars[Int]): Option[Int] = getOption(commonVars)

  def contains(key: String): Boolean = getOption(key).isDefined

  def contains(key: String, hotload: Boolean = false): Boolean = getOption(key, hotload).isDefined

}
