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
 
package org.apache.linkis.manager.client.annotation.resource;

import org.apache.linkis.common.utils.JavaLog;
import org.apache.linkis.manager.client.resource.ResourceManagerClient;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.RMNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;


@Component
public class RMAnnotationParser extends JavaLog {

    @Autowired
    private ResourceManagerClient resourceManagerClient;

    /**
     * Load after startup(启动后加载)
     *
     * @param readyEvent
     */
    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent readyEvent) {
        info("Prepare to register resources with RM(准备向RM注册资源)...");
        Map<String, Object> permissionMap = readyEvent.getApplicationContext().getBeansWithAnnotation(EnableResourceManager.class);
        if (permissionMap.size() != 1) {
            error("EnableResourceManager Annotations must be and can only occur once!(注解必须且只能出现一次！)");
            System.exit(12000);
        }
        Object enableResourceManagerObj = null;
        Method registerResourceMethod = null;
        for (Object permissionObject : permissionMap.values()) {
            Class<? extends Object> permissionClass = permissionObject.getClass();
            for (Method method : permissionClass.getMethods()) {
                if (method.isAnnotationPresent(RegisterResource.class)) {
                    enableResourceManagerObj = permissionObject;
                    registerResourceMethod = method;
                }
            }
        }
        if (registerResourceMethod == null) {
            error("RegisterResourceThe annotation must be and can only appear once!(注解必须且只能出现一次！)");
            System.exit(12000);
        }
        Object rmNode = null;
        try {
            rmNode = registerResourceMethod.invoke(enableResourceManagerObj);
        } catch (Throwable e) {
            error("Failed to get information to register resources with RM!(获取向RM注册资源的信息失败！)", e);
            System.exit(12000);
        }
        if (!(rmNode instanceof RMNode)) {
            error("RegisterResourceNote the modified method, the return value must be a ModuleInfo object!(注解修饰的方法，返回值必须为ModuleInfo对象！)");
            System.exit(12000);
        }
        info("Start registering resources with RM(开始向RM注册资源)：" + rmNode);
        resourceManagerClient.register((EMNode) rmNode);
    }

}
