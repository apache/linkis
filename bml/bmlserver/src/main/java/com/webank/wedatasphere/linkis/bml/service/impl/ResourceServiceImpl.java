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
package com.webank.wedatasphere.linkis.bml.service.impl;

import com.webank.wedatasphere.linkis.bml.Entity.Resource;
import com.webank.wedatasphere.linkis.bml.Entity.ResourceVersion;
import com.webank.wedatasphere.linkis.bml.common.Constant;
import com.webank.wedatasphere.linkis.bml.common.ResourceHelper;
import com.webank.wedatasphere.linkis.bml.common.ResourceHelperFactory;
import com.webank.wedatasphere.linkis.bml.dao.ResourceDao;
import com.webank.wedatasphere.linkis.bml.dao.VersionDao;
import com.webank.wedatasphere.linkis.bml.service.ResourceService;
import com.webank.wedatasphere.linkis.common.exception.ErrorException;

import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

/**
 * Created by cooperyang on 2019/5/17.
 */
@Service
public class ResourceServiceImpl implements ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private VersionDao versionDao;


    private static final String FIRST_VERSION = "v000001";

    @Override
    public List<Resource> getResources(Map paramMap) {
        return resourceDao.getResources(paramMap);
    }

    @Override
    public void deleteResource(String resourceId) {
        resourceDao.deleteResource(resourceId);
        versionDao.deleteResource(resourceId);
    }

    @Override
    public void batchDeleteResources(List<String> resourceIds) {
        resourceDao.batchDeleteResources(resourceIds);
        versionDao.batchDeleteResources(resourceIds);
    }

    @Transactional(rollbackFor = ErrorException.class)
    @Override
    public List<UploadResult> upload(FormDataMultiPart formDataMultiPart, String user, Map<String, Object> properties)throws Exception{
        ResourceHelper resourceHelper = ResourceHelperFactory.getResourceHelper();
        List<FormDataBodyPart> files = formDataMultiPart.getFields("file");
        List<UploadResult> results = new ArrayList<>();
        for (FormDataBodyPart p : files) {
            InputStream inputStream = p.getValueAs(InputStream.class);
            FormDataContentDisposition fileDetail = p.getFormDataContentDisposition();
            String fileName = new String(fileDetail.getFileName().getBytes("ISO8859-1"), "UTF-8");
            String path = resourceHelper.generatePath(user, fileName, properties);
            StringBuilder sb = new StringBuilder();
            //在upload之前首先应该判断一下这个path是否是已经存在了，如果存在了，抛出异常
            boolean isFileExists = resourceHelper.checkIfExists(path, user);
//            if (isFileExists){
//                throw new ErrorException(70035, "同名文件已经于今日已经上传,请使用更新操作");
//            }
            long size = resourceHelper.upload(path, user, inputStream, sb);
            String md5String = sb.toString();
            boolean isSuccess = false;
            if (StringUtils.isNotEmpty(md5String) && size >= 0) {
                isSuccess = true;
            }
            String resourceId = (String) properties.get("resourceId");
            Resource resource = Resource.createNewResource(resourceId, user, fileName, properties);
            //插入一条记录到resource表
            long id = resourceDao.uploadResource(resource);
            logger.info("{} uploaded a resource and resourceId is {}", user, resource.getResourceId());
            //插入一条记录到resource version表
            String clientIp = (String) properties.get("clientIp");
            ResourceVersion resourceVersion = ResourceVersion.createNewResourceVersion(resourceId, path, md5String,
                    clientIp, size, Constant.FIRST_VERSION, 1);
            versionDao.insertNewVersion(resourceVersion);
            UploadResult uploadResult = new UploadResult(resourceId, FIRST_VERSION, isSuccess);
            results.add(uploadResult);
        }
        return results;
    }

    @Override
    public boolean checkResourceId(String resourceId) {
        return resourceDao.checkExists(resourceId) == 1;
    }

    public static class UploadResult{
        private String resourceId;
        private String version;
        private boolean isSuccess;

        public UploadResult(String resourceId, String version, boolean isSuccess){
            this.resourceId = resourceId;
            this.version = version;
            this.isSuccess = isSuccess;
        }

        public String getResourceId() {
            return resourceId;
        }

        public void setResourceId(String resourceId) {
            this.resourceId = resourceId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }
    }


    @Override
    public Resource getResource(String resourceId) {
        return resourceDao.getResource(resourceId);
    }


    @Override
    public boolean checkAuthority(@NotNull String user, String resourceId) {
        return user.equals(resourceDao.getUserByResourceId(resourceId));
    }


    @Override
    public boolean checkExpire(String resourceId, String version) {
        Resource resource = resourceDao.getResource(resourceId);
        ResourceVersion resourceVersion = versionDao.getResourceVersion(resourceId, version);
        if (!resource.isEnableFlag() || !resourceVersion.isEnableFlag()) {
            return false;
        }
        return true;
    }


    @Override
    public void cleanExpiredResources() {
       //1 找出已经过期的所有的资源
       //2 将这些资源干掉
//        List<Resource> resources = resourceDao.getResources();
//        List<Resource> expiredResources = new ArrayList<>();
//        for(Resource resource : resources){
//            if (resource.isExpire() && resource.isEnableFlag()){
//                String expiredTimeStr = RestfulUtils.getExpireTime(resource.getCreateTime(), resource.getExpireType(), resource.getExpireTime());
//
//            }
//        }
        //resourceDao.cleanExpiredResources();
    }
}
