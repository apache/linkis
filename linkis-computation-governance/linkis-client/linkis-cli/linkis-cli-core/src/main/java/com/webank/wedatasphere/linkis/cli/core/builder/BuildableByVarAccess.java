/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cli.core.builder;

import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.BuilderException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import com.webank.wedatasphere.linkis.cli.core.interactor.var.VarAccess;

import java.lang.reflect.ParameterizedType;

/**
 * @description: Builders that need to access user input or configuration
 */
public abstract class BuildableByVarAccess<T> extends AbstractBuilder<T> {
    protected VarAccess stdVarAccess;
    protected VarAccess sysVarAccess;

    protected void checkInit() {
        if (stdVarAccess == null || sysVarAccess == null) {
            ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
            Class<T> clazz = (Class<T>) pt.getActualTypeArguments()[0];
            throw new BuilderException("BLD0003", ErrorLevel.ERROR, CommonErrMsg.BuilderInitErr, "Cannot init builder: " + clazz.getCanonicalName()
                    + "Cause: stdVarAccess or sysVarAccess is null");
        }
        stdVarAccess.checkInit();
        sysVarAccess.checkInit();
    }

    public BuildableByVarAccess<T> setStdVarAccess(VarAccess varAccess) {
        this.stdVarAccess = varAccess;
        return this;
    }

    public BuildableByVarAccess<T> setSysVarAccess(VarAccess varAccess) {
        this.sysVarAccess = varAccess;
        return this;
    }

}