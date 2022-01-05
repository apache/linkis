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
 
package org.apache.linkis.manager.label.entity.cluster;

import static org.apache.linkis.manager.label.constant.LabelConstant.LABEL_BUILDER_ERROR_CODE;
import static org.apache.linkis.manager.label.constant.LabelKeyConstant.ENV_TYPE_KEY;

import org.apache.linkis.manager.label.entity.Feature;
import org.apache.linkis.manager.label.entity.GenericLabel;
import org.apache.linkis.manager.label.exception.LabelRuntimeException;
import java.util.HashMap;


public class EnvLabel extends GenericLabel {

    public static final String DEV = "dev";
    public static final String TEST = "test";
    public static final String PROD = "prod";

    public EnvLabel() {
        setLabelKey(ENV_TYPE_KEY);
    }

    @Override
    public Feature getFeature() {
        return Feature.CORE;
    }

    public void setEnvType(String envType) {
        if(!envType.equals(DEV) && !envType.equals(TEST) && !envType.equals(PROD)) {
            throw new LabelRuntimeException(LABEL_BUILDER_ERROR_CODE, "Not support envType: " + envType);
        }
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put(ENV_TYPE_KEY, envType);
    }

    public String getEnvType() {
        if (getValue() != null && null != getValue().get(ENV_TYPE_KEY)) {
            return getValue().get(ENV_TYPE_KEY);
        }
        return null;
    }
}
