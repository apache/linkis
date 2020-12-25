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
package com.webank.wedatasphere.linkis.cs.highavailable.ha.impl;

import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.cs.common.entity.source.HAContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.CommonHAContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.utils.CSHighAvailableUtils;
import com.webank.wedatasphere.linkis.cs.highavailable.exception.ErrorCode;
import com.webank.wedatasphere.linkis.cs.highavailable.ha.ContextHAChecker;
import com.webank.wedatasphere.linkis.rpc.instancealias.impl.InstanceAliasManagerImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author alexyang
 * @Date 2020/2/19
 */
@Component
public class ContextHACheckerImpl implements ContextHAChecker {

    private final static Logger logger = LoggerFactory.getLogger(ContextHACheckerImpl.class);

    @Autowired
    private InstanceAliasManagerImpl instanceAliasManager;
    /**
     * ${第一个instance长度}${第二个instance长度}{instance别名1}{instance别名2}{实际ID}
     *
     * @param haIDKey
     * @return
     */
    @Override
    public boolean isHAIDValid(String haIDKey) {
        if (CSHighAvailableUtils.checkHAIDBasicFormat(haIDKey)) {
            try {
                return checkHAIDInstance(CSHighAvailableUtils.decodeHAID(haIDKey));
            } catch (CSErrorException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 主备实例同时有效，且id为有效的HAID或数字时才有效
     * @param haContextID
     * @return
     * @throws CSErrorException
     */
    @Override
    public boolean isHAContextIDValid(HAContextID haContextID) throws CSErrorException {
        boolean valid = false;
        if (null != haContextID && StringUtils.isNotBlank(haContextID.getInstance())
                && StringUtils.isNotBlank(haContextID.getBackupInstance())) {
            if (StringUtils.isNotBlank(haContextID.getContextId())) {
                if (StringUtils.isNumeric(haContextID.getContextId())) {
                    valid = checkHAIDInstance(haContextID);
                } else {
                    valid = isHAIDValid(haContextID.getContextId());
                }
            } else {
                valid = false;
            }
        } else {
            valid = false;
        }
        return valid;
    }

    @Override
    public String convertHAIDToHAKey(HAContextID haContextID) throws CSErrorException {
        if (null == haContextID || StringUtils.isBlank(haContextID.getInstance())
                || StringUtils.isBlank(haContextID.getBackupInstance())
                || StringUtils.isBlank(haContextID.getContextId())) {
            throw new CSErrorException(ErrorCode.INVALID_HAID, "Incomplete HAID Object cannot be encoded. mainInstance : "
                    + haContextID.getInstance() + ", backupInstance : " + haContextID.getBackupInstance() + ", contextID : " + haContextID.getContextId());
        }
        if (StringUtils.isNumeric(haContextID.getContextId())) {
            return encode(haContextID);
        } else if (isHAIDValid(haContextID.getContextId())) {
            return haContextID.getContextId();
        } else {
            logger.error("ConvertHAIDToHAKey error, invald HAID : " + haContextID.getContextId());
            throw new CSErrorException(ErrorCode.INVALID_HAID, "ConvertHAIDToHAKey error, invald HAID : " + haContextID.getContextId());
        }
    }

    /**
     * Encode HAContextID to HAKey String.
     * ${第一个instance长度}${第二个instance长度}{instance别名0}{instance别名2}{实际ID}
     * @return
     */
    private String encode(HAContextID haContextID) throws CSErrorException {
        List<String> backupInstanceList = new ArrayList<>();
        if (StringUtils.isNotBlank(haContextID.getBackupInstance())) {
            backupInstanceList.add(haContextID.getBackupInstance());
        } else {
            backupInstanceList.add(haContextID.getInstance());
        }
        return CSHighAvailableUtils.encodeHAIDKey(haContextID.getContextId(), haContextID.getInstance(), backupInstanceList);
    }

    private boolean checkHAIDInstance(HAContextID haContextID) {
        ServiceInstance serverMainInstance = DataWorkCloudApplication.getServiceInstance();
        String mainInstanceAlias = haContextID.getInstance();
        String backupInstanceAlias = haContextID.getBackupInstance();
        if (!instanceAliasManager.isInstanceAliasValid(mainInstanceAlias) && !instanceAliasManager.isInstanceAliasValid(backupInstanceAlias)) {
            return false;
        }
        ServiceInstance mainInstance = instanceAliasManager.getInstanceByAlias(mainInstanceAlias);
        ServiceInstance backupInstance = instanceAliasManager.getInstanceByAlias(backupInstanceAlias);
        if (serverMainInstance.equals(mainInstance) || serverMainInstance.equals(backupInstance)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public HAContextID parseHAIDFromKey(String haIDKey) throws CSErrorException {
        HAContextID haContextID = null;
        if (StringUtils.isBlank(haIDKey) || !CSHighAvailableUtils.checkHAIDBasicFormat(haIDKey)) {
            logger.error("Invalid haIDKey : " + haIDKey);
            throw new CSErrorException(ErrorCode.INVALID_HAID, "Invalid haIDKey : " + haIDKey);
        }
        return CSHighAvailableUtils.decodeHAID(haIDKey);
    }
}
