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

/**
 * author: enjoyyin
 * date: 2018/9/10
 * time: 19:15
 * Description:
 */
package com.webank.wedatasphere.linkis.engine.log;

import com.webank.wedatasphere.linkis.rpc.Sender;
import com.webank.wedatasphere.linkis.scheduler.listener.LogListener;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;


public class RPCAppender extends AppenderSkeleton {

    private static Sender sender;

    private static LogListener logListener;

    private StringBuilder stringBuilder;

    //private static final String SEPARATOR = "*/*/*";

    public RPCAppender(){
        stringBuilder = new StringBuilder();
    }

    public static void setLogListener(LogListener logListener1){
        logListener = logListener1;
    }

    public static LogListener getLogListener(){
        return logListener;
    }

    @Override
    protected void append(LoggingEvent event) {
//        if (logListener == null){
//            stringBuilder.append(event.getMessage().toString());
//           //stringBuilder.append(SEPARATOR);
//        }else{
//            if (stringBuilder != null){
//                //Message message = new Message();
//                logListener.onLogUpdate((Job) null, stringBuilder.toString());
//                stringBuilder = null;
//                logListener.onLogUpdate((Job) null, event.getMessage().toString());
//            }
//        }
    }
    @Override
    public void close() {

    }
    @Override
    public boolean requiresLayout() {
        return false;
    }

}
