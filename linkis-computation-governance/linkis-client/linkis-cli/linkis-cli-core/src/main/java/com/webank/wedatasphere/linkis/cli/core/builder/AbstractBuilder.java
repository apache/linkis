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


/**
 * @program: linkis-cli
 * @description:
 * @author: shangda
 * @create: 2021/03/12 16:54
 */
public abstract class AbstractBuilder<T> implements Builder<T> {
    protected T targetObj;

    public AbstractBuilder() {
        reset();
    }

    protected void reset() {
        targetObj = getTargetNewInstance();
    }

    @Override
    public T build() {
        T ret = targetObj;
        reset();
        return ret;
    }

    protected abstract T getTargetNewInstance();

//    protected T getTargetNewInstance() {
//        try {
//            // 通过反射获取model的真实类型
//            ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
//            Class<T> clazz = (Class<T>) pt.getActualTypeArguments()[0];
//            // 通过反射创建model的实例
//            targetObj = clazz.newInstance();
//        } catch (InstantiationException e) {
//            throw new BuilderException("BLD0001", ErrorLevel.ERROR, CommonErrMsg.BuilderBuildErr, "Cannot generate new instance.", e);
//        } catch (IllegalAccessException ie) {
//            throw new BuilderException("BLD0001", ErrorLevel.ERROR, CommonErrMsg.BuilderBuildErr, "Cannot generate new instance.", ie);
//        }
//        return targetObj;
//    }
}