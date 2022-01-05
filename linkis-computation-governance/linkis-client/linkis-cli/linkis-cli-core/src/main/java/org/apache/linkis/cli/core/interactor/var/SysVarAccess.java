/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cli.core.interactor.var;

import org.apache.linkis.cli.common.entity.properties.ClientProperties;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.VarAccessException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: visit sys_prop and sys_env
 */
public class SysVarAccess implements VarAccess {
    private static Logger logger = LoggerFactory.getLogger(SysVarAccess.class);
    private ClientProperties sysProp;
    private ClientProperties sysEnv;

    public SysVarAccess() {
    }

    public SysVarAccess(ClientProperties sysProp, ClientProperties sysEnv) {
        this.sysProp = sysProp;
        this.sysEnv = sysEnv;
    }

    public SysVarAccess setSysProp(ClientProperties sysProp) {
        this.sysProp = sysProp;
        return this;
    }

    public ClientProperties getSysProp(String identifier) {
        return this.sysProp;
    }

    public SysVarAccess setSysEnv(ClientProperties sysEnv) {
        this.sysEnv = sysEnv;
        return this;
    }

    public ClientProperties getSysEnv(String identifier) {
        return this.sysEnv;
    }


    @Override
    public void checkInit() {
        if (this.sysProp == null &&
                this.sysEnv == null) {
            throw new VarAccessException("VA0001", ErrorLevel.ERROR, CommonErrMsg.VarAccessInitErr, "sys_prop and sys_env are both null");
        }
    }

    @Override
    public <T> T getVar(Class<T> clazz, String key) {
        checkInit();
        if (clazz != String.class) {
            //throw exception
        }
        Object o1 = sysProp.get(key);
        Object o2 = sysEnv.get(key);
        if (o1 != null && o2 != null) {
            throw new VarAccessException("VA0002", ErrorLevel.WARN, CommonErrMsg.VarAccessErr, "same key occurred in sys_prop and sys_env. will use sys_prop");
        }
        Object ret = o1 != null ? o1 : o2;
        return clazz.cast(ret);
    }

    @Override
    public <T> T getVarOrDefault(Class<T> clazz, String key, T defaultValue) {
        T ret = getVar(clazz, key);
        if (ret == null) {
            ret = defaultValue;
        }
        return ret;
    }

    @Override
    public String[] getAllVarKeys() {
        List<String> varKeys = new ArrayList<>();
        if (sysProp != null) {
            for (Object key : sysProp.keySet()) {
                varKeys.add((String) key);
            }
        }
        if (sysEnv != null) {
            for (Object key : sysEnv.keySet()) {
                varKeys.add((String) key);
            }
        }
        return varKeys.toArray(new String[varKeys.size()]);
    }

    @Override
    public boolean hasVar(String key) {
        return sysEnv.containsKey(key) || sysProp.containsKey(key);
    }
}