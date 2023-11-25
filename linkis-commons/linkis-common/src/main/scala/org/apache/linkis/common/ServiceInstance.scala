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

package org.apache.linkis.common

class ServiceInstance {
  private var applicationName: String = _
  private var instance: String = _
  private var registryTimestamp: Long = _
  def setApplicationName(applicationName: String): Unit = this.applicationName = applicationName
  def getApplicationName: String = applicationName
  def setInstance(instance: String): Unit = this.instance = instance
  def getInstance: String = instance

  def setRegistryTimestamp(registryTimestamp: Long): Unit = this.registryTimestamp =
    registryTimestamp

  def getRegistryTimestamp: Long = registryTimestamp

  override def equals(other: Any): Boolean = other match {
    case that: ServiceInstance =>
      applicationName == that.applicationName &&
        instance == that.instance
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(applicationName, instance)
    state
      .map {
        case null => 0
        case s => s.hashCode
      }
      .foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString: String =
    s"ServiceInstance($applicationName, $instance, $registryTimestamp)"

}

object ServiceInstance {

  def apply(applicationName: String, instance: String): ServiceInstance = {
    val serviceInstance = new ServiceInstance
    serviceInstance.setApplicationName(applicationName)
    serviceInstance.setInstance(instance)
    serviceInstance
  }

  def apply(applicationName: String, instance: String, registryTimestamp: Long): ServiceInstance = {
    val serviceInstance = new ServiceInstance
    serviceInstance.setApplicationName(applicationName)
    serviceInstance.setInstance(instance)
    serviceInstance.setRegistryTimestamp(registryTimestamp)
    serviceInstance
  }

  def unapply(serviceInstance: ServiceInstance): Option[(String, String)] =
    if (serviceInstance != null) {
      Some(serviceInstance.applicationName, serviceInstance.instance)
    } else None

}
