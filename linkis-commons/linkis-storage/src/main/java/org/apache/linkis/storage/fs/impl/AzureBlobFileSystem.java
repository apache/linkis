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
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;

import static org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.TO_BE_UNKNOW;

public class AzureBlobFileSystem extends FileSystem {

  private static final String SLASH = "/";

  public static class PahtInfo {
    private String schema = "http://"; // http
    private String domain; //
    private String container; // container name
    private String blobName; // blob name
    private String tail;

    public PahtInfo(String domain, String container, String blobName) {
      this.domain = domain;
      this.container = container;
      this.blobName = blobName;
      if (blobName != null) {
        String[] names = blobName.split(SLASH, -1);
        tail = names[names.length - 1];
      }
    }

    public String toFullName() {
      return schema + domain + SLASH + container + SLASH + blobName;
    }

    public String getSchema() {
      return schema;
    }

    public String getDomain() {
      return domain;
    }

    public String getContainer() {
      return container;
    }

    public String getBlobName() {
      return blobName;
    }

    public String getTail() {
      return tail;
    }

    @Override
    public String toString() {
      return "PahtInfo{"
          + "schema='"
          + schema
          + '\''
          + ", domain='"
          + domain
          + '\''
          + ", container='"
          + container
          + '\''
          + ", blobName='"
          + blobName
          + '\''
          + ", tail='"
          + tail
          + '\''
          + '}';
    }
  }

  /** manipulate Azure storage resources and Blob container 管理命名空间下的存储资源和Blob容器 */
  private BlobServiceClient serviceClient;

  /**
   * getBlobContainerClient
   *
   * @param containerName
   * @return client which can manipulate Azure Storage containers and their blobs.<br>
   *     操作一个容器和其blobs的客户端
   */
  private BlobContainerClient getBlobContainerClient(String containerName) {
    return serviceClient.getBlobContainerClient(containerName);
  }

  private PahtInfo azureLocation(String path) {
    return this.azureLocation(new FsPath(path));
  }

  /**
   * @param dest
   * @return domain name,container name,blob name
   */
  private PahtInfo azureLocation(FsPath dest) {
    // https://myaccount.blob.core.windows.net/mycontainer/dir/blobname
    // returns myaccount.blob.core.windows.net/mycontainer/dir/blobname
    String path = dest.getPath();
    // myaccount.blob.core.windows.net/mycontainer/dir/blobname
    // will split to myaccount.blob.core.windows.net
    // and mycontainer/dir/blobname
    String[] paths = path.split(SLASH, 2);
    if (paths.length < 2) {
      throw new IllegalArgumentException("file path error,with out container:" + path);
    }
    // split to container and blob object,
    // container/dir/blobname will split to container and dir/blobname
    String[] names = paths[1].split(SLASH, 2);
    if (names.length < 2) {
      return new PahtInfo(paths[0], names[0], null);
    } else {
      return new PahtInfo(paths[0], names[0], names[1]);
    }
  }

  /**
   * init serviceClient
   *
   * @param properties
   * @throws IOException
   */
  @Override
  public void init(Map<String, String> properties) throws IOException {

    /**
     * The storage account provides the top-level namespace for the Blob service. 每个账户提供了一个顶级的命名空间
     */
    String acctName = StorageConfiguration.AZURE_ACCT_NAME().getValue(properties);
    String connectStr = StorageConfiguration.AZURE_ACCT_CONNECT_STR().getValue(properties);
    // Azure SDK client builders accept the credential as a parameter
    serviceClient =
        new BlobServiceClientBuilder()
            .endpoint(StorageUtils.BLOB_SCHEMA() + acctName + ".blob.core.windows.net/")
            .connectionString(connectStr)
            .buildClient();
  }

  /**
   * name of the fileSystem
   *
   * @return
   */
  @Override
  public String fsName() {
    return StorageUtils.BLOB();
  }

  @Override
  public String rootUserName() {
    return "";
  }

  /**
   * @param dest
   * @return
   * @throws IOException
   */
  @Override
  public FsPath get(String dest) throws IOException {
    FsPath path = new FsPath(dest);
    if (exists(path)) {
      return path;
    } else {
      throw new StorageWarnException(
          TO_BE_UNKNOW.getErrorCode(),
          "File or folder does not exist or file name is garbled(文件或者文件夹不存在或者文件名乱码)");
    }
  }

  /**
   * Opens a blob input stream to download the blob.
   *
   * @param dest
   * @return
   * @throws BlobStorageException – If a storage service error occurred.
   */
  @Override
  public InputStream read(FsPath dest) {
    PahtInfo result = azureLocation(dest);
    BlobClient blobclient =
        getBlobContainerClient(result.getContainer()).getBlobClient(result.getBlobName());
    return blobclient.openInputStream();
  }

  /**
   * @param dest
   * @param overwrite
   * @return
   * @throws BlobStorageException – If a storage service error occurred.
   * @see BlockBlobClient #getBlobOutputStream
   */
  @Override
  public OutputStream write(FsPath dest, boolean overwrite) {

    PahtInfo result = azureLocation(dest);
    BlobClient blobclient =
        getBlobContainerClient(result.getContainer()).getBlobClient(result.getBlobName());
    return blobclient.getBlockBlobClient().getBlobOutputStream(overwrite);
  }

