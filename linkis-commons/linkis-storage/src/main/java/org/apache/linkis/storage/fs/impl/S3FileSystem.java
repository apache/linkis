/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.storage.fs.impl;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.domain.FsPathListWithError;
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.TO_BE_UNKNOW;

public class S3FileSystem extends FileSystem {
  private static final Logger logger = LoggerFactory.getLogger(S3FileSystem.class);
  private String accessKey;
  private String secretKey;

  private String endPoint;

  private String region;

  private String bucket;

  private String label;

  private AmazonS3 s3Client;

  private static final String INIT_FILE_NAME = ".s3_dir_init";

  @Override
  public void init(Map<String, String> properties) throws IOException {
    accessKey = StorageConfiguration.S3_ACCESS_KEY.getValue(properties);
    secretKey = StorageConfiguration.S3_SECRET_KEY.getValue(properties);
    endPoint = StorageConfiguration.S3_ENDPOINT.getValue(properties);
    bucket = StorageConfiguration.S3_BUCKET.getValue(properties);
    region = StorageConfiguration.S3_REGION.getValue(properties);

    AwsClientBuilder.EndpointConfiguration endpointConfiguration =
        new AwsClientBuilder.EndpointConfiguration(endPoint, region);

    BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, secretKey);

    AWSStaticCredentialsProvider StaticCredentials =
        new AWSStaticCredentialsProvider(basicAWSCredentials);

