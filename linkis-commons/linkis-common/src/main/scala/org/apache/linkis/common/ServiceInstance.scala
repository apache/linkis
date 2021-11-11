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
 
package org.apache.linkis.common


class ServiceInstance {
  private var applicationName: String = _
  private var instance: String = _
  def setApplicationName(applicationName: String) = this.applicationName = applicationName
  def getApplicationName = applicationName
  def setInstance(instance: String) = this.instance = instance
  def getInstance = instance


  def canEqual(other: Any): Boolean = other.isInstanceOf[ServiceInstance]

  override def equals(other: Any): Boolean = other match {
    case that: ServiceInstance =>
      (that canEqual this) &&
        applicationName == that.applicationName &&
        instance == that.instance
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(applicationName, instance)
    state.map{
      case null => 0
      case s => s.hashCode
    }.foldLeft(0)((a, b) => 31 * a + b)
  }


  override def toString = s"ServiceInstance($applicationName, $instance)"
}
object ServiceInstance {
  def apply(applicationName: String, instance: String): ServiceInstance = {
    val serviceInstance = new ServiceInstance
    serviceInstance.setApplicationName(applicationName)
    serviceInstance.setInstance(instance)
    serviceInstance
  }

  def unapply(serviceInstance: ServiceInstance): Option[(String, String)] = if(serviceInstance != null)
    Some(serviceInstance.applicationName, serviceInstance.instance) else None
}