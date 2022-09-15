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

package org.apache.linkis.manager.common.utils

import org.apache.linkis.manager.common.entity.persistence.PersistenceResource
import org.apache.linkis.manager.common.entity.resource.{CPUResource, CommonNodeResource, DriverAndYarnResource, InstanceResource, LoadInstanceResource, LoadResource, MemoryResource, Resource, ResourceType, SpecialResource, YarnResource}
import org.apache.linkis.manager.common.utils.ResourceUtils.serializeResource
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.{BeforeEach, Test}

import java.util.Date

class ResourceUtilsTest {
  
  @Test def testFromPersistenceResource: Unit = {
    val persistenceResource = new PersistenceResource
    val s = "{\"cores\":8}"
    persistenceResource.setMaxResource(s)
    persistenceResource.setMinResource(s)
    persistenceResource.setLockedResource(s)
    persistenceResource.setExpectedResource(s)
    persistenceResource.setLeftResource(s)
    persistenceResource.setUsedResource(s)
    persistenceResource.setCreateTime(new Date)
    persistenceResource.setUpdateTime(new Date)
    persistenceResource.setResourceType("CPU")
    val resource: CommonNodeResource = ResourceUtils.fromPersistenceResource(persistenceResource)
    val resourceType: ResourceType = resource.getResourceType
    assertEquals(resourceType, ResourceType.CPU)
  }

  @Test def testGetLoadInstanceResourceRate(): Unit = {
    val cpuResource = new CPUResource(8)
    val loadInstanceResource = new LoadInstanceResource(1, 1, 1)
    val loadInstanceResource1 = new LoadInstanceResource(2, 2, 2)
    val rate0: Float = ResourceUtils.getLoadInstanceResourceRate(null, null)
    val rate1: Float = ResourceUtils.getLoadInstanceResourceRate(cpuResource, null)
    val rate2: Float = ResourceUtils.getLoadInstanceResourceRate(loadInstanceResource, cpuResource)
    val rate3: Float = ResourceUtils.getLoadInstanceResourceRate(loadInstanceResource, loadInstanceResource1)
    assertEquals(0, rate0)
    assertEquals(1, rate1)
    assertEquals(1, rate2)
    assertEquals(0.5, rate3)
  }

  @Test def testSerializeResource(): Unit = {
    val cpuResource = new CPUResource(8)
    val cpuInfo: String = ResourceUtils.serializeResource(cpuResource)
    assertEquals("{\"cores\":8}", cpuInfo)
  }

  @Test def testDeserializeResource(): Unit = {
    val cpuInfo = "{\"cores\":8}";
    val resource: Resource = ResourceUtils.deserializeResource(cpuInfo)
    val resource1: CPUResource = resource.asInstanceOf[CPUResource]
    assertEquals(8, resource1.cores)
  }

  @Test def testGetResourceTypeByResource(): Unit = {
    val cpuResource = new CPUResource(8)
    val memoryResource = new MemoryResource(0)
    val instanceResource = new InstanceResource(0)
    val loadResource = new LoadResource(0, 0)
    val loadInstanceResource = new LoadInstanceResource(0, 0, 0)
    val yarnResource = new YarnResource(0, 0, 0)
    val driverAndYarnResource = new DriverAndYarnResource(
      new LoadInstanceResource(0, 0, 0),
      new YarnResource(0, 0, 0, "test")
    )
    val specialResource = new SpecialResource(new java.util.HashMap[String, AnyVal]())

    val cpuType: ResourceType = ResourceUtils.getResourceTypeByResource(cpuResource)
    val memoryType: ResourceType = ResourceUtils.getResourceTypeByResource(memoryResource)
    val instanceType: ResourceType = ResourceUtils.getResourceTypeByResource(instanceResource)
    val loadType: ResourceType = ResourceUtils.getResourceTypeByResource(loadResource)
    val loadInstanceType: ResourceType = ResourceUtils.getResourceTypeByResource(loadInstanceResource)
    val yarnType: ResourceType = ResourceUtils.getResourceTypeByResource(yarnResource)
    val driverType: ResourceType = ResourceUtils.getResourceTypeByResource(driverAndYarnResource)
    val specialType: ResourceType = ResourceUtils.getResourceTypeByResource(specialResource)

    assertEquals(cpuType, ResourceType.CPU)
    assertEquals(memoryType, ResourceType.LoadInstance)
    assertEquals(instanceType, ResourceType.Instance)
    assertEquals(loadType, ResourceType.Load)
    assertEquals(loadInstanceType, ResourceType.LoadInstance)
    assertEquals(yarnType, ResourceType.Yarn)
    assertEquals(driverType, ResourceType.DriverAndYarn)
    assertEquals(specialType, ResourceType.Special)
  }

  @Test def testConvertTo(): Unit = {
    val labelResource = new CommonNodeResource

    labelResource.setResourceType(ResourceType.CPU)
    val cr = ResourceUtils.convertTo(labelResource, ResourceType.CPU);
    assertEquals(labelResource, cr);

    val yn = ResourceUtils.convertTo(labelResource, ResourceType.Yarn);
    assertEquals(labelResource, yn);

    val driverResource: DriverAndYarnResource = new DriverAndYarnResource(new LoadInstanceResource(0, 0, 0), new YarnResource(0, 0, 0))
    labelResource.setResourceType(ResourceType.DriverAndYarn)
    labelResource.setMaxResource(driverResource)
    labelResource.setMaxResource(driverResource)
    labelResource.setMinResource(driverResource)
    labelResource.setLockedResource(driverResource)
    labelResource.setExpectedResource(driverResource)
    labelResource.setLeftResource(driverResource)
    labelResource.setUsedResource(driverResource)
    labelResource.setCreateTime(new Date)
    labelResource.setUpdateTime(new Date)
    val li = ResourceUtils.convertTo(labelResource, ResourceType.LoadInstance);
    assertEquals(ResourceType.LoadInstance, li.getResourceType);

  }

  @Test def toPersistenceResource(): Unit = {
    val labelResource = new CommonNodeResource
    val cpuResource = new CPUResource(8)
    labelResource.setMaxResource(cpuResource)
    labelResource.setMinResource(cpuResource)
    labelResource.setLockedResource(cpuResource)
    labelResource.setExpectedResource(cpuResource)
    labelResource.setLeftResource(cpuResource)
    labelResource.setUsedResource(cpuResource)
    labelResource.setCreateTime(new Date)
    labelResource.setUpdateTime(new Date)
    labelResource.setResourceType(ResourceType.CPU)
    val persistenceResource: PersistenceResource = ResourceUtils.toPersistenceResource(labelResource)
    assertEquals("{\"cores\":8}", persistenceResource.getMaxResource)
  }

  @Test def testFromPersistenceResourceAndUser(): Unit = {
    val persistenceResource = new PersistenceResource
    val s = "{\"cores\":8}"
    persistenceResource.setId(0)
    persistenceResource.setMaxResource(s)
    persistenceResource.setMinResource(s)
    persistenceResource.setLockedResource(s)
    persistenceResource.setExpectedResource(s)
    persistenceResource.setLeftResource(s)
    persistenceResource.setUsedResource(s)
    persistenceResource.setCreateTime(new Date)
    persistenceResource.setUpdateTime(new Date)
    persistenceResource.setResourceType("CPU")
    val resource: CommonNodeResource = ResourceUtils.fromPersistenceResourceAndUser(persistenceResource)
    val resourceType: ResourceType = resource.getResourceType
    assertEquals(resourceType, ResourceType.CPU)
  }
}
