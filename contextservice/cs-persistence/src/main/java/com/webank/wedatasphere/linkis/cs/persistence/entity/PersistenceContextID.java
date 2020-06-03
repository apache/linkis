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
package com.webank.wedatasphere.linkis.cs.persistence.entity;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ExpireType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.HAContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.UserContextID;
import com.webank.wedatasphere.linkis.cs.persistence.annotation.Ignore;

import java.util.Date;

/**
 * Created by patinousward on 2020/2/12.
 */
@Ignore
public class PersistenceContextID implements UserContextID, HAContextID {

    private String contextId;

    private String user;

    private String instance;

    private String backupInstance;

    private String application;

    private ExpireType expireType;

    private Date expireTime;

    private String source;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String getContextId() {
        return this.contextId;
    }

    @Override
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public String getInstance() {
        return this.instance;
    }

    @Override
    public void setInstance(String instance) {
        this.instance = instance;
    }

    @Override
    public String getBackupInstance() {
        return this.backupInstance;
    }

    @Override
    public void setBackupInstance(String backupInstance) {
        this.backupInstance = backupInstance;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public ExpireType getExpireType() {
        return expireType;
    }

    public void setExpireType(ExpireType expireType) {
        this.expireType = expireType;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

}
