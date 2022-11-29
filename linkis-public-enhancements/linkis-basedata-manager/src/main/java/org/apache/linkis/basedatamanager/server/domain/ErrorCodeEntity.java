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

package org.apache.linkis.basedatamanager.server.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** @TableName linkis_ps_error_code */
@TableName(value = "linkis_ps_error_code")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorCodeEntity implements Serializable {
  /** */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** */
  private String errorCode;

  /** */
  private String errorDesc;

  /** */
  private String errorRegex;

  /** */
  private Integer errorType;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;

  /** */
  public Long getId() {
    return id;
  }

  /** */
  public void setId(Long id) {
    this.id = id;
  }

  /** */
  public String getErrorCode() {
    return errorCode;
  }

  /** */
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  /** */
  public String getErrorDesc() {
    return errorDesc;
  }

  /** */
  public void setErrorDesc(String errorDesc) {
    this.errorDesc = errorDesc;
  }

  /** */
  public String getErrorRegex() {
    return errorRegex;
  }

  /** */
  public void setErrorRegex(String errorRegex) {
    this.errorRegex = errorRegex;
  }

  /** */
  public Integer getErrorType() {
    return errorType;
  }

  /** */
  public void setErrorType(Integer errorType) {
    this.errorType = errorType;
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) {
      return true;
    }
    if (that == null) {
      return false;
    }
    if (getClass() != that.getClass()) {
      return false;
    }
    ErrorCodeEntity other = (ErrorCodeEntity) that;
    return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
        && (this.getErrorCode() == null
            ? other.getErrorCode() == null
            : this.getErrorCode().equals(other.getErrorCode()))
        && (this.getErrorDesc() == null
            ? other.getErrorDesc() == null
            : this.getErrorDesc().equals(other.getErrorDesc()))
        && (this.getErrorRegex() == null
            ? other.getErrorRegex() == null
            : this.getErrorRegex().equals(other.getErrorRegex()))
        && (this.getErrorType() == null
            ? other.getErrorType() == null
            : this.getErrorType().equals(other.getErrorType()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    result = prime * result + ((getErrorCode() == null) ? 0 : getErrorCode().hashCode());
    result = prime * result + ((getErrorDesc() == null) ? 0 : getErrorDesc().hashCode());
    result = prime * result + ((getErrorRegex() == null) ? 0 : getErrorRegex().hashCode());
    result = prime * result + ((getErrorType() == null) ? 0 : getErrorType().hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(" [");
    sb.append("Hash = ").append(hashCode());
    sb.append(", id=").append(id);
    sb.append(", errorCode=").append(errorCode);
    sb.append(", errorDesc=").append(errorDesc);
    sb.append(", errorRegex=").append(errorRegex);
    sb.append(", errorType=").append(errorType);
    sb.append(", serialVersionUID=").append(serialVersionUID);
    sb.append("]");
    return sb.toString();
  }
}
