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

package com.webank.wedatasphere.linkis.engine.log;

import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration;
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration$;
import com.webank.wedatasphere.linkis.scheduler.listener.LogListener;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * created by enjoyyin on 2018/11/6
 * Description:
 */
@Plugin(name = "Send", category = "Core", elementType = "appender", printObject = true)
public class SendAppender extends AbstractAppender {

    /**
     * @fields serialVersionUID
     */
    private static final long serialVersionUID = -830237775522429777L;
    private static LogListener logListener;
    private LogCache logCache;
    private static final Logger logger = LoggerFactory.getLogger(SendAppender.class);

    class SendThread implements Runnable{
        @Override
        public void run() {
           if (logListener == null){
                //ignore
           }else{
               if (logCache == null){
                   logger.warn("logCache is null");
                   return;
               }
               List<String> logs = logCache.getRemain();
               if (logs.size() > 0){
                   StringBuilder sb = new StringBuilder();
                   for(String log : logs){
                       sb.append(log);
                   }
                   logListener.onLogUpdate(null, sb.toString());
               }
           }
        }
    }


    public SendAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout,
                        final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        //todo enjoyyin 500 to be made configurable ide number（500要做成可配置ide数字）
        //this.logCache = new MountLogCache((Integer) EngineConfiguration.ENGINE_LOG_CACHE_NUM().getValue());
        this.logCache = LogHelper.logCache();
        SendThread thread = new SendThread();
        Utils.defaultScheduler().scheduleAtFixedRate(thread, 10, (Integer)EngineConfiguration$.MODULE$.ENGINE_LOG_SEND_TIME_INTERVAL().getValue(), TimeUnit.MILLISECONDS);
    }

    public static void setLogListener(LogListener ll){
        logListener = ll;
    }

//    public static void setLogCache(LogCache lc){
//        logCache = lc;
//    }

    @Override
    public void append(LogEvent event) {
        if (logListener == null) {
            return;
        }
        logCache.cacheLog(new String(getLayout().toByteArray(event)));
    }

    @PluginFactory
    public static SendAppender createAppender(@PluginAttribute("name") String name,
                                              @PluginElement("Filter") final Filter filter,
                                              @PluginElement("Layout") Layout<? extends Serializable> layout,
                                              @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        if (name == null) {
            LOGGER.error("No name provided for SendAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new SendAppender(name, filter, layout, ignoreExceptions);
    }

}