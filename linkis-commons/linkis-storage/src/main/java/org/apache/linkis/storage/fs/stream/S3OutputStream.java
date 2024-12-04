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

package org.apache.linkis.storage.fs.stream;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3OutputStream extends OutputStream {

  private static final Logger LOG = LoggerFactory.getLogger(S3OutputStream.class);

  /** The bucket-name on Amazon S3 */
  private final String bucket;

  /** The path (key) name within the bucket */
  private final String path;

  int BUFFER_SIZE = 5 * 1024 * 1024;

  private final byte[] buf = new byte[BUFFER_SIZE];;

  private byte[] flashBuffer;

  /** The position in the buffer */
  private int position = 0;

  /** Amazon S3 client. */
  private final AmazonS3 s3Client;

  /** The unique id for this upload */
  private String uploadId;

  /** Collection of the etags for the parts that have been uploaded */
  private final List<PartETag> etags = new ArrayList<>();

  /**
   * Creates a new S3 OutputStream
   *
   * @param s3Client the AmazonS3 client
   * @param bucket name of the bucket
   * @param path path within the bucket
   */
  public S3OutputStream(AmazonS3 s3Client, String bucket, String path) {
    if (s3Client == null) {
      throw new IllegalArgumentException("The s3Client cannot be null.");
    }
    if (bucket == null || bucket.isEmpty()) {
      throw new IllegalArgumentException("The bucket cannot be null or an empty string.");
    }
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("The path cannot be null or an empty string.");
    }
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.path = path;
  }

  /**
   * Write an array to the S3 output stream.
   *
   * @param b the byte-array to append
   */
  @Override
  public void write(byte[] b) {
    write(b, 0, b.length);
  }

  /**
   * Writes an array to the S3 Output Stream
   *
   * @param byteArray the array to write
   * @param o the offset into the array
   * @param l the number of bytes to write
   */
  @Override
  public void write(final byte[] byteArray, final int o, final int l) {
    int ofs = o, len = l;
    int size;
    while (len > (size = this.buf.length - position)) {
      System.arraycopy(byteArray, ofs, this.buf, this.position, size);
      this.position += size;
      flushBufferAndRewind();
      ofs += size;
      len -= size;
    }
    System.arraycopy(byteArray, ofs, this.buf, this.position, len);
    this.position += len;
  }

  /** Flushes the buffer by uploading a part to S3. */
  @Override
  public synchronized void flush() {}

  protected void flushBufferAndRewind() {
    if (uploadId == null) {
      LOG.info("Starting a multipart upload for {}/{}", this.bucket, this.path);
      try {
        final InitiateMultipartUploadRequest request =
            new InitiateMultipartUploadRequest(this.bucket, this.path)
                .withCannedACL(CannedAccessControlList.BucketOwnerFullControl);
        InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(request);
        this.uploadId = initResponse.getUploadId();
      } catch (AmazonS3Exception e) {
        LOG.error("Failed to start multipart upload: {}", e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }
    try {
      uploadPart();
    } catch (AmazonS3Exception e) {
      LOG.error("Failed to upload part: {}", e.getMessage(), e);
      this.s3Client.abortMultipartUpload(
          new AbortMultipartUploadRequest(this.bucket, this.path, this.uploadId));
      throw new RuntimeException(e);
    }
    this.position = 0;
  }

  protected void uploadPart() {
    LOG.debug("Uploading part {}", this.etags.size());
    try {
      UploadPartResult uploadResult =
          s3Client.uploadPart(
              new UploadPartRequest()
                  .withBucketName(this.bucket)
                  .withKey(this.path)
                  .withUploadId(this.uploadId)
                  .withInputStream(new ByteArrayInputStream(buf, 0, this.position))
                  .withPartNumber(this.etags.size() + 1)
                  .withPartSize(this.position));
      this.etags.add(uploadResult.getPartETag());
    } catch (AmazonS3Exception e) {
      LOG.error("Failed to upload part: {}", e.getMessage(), e);
      this.s3Client.abortMultipartUpload(
          new AbortMultipartUploadRequest(this.bucket, this.path, this.uploadId));
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    if (this.uploadId != null) {
      if (this.position > 0) {
        uploadPart();
      }
      LOG.debug("Completing multipart");
      try {
        this.s3Client.completeMultipartUpload(
            new CompleteMultipartUploadRequest(bucket, path, uploadId, etags));
      } catch (AmazonS3Exception e) {
        LOG.error("Failed to complete multipart upload: {}", e.getMessage(), e);
        throw new RuntimeException(e);
      }
    } else {
      LOG.debug("Uploading object at once to {}/{}", this.bucket, this.path);
      try {
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(this.position);
        final PutObjectRequest request =
            new PutObjectRequest(
                    this.bucket,
                    this.path,
                    new ByteArrayInputStream(this.buf, 0, this.position),
                    metadata)
                .withCannedAcl(CannedAccessControlList.BucketOwnerFullControl);
        this.s3Client.putObject(request);
      } catch (AmazonS3Exception e) {
        LOG.error("Failed to upload object: {}", e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }
  }

  public void cancel() {
    if (this.uploadId != null) {
      try {
        LOG.debug("Aborting multipart upload");
        this.s3Client.abortMultipartUpload(
            new AbortMultipartUploadRequest(this.bucket, this.path, this.uploadId));
      } catch (AmazonS3Exception e) {
        LOG.error("Failed to abort multipart upload: {}", e.getMessage(), e);
      }
    }
  }

  @Override
  public void write(int b) {

    if (position >= this.buf.length) {
      flushBufferAndRewind();
    }
    this.buf[position++] = (byte) b;
  }
}
