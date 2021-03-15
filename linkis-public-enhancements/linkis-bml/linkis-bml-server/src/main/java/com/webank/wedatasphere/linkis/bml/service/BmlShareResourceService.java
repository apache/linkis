package com.webank.wedatasphere.linkis.bml.service;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import java.io.OutputStream;
import java.util.Map;

public interface BmlShareResourceService {

    void uploadShareResource(FormDataMultiPart formDataMultiPart, String user, Map<String, Object> properties);

    void updateShareResource(FormDataMultiPart formDataMultiPart, String user, Map<String, Object> properties);

    void downloadShareResource(String user, String resourceId, String version, OutputStream outputStream, Map<String, Object> properties);



}
