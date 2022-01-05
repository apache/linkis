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
 
package org.apache.linkis.bml.service;

import org.apache.linkis.bml.Entity.Resource;
import org.apache.linkis.bml.service.impl.ResourceServiceImpl;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ResourceService {

   List<Resource> getResources(Map paramMap);

   void deleteResource(String resourceId);

   void batchDeleteResources(List<String> resourceIds);

   /**
    * 用于上传文件的函数，上传文件的步骤
    * 1.根据用户名和resourceHeader信息为用户创建一个文件
    * 2.利用storage模块将二进制流存入到物料库
    * 3.二进制流的存储方式有两种，根据资源文件的大小选择合并或者是单独存储
    * 4.生成resourceID
    * 4.更新resource 和 resource_version表
    * @param files notnull
    * @param user um_user
    * @param properties Map
    */
   List<ResourceServiceImpl.UploadResult> upload(List<MultipartFile> files, String user, Map<String, Object> properties)throws Exception;

   boolean checkResourceId(String resourceId);


   Resource getResource(String resourceId);


    boolean checkAuthority(String user, String resourceId);

   boolean checkExpire(String resourceId, String version);

   void cleanExpiredResources();
}
