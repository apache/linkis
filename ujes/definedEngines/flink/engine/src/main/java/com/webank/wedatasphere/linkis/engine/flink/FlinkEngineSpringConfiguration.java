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

/**
 * @program: linkis
 * @description:
 * @author: hui zhu
 * @create: 2020-08-04 14:41
 */

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
