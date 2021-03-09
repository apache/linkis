/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datasourcemanager.core.service.impl;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.common.exception.WarnException;
import com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.FormStreamContent;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.BmlAppService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.MetadataOperateService;
import com.webank.wedatasphere.linkis.metadatamanager.common.protocol.MetadataConnect;
import com.webank.wedatasphere.linkis.metadatamanager.common.protocol.MetadataResponse;
import com.webank.wedatasphere.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.webank.wedatasphere.linkis.datasourcemanager.common.ServiceErrorCode.BML_SERVICE_ERROR;
import static com.webank.wedatasphere.linkis.datasourcemanager.common.ServiceErrorCode.REMOTE_METADATA_SERVICE_ERROR;

/**
 * @author davidhua
 * 2020/02/14
 */
@Service
public class MetadataOperateServiceImpl implements MetadataOperateService {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataOperateService.class);
    @Autowired
    private BmlAppService bmlAppService;
    @Override
    public void doRemoteConnect(String mdRemoteServiceName ,
                                String operator, Map<String, Object> connectParams) throws WarnException {
        List<String> uploadedResources = new ArrayList<>();
        try{
            connectParams.entrySet().removeIf(entry -> {
                Object paramValue = entry.getValue();
                //Upload stream resource in connection parameters
                if(paramValue instanceof FormStreamContent){
                    FormStreamContent streamContent = (FormStreamContent)paramValue;
                    String fileName = streamContent.getFileName();
                    InputStream inputStream = streamContent.getStream();
                    if (null != inputStream){
                        try {
                            String resourceId = bmlAppService.clientUploadResource(operator,
                                    fileName, inputStream);
                            if(null == resourceId){
                                return true;
                            }
                            uploadedResources.add(resourceId);
                            entry.setValue(resourceId);
                        } catch (ErrorException e) {
                            throw new WarnException(BML_SERVICE_ERROR.getValue(), "Fail to operate file in request[上传文件处理失败]");
                        }
                    }
                }
                return false;
            });
            LOG.info("Send request to metadata service:[" + mdRemoteServiceName + "] for building a connection");
            //Get a sender
            Sender sender = Sender.getSender(mdRemoteServiceName);
            try {
                Object object = sender.ask(new MetadataConnect(operator, connectParams, ""));
                if (object instanceof MetadataResponse) {
                    MetadataResponse response = (MetadataResponse) object;
                    if (!response.status()) {
                        throw new WarnException(REMOTE_METADATA_SERVICE_ERROR.getValue(),
                                "Connection Failed[连接失败], Msg[" + response.data() + "]");
                    }
                } else {
                    throw new WarnException(REMOTE_METADATA_SERVICE_ERROR.getValue(),
                            "Remote Service Error[远端服务出错, 联系运维处理]");
                }
            }catch(Throwable t){
                if(!(t instanceof WarnException)) {
                    throw new WarnException(REMOTE_METADATA_SERVICE_ERROR.getValue(),
                            "Remote Service Error[远端服务出错, 联系运维处理]");
                }
                throw t;
            }
        }finally{
            if(!uploadedResources.isEmpty()){
                uploadedResources.forEach( resourceId ->{
                    try{
                        //Proxy to delete resource
                        bmlAppService.clientRemoveResource(operator, resourceId);
                    }catch(Exception e){
                        //ignore
                    }
                });
            }
        }
    }
}
