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

package org.apache.linkis.storage.exception;

public enum StorageErrorCode {

  /** */
  FS_NOT_INIT(53001, "please init first"),

  INCONSISTENT_DATA(53001, "Inconsistent row data read,read %s,need rowLen %s"),
  FS_OOM(53002, "OOM occurred while reading the file"),

  FS_ERROR(53003, "Failed to operation fs"),

  READ_PARQUET_FAILED(53004, "Failed to read parquet file"),

  READ_ORC_FAILED(53005, "Failed to read orc file");

  StorageErrorCode(int errorCode, String message) {
    this.code = errorCode;
    this.message = message;
  }

  private int code;

  private String message;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
