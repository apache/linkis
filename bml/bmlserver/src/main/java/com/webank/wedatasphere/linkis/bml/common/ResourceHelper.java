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
