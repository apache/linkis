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

package org.apache.linkis.bml.common;

public enum OperationEnum {
  /** 任务操作类型 */
  UPLOAD("upload", 0),
  UPDATE("update", 1),
  DOWNLOAD("download", 2),
  DELETE_VERSION("deleteVersion", 3),
  DELETE_RESOURCE("deleteResource", 4),
  DELETE_RESOURCES("deleteResources", 5),
  ROLLBACK_VERSION("rollbackVersion", 6),
  COPY_RESOURCE("copyResource", 7);

  private String value;
  private int id;

  private OperationEnum(String value, int id) {
    this.value = value;
    this.id = id;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
