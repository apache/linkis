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

import scala.collection.JavaConverters._

private[conf] object BDPConfiguration extends Logging {

  val DEFAULT_PROPERTY_FILE_NAME = "linkis.properties"

  val DEFAULT_SERVER_CONF_FILE_NAME = "linkis-server.properties"

  private val extractConfig = new Properties
  private val config = new Properties
  private val sysProps = sys.props
  private val env = sys.env

  private def init: Unit = {

    // load pub linkis conf
    val propertyFile = sysProps.getOrElse("wds.linkis.configuration", DEFAULT_PROPERTY_FILE_NAME)
    val configFileURL = getClass.getClassLoader.getResource(propertyFile)
    if (configFileURL != null && new File(configFileURL.getPath).exists) {
      logger.info(
        s"******************* Notice: The Linkis configuration file is $propertyFile ! *******************"
      )
      initConfig(config, configFileURL.getPath)
    } else {
      logger.warn(
        s"************ Notice: The Linkis configuration file $propertyFile is not exists! *******************"
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
    } else {
      logger.warn(
        s"**************** Notice: The Linkis serverConf file $serverConf is not exists! *******************"
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
        } else {
          logger.warn(
            s"********** Notice: The Linkis server.confs file $propertyF is not exists! **************"
          )
        }
      }
    }

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

  def getOption(key: String): Option[String] = {
    if (extractConfig.containsKey(key)) {
      return Some(extractConfig.getProperty(key))
    }
    val value = config.getProperty(key)
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

  def mergePropertiesFromMap(props: Properties, mapProps: Map[String, String]): Unit = {
    mapProps.foreach { case (k, v) => props.put(k, v) }
  }

  def getOption[T](commonVars: CommonVars[T]): Option[T] = if (commonVars.value != null) {
    Option(commonVars.value)
  } else {
    val value = BDPConfiguration.getOption(commonVars.key)
    if (value.isEmpty) Option(commonVars.defaultValue)
    else formatValue(commonVars.defaultValue, value)
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

  def getBoolean(key: String, default: Boolean): Boolean =
    getOption(key).map(_.toBoolean).getOrElse(default)

  def getBoolean(commonVars: CommonVars[Boolean]): Option[Boolean] = getOption(commonVars)

  def get(key: String, default: String): String = getOption(key).getOrElse(default)
  def get(commonVars: CommonVars[String]): Option[String] = getOption(commonVars)

  def get(key: String): String = getOption(key).getOrElse(throw new NoSuchElementException(key))

  def getInt(key: String, default: Int): Int = getOption(key).map(_.toInt).getOrElse(default)
  def getInt(commonVars: CommonVars[Int]): Option[Int] = getOption(commonVars)

  def contains(key: String): Boolean = getOption(key).isDefined

}
