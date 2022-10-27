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

import java.util.Properties

import scala.collection.JavaConverters._

case class CommonVars[T](
    key: String,
    defaultValue: T,
    value: T,
    description: String = null,
    hotload: Boolean = false
) {
  val getValue: T = BDPConfiguration.getOption(this).getOrElse(defaultValue)

  def getValue(properties: java.util.Map[String, String]): T = {
    if (properties == null || !properties.containsKey(key) || properties.get(key) == null) {
      getValue
    } else BDPConfiguration.formatValue(defaultValue, Option(properties.get(key))).get
  }

  def getValue(properties: Map[String, String], hotload: Boolean = false): T = getValue(
    properties.asJava
  )

  def acquireNew: T = BDPConfiguration.getOption(this).getOrElse(defaultValue)
}

object CommonVars {

  def apply[T](key: String, defaultValue: T, description: String): CommonVars[T] =
    CommonVars(key, defaultValue, null.asInstanceOf[T], description, false)

  def apply[T](key: String, defaultValue: T, description: String, hotload: Boolean): CommonVars[T] =
    CommonVars(key, defaultValue, null.asInstanceOf[T], description, hotload)

  implicit def apply[T](key: String, defaultValue: T): CommonVars[T] =
    new CommonVars(key, defaultValue, null.asInstanceOf[T], null, false)

  implicit def apply[T](key: String, defaultValue: T, hotload: Boolean): CommonVars[T] =
    new CommonVars(key, defaultValue, null.asInstanceOf[T], null, hotload)

  implicit def apply[T](key: String): CommonVars[T] =
    apply(key, null.asInstanceOf[T])

  implicit def apply[T](key: String, hotload: Boolean): CommonVars[T] =
    apply(key, null.asInstanceOf[T], hotload)

  def properties: Properties = BDPConfiguration.properties

  def hotProperties: Properties = BDPConfiguration.hotProperties
}
