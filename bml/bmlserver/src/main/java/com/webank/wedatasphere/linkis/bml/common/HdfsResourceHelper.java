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

import com.webank.wedatasphere.linkis.bml.conf.BmlServerConfiguration;
import com.webank.wedatasphere.linkis.common.io.Fs;
import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.storage.FSFactory;
import com.webank.wedatasphere.linkis.storage.utils.FileSystemUtils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * created by cooperyang on 2019/5/21
 * Description: 资源文件上传到hdfs存储目录，存储的原则是一个资源
 */
public class HdfsResourceHelper implements ResourceHelper {

    private static final Logger logger = LoggerFactory.getLogger(HdfsResourceHelper.class);

    private static final String SCHEMA = "hdfs://";

    @Override
    public long upload(String path, String user, InputStream inputStream,
                       StringBuilder stringBuilder) throws UploadResourceException {
        OutputStream outputStream = null;
        long size = -1;
        Fs fileSystem = null;
        try {
            FsPath fsPath = new FsPath(path);
            fileSystem = FSFactory.getFs(fsPath);
            fileSystem.init(new HashMap<String, String>());
            if (!fileSystem.exists(fsPath)) FileSystemUtils.createNewFile(fsPath, user, true);
            outputStream = fileSystem.write(fsPath, false);
            byte[] buffer = new byte[1024];
            int ch = 0;
            MessageDigest md5Digest = DigestUtils.getMd5Digest();
            while ((ch = inputStream.read(buffer)) != -1) {
                md5Digest.update(buffer, 0, ch);
                outputStream.write(buffer, 0, ch);
                size += ch;
            }
            if (stringBuilder != null) {
                stringBuilder.append(Hex.encodeHexString(md5Digest.digest()));
            }
        } catch (final IOException e) {
            logger.error("{} write to {} failed, reason is, IOException:", user, path, e);
            UploadResourceException uploadResourceException = new UploadResourceException();
            uploadResourceException.initCause(e);
            throw uploadResourceException;
        } catch (final Throwable t) {
            logger.error("{} write to {} failed, reason is", user, path, t);
            UploadResourceException uploadResourceException = new UploadResourceException();
            uploadResourceException.initCause(t);
            throw uploadResourceException;
        } finally {
            IOUtils.closeQuietly(outputStream);
            if (fileSystem != null) try {
                fileSystem.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            IOUtils.closeQuietly(inputStream);
        }
        size += 1;
        return size;
    }


    @Override
    public void update(String path) {

    }


    @Override
    public void getResource(String path, int start, int end) {

    }

    @Override
    public String getSchema() {
        return SCHEMA;
    }

    @Override
    public String generatePath(String user, String fileName, Map<String, Object> properties) {
        String resourceHeader = (String) properties.get("resourceHeader");
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String dateStr = format.format(new Date());
        if (StringUtils.isNotEmpty(resourceHeader)) {
            return getSchema() + BmlServerConfiguration.BML_HDFS_PREFIX().getValue()
                    + "/" + user + "/" + dateStr + "/" + resourceHeader + "/" + fileName;
        } else {
            return getSchema() + BmlServerConfiguration.BML_HDFS_PREFIX().getValue() + "/" + user + "/" + dateStr + "/" + fileName;
        }
    }


    @Override
    public boolean checkIfExists(String path) throws IOException {
        Fs fileSystem = FSFactory.getFs(new FsPath(path));
        fileSystem.init(new HashMap<String, String>());
        try {
            return fileSystem.exists(new FsPath(path));
        } finally {
            fileSystem.close();
        }
    }
}