    s3Client =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(endpointConfiguration)
            .withPathStyleAccessEnabled(true)
            .withCredentials(StaticCredentials)
            .build();
  }

  @Override
  public String fsName() {
    return StorageUtils.S3;
  }

  @Override
  public String rootUserName() {
    return null;
  }

  @Override
  public FsPath get(String dest) throws IOException {
    FsPath ret = new FsPath(dest);
    if (exists(ret)) {
      return ret;
    } else {
      logger.warn("File or folder does not exist or file name is garbled(文件或者文件夹不存在或者文件名乱码)");
      throw new StorageWarnException(
          TO_BE_UNKNOW.getErrorCode(),
          "File or folder does not exist or file name is garbled(文件或者文件夹不存在或者文件名乱码)");
    }
  }

  @Override
  public InputStream read(FsPath dest) throws IOException {
    try {
      return s3Client.getObject(bucket, dest.getPath()).getObjectContent();
    } catch (AmazonS3Exception e) {
      throw new IOException("You have not permission to access path " + dest.getPath());
    }
  }

  @Override
  public OutputStream write(FsPath dest, boolean overwrite) throws IOException {
    try (InputStream inputStream = read(dest);
        OutputStream outputStream = new S3OutputStream(s3Client, bucket, dest.getPath())) {
      if (overwrite) {
        IOUtils.copy(inputStream, outputStream);
      }
      return outputStream;
    }
  }

  @Override
  public boolean create(String dest) throws IOException {
    if (exists(new FsPath(dest))) {
      return false;
    }
    s3Client.putObject(bucket, dest, "");
    return true;
  }

  @Override
  public List<FsPath> list(FsPath path) throws IOException {
    try {
      if (!StringUtils.isEmpty(path.getPath())) {
        ListObjectsV2Result listObjectsV2Result = s3Client.listObjectsV2(bucket, path.getPath());
        List<S3ObjectSummary> s3ObjectSummaries = listObjectsV2Result.getObjectSummaries();
        return s3ObjectSummaries.stream()
            .filter(summary -> !isInitFile(summary))
            .map(
                summary -> {
                  FsPath newPath = new FsPath(buildPath(summary.getKey()));
                  return fillStorageFile(newPath, summary);
                })
            .collect(Collectors.toList());
      }
    } catch (AmazonS3Exception e) {
      throw new IOException("You have not permission to access path " + path.getPath());
    }

    return new ArrayList<>();
  }

  @Override
  public FsPathListWithError listPathWithError(FsPath path) throws IOException {
    try {
      if (!StringUtils.isEmpty(path.getPath())) {
        ListObjectsV2Result listObjectsV2Result = s3Client.listObjectsV2(bucket, path.getPath());
        List<S3ObjectSummary> s3ObjectSummaries = listObjectsV2Result.getObjectSummaries();
        if (s3ObjectSummaries != null) {
          List<FsPath> rtn = new ArrayList();
          String message = "";
          for (S3ObjectSummary summary : s3ObjectSummaries) {
            if (isDir(summary, path.getPath()) || isInitFile(summary)) continue;
            FsPath newPath = new FsPath(buildPath(summary.getKey()));
            rtn.add(fillStorageFile(newPath, summary));
          }
          return new FsPathListWithError(rtn, message);
        }
      }
    } catch (AmazonS3Exception e) {
      throw new IOException("You have not permission to access path " + path.getPath());
    }

    return null;
  }

  @Override
  public boolean exists(FsPath dest) throws IOException {
    try {
      int size = s3Client.listObjectsV2(bucket, dest.getPath()).getObjectSummaries().size();
      return size > 0;
    } catch (AmazonS3Exception e) {
      return false;
    }
  }

  @Override
  public boolean delete(FsPath dest) throws IOException {
    try {
      s3Client.deleteObject(bucket, dest.getPath());
      return true;
    } catch (AmazonS3Exception e) {
      throw new IOException("You have not permission to access path " + dest.getPath());
    }
  }

  @Override
  public boolean renameTo(FsPath oldDest, FsPath newDest) throws IOException {
    try {
      s3Client.copyObject(bucket, oldDest.getPath(), bucket, newDest.getPath());
      s3Client.deleteObject(bucket, oldDest.getPath());
      return true;
    } catch (AmazonS3Exception e) {
      s3Client.deleteObject(bucket, newDest.getPath());
      throw new IOException(
          "You have not permission to access path "
              + oldDest.getPath()
              + " or "
              + newDest.getPath());
    }
  }

  @Override
  public boolean copy(String origin, String dest) throws IOException {
    try {
      s3Client.copyObject(bucket, origin, bucket, dest);
      return true;
    } catch (AmazonS3Exception e) {
      throw new IOException("You have not permission to access path " + origin + " or " + dest);
    }
  }

  private boolean isDir(S3ObjectSummary s3ObjectSummary, String prefix) {
    return s3ObjectSummary.getKey().substring(prefix.length()).contains("/");
  }

  private boolean isInitFile(S3ObjectSummary s3ObjectSummary) {
    return s3ObjectSummary.getKey().contains(INIT_FILE_NAME);
  }

  @Override
  public String listRoot() {
    return "/";
  }

  @Override
  public boolean mkdir(FsPath dest) throws IOException {
    String path = new File(dest.getPath(), INIT_FILE_NAME).getPath();
    if (exists(new FsPath(path))) {
      return false;
    }
    return create(path);
  }

  @Override
  public boolean mkdirs(FsPath dest) throws IOException {
    return mkdir(dest);
  }

  private FsPath fillStorageFile(FsPath fsPath, S3ObjectSummary s3ObjectSummary) {
    fsPath.setModification_time(s3ObjectSummary.getLastModified().getTime());
    fsPath.setOwner(s3ObjectSummary.getOwner().getDisplayName());
    try {
      fsPath.setIsdir(isDir(s3ObjectSummary, fsPath.getParent().getPath()));
    } catch (Throwable e) {
      logger.warn("Failed to fill storage file：" + fsPath.getPath(), e);
    }

    if (fsPath.isdir()) {
      fsPath.setLength(0);
    } else {
      fsPath.setLength(s3ObjectSummary.getSize());
    }
    return fsPath;
  }

  @Override
  public boolean canRead(FsPath dest) {
    return true;
  }

  @Override
  public boolean canWrite(FsPath dest) {
    return true;
  }

  @Override
  public long getTotalSpace(FsPath dest) {
    return 0;
  }

  @Override
  public long getFreeSpace(FsPath dest) {
    return 0;
  }

  @Override
  public long getUsableSpace(FsPath dest) {
    return 0;
  }

  @Override
  public boolean canExecute(FsPath dest) {
    return true;
  }

  @Override
  public boolean setOwner(FsPath dest, String user, String group) {
    return false;
  }

  @Override
  public boolean setOwner(FsPath dest, String user) {
    return false;
  }

  @Override
  public boolean setGroup(FsPath dest, String group) {
    return false;
  }

  @Override
  public boolean setPermission(FsPath dest, String permission) {
    return false;
  }

  @Override
  public void close() throws IOException {}

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String buildPath(String path) {
    if (path == null || "".equals(path)) return "";
    if (path.startsWith("/")) {
      return StorageUtils.S3_SCHEMA + path;
    }
    return StorageUtils.S3_SCHEMA + "/" + path;
  }
}

class S3OutputStream extends ByteArrayOutputStream {
  private AmazonS3 s3Client;
  private String bucket;
  private String path;

  public S3OutputStream(AmazonS3 s3Client, String bucket, String path) {
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.path = path;
  }

  @Override
  public void close() throws IOException {
    byte[] buffer = this.toByteArray();
    try (InputStream in = new ByteArrayInputStream(buffer)) {
      s3Client.putObject(bucket, path, in, new ObjectMetadata());
    }
  }
}
