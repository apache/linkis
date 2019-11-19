package com.webank.wedatasphere.linkis.bml.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * created by cooperyang on 2019/5/21
 * Description:
 */
public interface ResourceHelper {


    public long upload(String path, String user, InputStream inputStream, StringBuilder stringBuilder) throws UploadResourceException;

    public void update(String path);

    public void getResource(String path, int start, int end);

    public String generatePath(String user, String fileName, Map<String, Object> properties);

    public String getSchema();


    boolean checkIfExists(String path) throws IOException;
}
