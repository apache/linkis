package com.webank.wedatasphere.linkis.bml.service.impl;

import com.webank.wedatasphere.linkis.bml.service.BmlShareResourceService;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import java.io.OutputStream;
import java.util.Map;

public class BmlShareResourceServiceImpl implements BmlShareResourceService {
    @Override
    public void uploadShareResource(FormDataMultiPart formDataMultiPart, String user, Map<String, Object> properties) {

    }

    @Override
    public void updateShareResource(FormDataMultiPart formDataMultiPart, String user, Map<String, Object> properties) {

    }

    @Override
    public void downloadShareResource(String user, String resourceId, String version, OutputStream outputStream, Map<String, Object> properties) {

    }
}
