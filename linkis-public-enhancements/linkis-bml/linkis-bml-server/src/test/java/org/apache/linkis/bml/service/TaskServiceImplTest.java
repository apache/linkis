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

package org.apache.linkis.bml.service;

import org.apache.linkis.bml.dao.ResourceDao;
import org.apache.linkis.bml.dao.TaskDao;
import org.apache.linkis.bml.entity.Resource;
import org.apache.linkis.bml.entity.ResourceTask;
import org.apache.linkis.bml.service.impl.TaskServiceImpl;
import org.apache.linkis.common.ServiceInstance;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** TaskServiceImpl Tester */
@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {

  @InjectMocks private TaskServiceImpl taskServiceImpl;

  @Mock private TaskDao taskDao;

  @Mock private ResourceDao resourceDao;

  @MockBean ResourceTask resourceTask;

  @Mock ServiceInstance serviceInstance;

  Resource addResource() {
    Resource resource = new Resource();
    resource.setResourceId("123");
    resource.setResourceHeader("2");
    resource.setDownloadedFileName("testFileName");
    resource.setSystem("testSystem");
    resource.setCreateTime(new Date());
    resource.setUser("testUser");
    resource.setExpireTime("2012.12.02");
    resource.setMaxVersion(3);
    resource.setUpdateTime(new Date());
    resource.setUpdator("testUpdator");
    resource.setEnableFlag(false);
    return resource;
  }

  @Test
  public void testCreateUploadTask() throws Exception {
    List<MultipartFile> files = new ArrayList<>();
    String user = "userTest";
    Map<String, Object> properties = new HashMap<>();
    String resourceId = UUID.randomUUID().toString();
  }

  @Test
  public void testCreateUpdateTask() throws Exception {}

  @Test
  public void testCreateDownloadTask() throws Exception {

    /* String resourceId="123";
    String version="0.231";
    String user="testUser";
    String clientIp="192.168.13.1";
    Resource resource=addResource();
    ResourceTask resourceTask = new ResourceTask();
    resourceTask.setResourceId(resourceId);
    resourceTask.setVersion(version);
    resourceTask.setOperation(OperationEnum.DOWNLOAD.getValue());
    resourceTask.setState(TaskState.RUNNING.getValue());
    resourceTask.setSubmitUser(user);
    resourceTask.setClientIp(clientIp);
    resourceTask.setSystem(resource.getSystem());
    resourceTask.setStartTime(new Date());
    resourceTask.setLastUpdateTime(new Date());
    Mockito.when(serviceInstance.getInstance()).thenReturn("test");
    Mockito.when(resourceDao.getResource(resourceId)).thenReturn(resource);
    Mockito.when(ResourceTask.createDownloadTask(resourceId, version, user, resource.getSystem(), clientIp)).thenReturn(resourceTask);
    ResourceTask resourceTasks=taskServiceImpl.createDownloadTask(resourceId,version,user,clientIp);
    assertTrue(resourceTask.equals(resourceTask));*/
  }

  @Test
  public void testUpdateState() throws Exception {

    long taskId = 1;
    String state = "1";
    Date updateTime = new Date();
    taskServiceImpl.updateState(taskId, state, updateTime);
  }

  @Test
  public void testUpdateState2Failed() throws Exception {
    long taskId = 1l;
    String state = "1";
    Date updateTime = new Date();
    String errMsg = "123";
    taskServiceImpl.updateState2Failed(taskId, state, updateTime, errMsg);
  }

  @Test
  public void testCreateDeleteVersionTask() throws Exception {
    /*String resourceId="123";
    String version="0.321";
    String user="testUser";
    String clientIp="192.167.13.2";
    Mockito.when(serviceInstance.getInstance()).thenReturn("test");
    taskServiceImpl.createDeleteVersionTask(resourceId,version,user,clientIp);*/
  }

  @Test
  public void testCreateDeleteResourceTask() throws Exception {}

  @Test
  public void testCreateDeleteResourcesTask() throws Exception {}

  @Test
  public void testCreateRollbackVersionTask() throws Exception {}

  @Test
  public void testCreateCopyResourceTask() throws Exception {}

  @Test
  public void testGetResourceLastVersion() throws Exception {}

  @Test
  public void testGenerateNewVersion() throws Exception {

    /*
    try {
       Method method = TaskServiceImpl.getClass().getMethod("generateNewVersion", String.class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }
}
