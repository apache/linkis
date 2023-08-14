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

package org.apache.linkis.cs.common.protocol;

public enum ContextIDType {
  /** index表示contextValueType的int typeName 表示全路径类名 */
  COMMON_CONTEXT_ID_TYPE(0, "org.apache.linkis.cs.common.entity.source.CommonContextID"),
  LINKIS_WORKFLOW_CONTEXT_ID_TYPE(
      1, "org.apache.linkis.cs.common.entity.source.LinkisWorkflowContextID");

  private int index;
  private String typeName;

  private ContextIDType(int index, String typeName) {
    this.index = index;
    this.typeName = typeName;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public static String getTypeName(int index) {
    for (ContextIDType type : ContextIDType.values()) {
      if (type.index == index) {
        return type.typeName;
      }
    }
    return LINKIS_WORKFLOW_CONTEXT_ID_TYPE.typeName;
  }
}
