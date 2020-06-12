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
package com.webank.wedatasphere.linkis.cs.common.entity.resource;

import java.util.Date;

/**
 * Created by patinousward on 2020/2/11.
 */
public interface UDFResource extends Resource {

    String getCreateUser();

    void setCreateUser(String createUser);

    String getUdfName();

    void setUdfName(String udfName);

    Integer getUdfType();

    void setUdfType(Integer udfType);

    String getPath();

    void setPath(String path);

    String getRegisterFormat();

    void setRegisterFormat(String registerFormat);

    String getUseFormat();

    void setUseFormat(String useFormat);

    String getDescription();

    void setDescription(String description);

    Boolean getExpire();

    void setExpire(Boolean expire);

    Boolean getShared();

    void setShared(Boolean shared);

    Long getTreeId();

    void setTreeId(Long treeId);

    Date getCreateTime();

    void setCreateTime(Date createTime);

    Date getUpdateTime();

    void setUpdateTime(Date updateTime);

    Boolean getLoad();

    void setLoad(Boolean load);
}