  /**
   * create a blob<br>
   * 创建一个对象("文件")
   *
   * @param dest
   * @return
   * @throws IOException
   */
  @Override
  public boolean create(String dest) throws IOException {
    FsPath path = new FsPath(dest);
    if (exists(path)) {
      return false;
    }
    PahtInfo names = this.azureLocation(dest);
    // TODO 如果是路径的话后面补一个文件.
    if (!names.getTail().contains(".")) {
      String tmp = names.toFullName() + SLASH + "_tmp.txt";
      names = this.azureLocation(tmp);
    }
    BlobContainerClient client = serviceClient.createBlobContainerIfNotExists(names.getContainer());
    try (BlobOutputStream bos =
        client.getBlobClient(names.getBlobName()).getBlockBlobClient().getBlobOutputStream()) {
      bos.write(1);
      bos.flush();
    }

    return true;
  }

  /**
   * Flat listing 5000 results at a time,without deleted.<br>
   * 扁平化展示未删除的blob对象,最多5000条 TODO 分页接口,迭代器接口?
   *
   * @param path
   * @return
   * @throws IOException
   */
  @Override
  public List<FsPath> list(FsPath path) throws IOException {
    final PahtInfo result = azureLocation(path);
    return getBlobContainerClient(result.getContainer()).listBlobs().stream()
        // Azure不会返回已删除对象
        .filter(item -> !item.isDeleted())
        .map(
            item -> {
              FsPath tmp = new FsPath(result.toFullName() + SLASH + item.getName());
              // TODO 根据观察使用contentType来区别"对象"和"路径",但文档中没有具体的说明
              if (item.getProperties().getContentType() == null) {
                tmp.setIsdir(true);
              }
              return tmp;
            })
        .collect(Collectors.toList());
  }

  @Override
  public boolean canRead(FsPath dest) throws IOException {
    if (this.exists(dest)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean canRead(FsPath dest, String user) throws IOException {
    return false;
  }

  @Override
  public boolean canWrite(FsPath dest) throws IOException {
    if (this.exists(dest)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean exists(FsPath dest) throws IOException {
    PahtInfo file = this.azureLocation(dest);
    return getBlobContainerClient(file.getContainer()).getBlobClient(file.getBlobName()).exists();
  }

  @Override
  public boolean delete(FsPath dest) throws IOException {
    PahtInfo file = this.azureLocation(dest);
    return getBlobContainerClient(file.getContainer())
        .getBlobClient(file.getBlobName())
        .deleteIfExists();
  }

  @Override
  public boolean copy(String origin, String dest) throws IOException {
    PahtInfo oriNames = this.azureLocation(origin);
    PahtInfo destNames = this.azureLocation(dest);

    BlobClient oriClient =
        getBlobContainerClient(oriNames.getContainer()).getBlobClient(oriNames.getBlobName());
    BlockBlobClient destClient =
        getBlobContainerClient(destNames.getContainer())
            .getBlobClient(destNames.getBlobName())
            .getBlockBlobClient();
    SyncPoller<BlobCopyInfo, Void> poller =
        destClient.beginCopy(oriClient.getBlobUrl(), Duration.ofSeconds(2));
    poller.waitForCompletion();
    return true;
  }

  @Override
  public boolean renameTo(FsPath oldDest, FsPath newDest) throws IOException {
    // 没有事务性保证
    this.copy(oldDest.getPath(), newDest.getPath());
    this.delete(oldDest);
    return true;
  }

  @Override
  public boolean mkdir(FsPath dest) throws IOException {
    return this.create(dest.getPath());
  }

  @Override
  public boolean mkdirs(FsPath dest) throws IOException {
    return this.mkdir(dest);
  }

  // 下面这些方法可能都无法支持
  @Override
  public String listRoot() throws IOException {
    return "";
  }

  @Override
  public long getTotalSpace(FsPath dest) throws IOException {
    return 0;
  }

  @Override
  public long getFreeSpace(FsPath dest) throws IOException {
    return 0;
  }

  @Override
  public long getUsableSpace(FsPath dest) throws IOException {
    return 0;
  }

  @Override
  public long getLength(FsPath dest) throws IOException {
    return 0;
  }

  @Override
  public String getChecksum(FsPath dest) throws IOException {
    return null;
  }

  @Override
  public boolean canExecute(FsPath dest) throws IOException {
    return false;
  }

  @Override
  public boolean setOwner(FsPath dest, String user, String group) throws IOException {
    return false;
  }

  @Override
  public boolean setOwner(FsPath dest, String user) throws IOException {
    return false;
  }

  @Override
  public boolean setGroup(FsPath dest, String group) throws IOException {
    return false;
  }

  @Override
  public boolean setPermission(FsPath dest, String permission) throws IOException {
    return false;
  }

  @Override
  public void close() throws IOException {}
}
