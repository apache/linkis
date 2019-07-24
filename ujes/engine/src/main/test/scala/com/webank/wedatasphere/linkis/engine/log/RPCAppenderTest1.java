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

package scala.com.webank.wedatasphere.linkis.engine.log;

import com.webank.wedatasphere.linkis.engine.log.RPCAppender;
import com.webank.wedatasphere.linkis.scheduler.listener.LogListener;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

/**
 * created by enjoyyin on 2018/11/5
 * Description:
 */
public class RPCAppenderTest1 {
    private static final Logger logger = Logger.getLogger(RPCAppenderTest1.class);

    @Before
    public void init(){
        LogListener logListener = new LogListener() {
            @Override
            public void onLogUpdate(Job job, String log) {
                System.out.println("log===> " + log);
            }
        };
        RPCAppender.setLogListener(logListener);
        System.out.println("INIT");
    }

    @Test
    public void testAppend(){
        logger.info("I am a appender");
        logger.info("this is very interesting");
        logger.error("this is very wrong");
    }

}
