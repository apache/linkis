/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.configuration.util;

import com.webank.wedatasphere.linkis.common.conf.CommonVars$;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineTypeLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.UserCreatorLabel;

import java.util.ArrayList;

public class ConfigurationConfiguration {

    public static final ArrayList<Label> PERMIT_LABEL_TYPE = new ArrayList<>();

    public static final String COPYKEYTOKEN = CommonVars$.MODULE$.apply("wds.linkis.configuration.copykey.token","e8724-e").getValue();

    static{
        PERMIT_LABEL_TYPE.add(new UserCreatorLabel());
        PERMIT_LABEL_TYPE.add(new EngineTypeLabel());
    }
}
