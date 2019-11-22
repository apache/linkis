/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.bml.service;

import com.webank.wedatasphere.linkis.bml.Entity.ResourceVersion;
import com.webank.wedatasphere.linkis.bml.Entity.Version;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by cooperyang on 2019/5/17.
 */
public interface VersionService {

   Version getVersion(String resourceId, String version);

//   List<Version> getVersions(String resourceId, List<String> versions);

   List<ResourceVersion> getResourcesVersions(Map paramMap);

   List<ResourceVersion> getAllResourcesViaSystem(String system, String user);
   //分页查询VResourcesViaSystem
   public List<ResourceVersion> selectResourcesViaSystemByPage(int currentPage, int pageSize,String system, String user);

   void deleteResourceVersion(String resourceId, String version);

   void deleteResourceVersions(String resourceId);

   void deleteResourcesVersions(List<String> resourceIds, List<String> versions);


   /**
    * 通过resourceId 获取对应资源所有的版本信息
    * @param resourceId resourceId
    * @return Version的链表
    */
   List<Version> getVersions(String resourceId);

   //分页查询Version
   List<Version> selectVersionByPage(int currentPage, int pageSize,String resourceId);



   /**
    * 更新资源的步骤是
    * 1.获取resourceID对应资源的path
    * 2.将资源的二进制流append到path对应的文件末尾
    * @param resourceId resourceId
    * @param user 用户信息
    * @param formDataMultiPart 上传的二进制流
    * @param params 可选参数
    * @return 新的version
    * @throws Exception
    */
   String updateVersion(String resourceId, String user, FormDataMultiPart formDataMultiPart, Map<String, Object> params)throws Exception;


   String getNewestVersion(String resourceId);



   boolean downloadResource(String user, String resourceId, String version, OutputStream outputStream, Map<String, Object> properties) throws IOException;

   boolean checkVersion(String resourceId, String version);


   boolean canAccess(String resourceId, String version);


}
