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
import org.apache.linkis.storage.fs.stream.S3OutputStream;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
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
    accessKey = StorageConfiguration.S3_ACCESS_KEY().getValue(properties);
    secretKey = StorageConfiguration.S3_SECRET_KEY().getValue(properties);
    endPoint = StorageConfiguration.S3_ENDPOINT().getValue(properties);
    bucket = StorageConfiguration.S3_BUCKET().getValue(properties);
    region = StorageConfiguration.S3_REGION().getValue(properties);

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
    return StorageUtils.S3();
  }

  @Override
  public String rootUserName() {
    return null;
  }

  @Override
  public FsPath get(String dest) throws IOException {
    FsPath ret = new FsPath(dest);
    if (exists(ret)) {
      ret.setIsdir(isDir(buildKey(ret.getPath())));
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
      return s3Client.getObject(bucket, buildKey(dest.getPath())).getObjectContent();
    } catch (AmazonS3Exception e) {
      throw new IOException("You have not permission to access path " + dest.getPath());
    }
  }

  @Override
  public OutputStream write(FsPath dest, boolean overwrite) throws IOException {
    InputStream inputStream = null;
    try {
      if (!exists(dest)) {
        create(dest.getPath());
      }

      OutputStream outputStream = new S3OutputStream(s3Client, bucket, buildKey(dest.getPath()));

      if (!overwrite) {
        inputStream = read(dest);
        IOUtils.copy(inputStream, outputStream);
      }
      return outputStream;
    } catch (IOException e) {
      throw new IOException("You have not permission to access path " + dest.getPath());
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  @Override
  public boolean create(String dest) throws IOException {
    if (exists(new FsPath(dest))) {
      return false;
    }
    s3Client.putObject(bucket, buildKey(dest), "");
    return true;
  }

  @Override
  public List<FsPath> list(FsPath path) throws IOException {
    try {
      if (!StringUtils.isEmpty(path.getPath())) {
        ListObjectsV2Request listObjectsV2Request =
            new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(buildKey(path.getPath()) + "/")
                .withDelimiter("/");
        ListObjectsV2Result dirResult = s3Client.listObjectsV2(listObjectsV2Request);
        List<S3ObjectSummary> s3ObjectSummaries = dirResult.getObjectSummaries();
        List<String> commonPrefixes = dirResult.getCommonPrefixes();
        List<FsPath> fsPaths =
            s3ObjectSummaries.stream()
                .filter(summary -> !isInitFile(summary))
                .map(
                    summary -> {
                      FsPath newPath = new FsPath(buildPath(summary.getKey()));
                      return fillStorageFile(newPath, summary);
                    })
                .collect(Collectors.toList());
        if (commonPrefixes != null) {
          for (String dir : commonPrefixes) {
            FsPath newPath = new FsPath(buildPath(dir));
            newPath.setIsdir(true);
            fsPaths.add(newPath);
          }
        }
        return fsPaths;
      }
    } catch (AmazonS3Exception e) {
      throw new IOException("You have not permission to access path " + path.getPath());
    }

    return new ArrayList<>();
  }

  @Override
  public FsPathListWithError listPathWithError(FsPath path) throws IOException {
    return listPathWithError(path, true);
  }

  public FsPathListWithError listPathWithError(FsPath path, boolean ignoreInitFile)
      throws IOException {
    List<FsPath> rtn = new ArrayList<>();
    try {
      if (!StringUtils.isEmpty(path.getPath())) {
        ListObjectsV2Request listObjectsV2Request =
            new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(buildKey(path.getPath()) + "/")
                .withDelimiter("/");
        ListObjectsV2Result dirResult = s3Client.listObjectsV2(listObjectsV2Request);
        List<S3ObjectSummary> s3ObjectSummaries = dirResult.getObjectSummaries();
        List<String> commonPrefixes = dirResult.getCommonPrefixes();
        if (s3ObjectSummaries != null) {
          for (S3ObjectSummary summary : s3ObjectSummaries) {
            if (isInitFile(summary) && ignoreInitFile) continue;
            FsPath newPath = new FsPath(buildPath(summary.getKey()));
            rtn.add(fillStorageFile(newPath, summary));
          }
        }
        if (commonPrefixes != null) {
          for (String dir : commonPrefixes) {
            FsPath newPath = new FsPath(buildPath(dir));
            newPath.setIsdir(true);
            rtn.add(newPath);
          }
        }
        return new FsPathListWithError(rtn, "");
      }
    } catch (AmazonS3Exception e) {
      throw new IOException("You have not permission to access path " + path.getPath());
    }

    return null;
  }

  @Override
  public boolean exists(FsPath dest) throws IOException {
    try {
      if (dest == null) {
        return false;
      }
      ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
      listObjectsV2Request
          .withBucketName(bucket)
          .withPrefix(buildKey(dest.getPath()))
          .withMaxKeys(1);
      return !s3Client.listObjectsV2(listObjectsV2Request).getObjectSummaries().isEmpty();
    } catch (AmazonS3Exception e) {
      return false;
    }
  }

  @Override
  public boolean delete(FsPath dest) throws IOException {
    try {
      List<String> deleteKeys = new ArrayList<>();
      delete(dest, deleteKeys);
      if (!deleteKeys.isEmpty()) {
        DeleteObjectsRequest deleteObjectsRequest =
            new DeleteObjectsRequest(bucket).withKeys(deleteKeys.toArray(new String[0]));
        s3Client.deleteObjects(deleteObjectsRequest);
      }
      return true;
    } catch (AmazonS3Exception e) {
      throw new IOException("You have not permission to access path " + dest.getPath());
    }
  }

  public void delete(FsPath dest, List<String> keys) throws IOException {
    if (isDir(buildKey(dest.getPath()))) {
      FsPathListWithError fsPathListWithError = listPathWithError(dest, false);
      List<FsPath> fsPaths = fsPathListWithError.getFsPaths();
      fsPaths.forEach(
          fsPath -> {
            try {
              delete(fsPath, keys);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
    } else {
      keys.add(buildKey(dest.getPath()));
    }
  }

  @Override
  public boolean renameTo(FsPath oldDest, FsPath newDest) throws IOException {
    try {
      String newOriginPath = buildKey(oldDest.getPath());
      String newDestPath = buildKey(newDest.getPath());
      ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
      listObjectsV2Request.withBucketName(bucket).withPrefix(newOriginPath);
      ListObjectsV2Result result = s3Client.listObjectsV2(listObjectsV2Request);
      List<String> keyList =
          result.getObjectSummaries().stream()
              .map(S3ObjectSummary::getKey)
              .collect(Collectors.toList());
      List<String> newKeyList =
          keyList.stream()
              .map(key -> key.replaceFirst(newOriginPath, newDestPath))
              .collect(Collectors.toList());
      for (int i = 0; i < keyList.size(); i++) {
        String key = keyList.get(i);
        String newKey = newKeyList.get(i);
        s3Client.copyObject(bucket, key, bucket, newKey);
        s3Client.deleteObject(bucket, key);
      }
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
      String newOrigin = buildKey(origin);
      String newDest = buildKey(dest);
      ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
      listObjectsV2Request.withBucketName(bucket).withPrefix(newOrigin);
      ListObjectsV2Result result = s3Client.listObjectsV2(listObjectsV2Request);
      List<String> keyList =
          result.getObjectSummaries().stream()
              .map(S3ObjectSummary::getKey)
              .collect(Collectors.toList());
      List<String> newKeyList =
          keyList.stream()
              .map(key -> key.replaceFirst(newOrigin, newDest))
              .collect(Collectors.toList());
      for (int i = 0; i < keyList.size(); i++) {
        String key = keyList.get(i);
        String newKey = newKeyList.get(i);
        s3Client.copyObject(bucket, key, bucket, newKey);
      }
      return true;
    } catch (AmazonS3Exception e) {
      throw new IOException("You have not permission to access path " + origin + " or " + dest);
    }
  }

  private boolean isDir(String key) {
    ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
    listObjectsV2Request
        .withBucketName(bucket)
        .withPrefix(key + "/")
        .withDelimiter("/")
        .withMaxKeys(1);

    return !(s3Client.listObjectsV2(listObjectsV2Request).getCommonPrefixes().isEmpty()
        && s3Client.listObjectsV2(listObjectsV2Request).getObjectSummaries().isEmpty());
  }

  private boolean isInitFile(S3ObjectSummary s3ObjectSummary) {
    return s3ObjectSummary.getKey().contains(INIT_FILE_NAME);
  }

  @Override
  public String listRoot() {
    return "/";
  }

  /**
   * s3没有目录概念，无法直接创建目录 S3 lacks the concept of directories and cannot create directories directly.
   *
   * @param dest
   * @return
   * @throws IOException
   */
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
    Owner owner = s3ObjectSummary.getOwner();
    if (owner != null) {
      fsPath.setOwner(owner.getDisplayName());
    }
    try {
      fsPath.setIsdir(isDir(s3ObjectSummary.getKey()));
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
  public boolean canRead(FsPath dest, String user) throws IOException {
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
  public long getLength(FsPath dest) throws IOException {
    return s3Client
        .getObject(bucket, buildKey(dest.getPath()))
        .getObjectMetadata()
        .getContentLength();
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
  public void close() throws IOException {
    s3Client.shutdown();
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String buildPath(String path) {
    if (path == null || path.isEmpty()) return "";
    if (path.startsWith("/")) {
      return StorageUtils.S3_SCHEMA() + path;
    }
    return StorageUtils.S3_SCHEMA() + "/" + path;
  }

  public String buildKey(String path) {
    String res = path;
    if (path == null || path.isEmpty()) return "";
    if (path.startsWith("/")) {
      res = path.replaceFirst("/", "");
    }
    if (path.endsWith("/") && !res.isEmpty()) {
      res = res.substring(0, res.length() - 1);
    }
    return res;
  }
}
