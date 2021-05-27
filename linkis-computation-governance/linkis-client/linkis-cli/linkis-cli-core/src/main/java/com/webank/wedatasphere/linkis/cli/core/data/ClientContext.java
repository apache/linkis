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

package com.webank.wedatasphere.linkis.cli.core.data;

import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: linkis-cli
 * @description: store some state information
 * @author: shangda
 * @create: 2021/03/14 18:25
 */
public class ClientContext {
    private static final Map<String, CmdTemplate> generatedTemplateMap = new HashMap<>();

    public static Map<String, CmdTemplate> getGeneratedTemplateMap() {
        return generatedTemplateMap;
    }

}