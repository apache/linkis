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
package com.webank.wedatasphere.linkis.enginemanager.flink.process;

import com.webank.wedatasphere.linkis.common.conf.DWCArgumentsParser;
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.enginemanager.process.CommonProcessEngine;
import com.webank.wedatasphere.linkis.enginemanager.process.JavaProcessEngineBuilder;
import com.webank.wedatasphere.linkis.enginemanager.process.ProcessEngineBuilder;
import org.apache.commons.lang.StringUtils;


/**
 *
 * Created by liangqilang on 01 20, 2021
 *
 */
public class FlinkCommonProcessEngine extends CommonProcessEngine {



    public FlinkCommonProcessEngine(ProcessEngineBuilder processBuilder, DWCArgumentsParser dwcArgumentsParser, long timeout) {
        super(processBuilder, dwcArgumentsParser, timeout);
    }

    public FlinkCommonProcessEngine(ProcessEngineBuilder processBuilder, DWCArgumentsParser dwcArgumentsParser) {
        super(processBuilder, dwcArgumentsParser);
    }

}
