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

import com.webank.wedatasphere.linkis.engine.log.SendAppender;
import com.webank.wedatasphere.linkis.scheduler.listener.LogListener;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;

/**
 * created by enjoyyin on 2018/10/29
 * Description: Test the effectiveness of SendAppender in log4j2 environment(测试SendAppender 在 log4j2环境下的有效性)
 */
public class SendAppenderTest {


    private static final Logger logger = LoggerFactory.getLogger(SendAppenderTest.class);

    @Before
    public void init(){
        LogListener logListener = new LogListener() {
            @Override
            public void onLogUpdate(Job job, String log) {
                System.out.println("log===> " + log);
            }
        };
        SendAppender.setLogListener(logListener);
    }

    @Test
    public void testAppend()throws Exception{

        logger.info("I am a logger");
        logger.info("I am a logger1");
        logger.info("I am a logger2");
        logger.info("I am a logger3");
        logger.info("I am a logger4");
        logger.info("I am a logger5");
        logger.info("I am a logger6");
        logger.info("I am a logger7");
        try{
            new FileInputStream("sss");
        }catch(Exception e){
            logger.error("this is an error", e);
        }
        Thread.sleep(3000);
    }
}
