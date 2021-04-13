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
package com.webank.wedatasphere.linkis.engine.flink;

import com.webank.wedatasphere.linkis.common.utils.Logging;
import com.webank.wedatasphere.linkis.engine.execute.CodeParser;
import com.webank.wedatasphere.linkis.engine.execute.EngineHook;
import com.webank.wedatasphere.linkis.engine.execute.SQLCodeParser;
import com.webank.wedatasphere.linkis.engine.execute.hook.MaxExecuteNumEngineHook;
import com.webank.wedatasphere.linkis.engine.execute.hook.ReleaseEngineHook;
import com.webank.wedatasphere.linkis.engine.flink.codeparser.FlinkCodeParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Array;
@Configuration
public class FlinkEngineSpringConfiguration  {


    @Bean(name="codeParser")
    CodeParser generateCodeParser() {
        return  new FlinkCodeParser();
    }

    @Bean(name="engineHooks")
    EngineHook[] generateEngineHooks()  {
        return new EngineHook[]{new ReleaseEngineHook(), new MaxExecuteNumEngineHook()};
    }
}
