package com.webank.wedatasphere.linkis.bml.service;

import com.webank.wedatasphere.linkis.bml.Entity.Resource;
import com.webank.wedatasphere.linkis.bml.service.impl.ResourceServiceImpl;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import java.util.List;
import java.util.Map;

/**
 * Created by v_wbjjianli on 2019/5/17.
 */
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
    * @param formDataMultiPart notnull
    * @param user um_user
    * @param properties Map
    */
   List<ResourceServiceImpl.UploadResult> upload(FormDataMultiPart formDataMultiPart, String user, Map<String, Object> properties)throws Exception;

   boolean checkResourceId(String resourceId);


   Resource getResource(String resourceId);


    boolean checkAuthority(String user, String resourceId);

   boolean checkExpire(String resourceId, String version);

   void cleanExpiredResources();
}
