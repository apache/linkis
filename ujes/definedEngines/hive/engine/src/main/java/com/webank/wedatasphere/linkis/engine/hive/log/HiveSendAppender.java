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

package com.webank.wedatasphere.linkis.engine.hive.log;

import com.webank.wedatasphere.linkis.engine.hive.progress.HiveProgressHelper;
import com.webank.wedatasphere.linkis.engine.log.SendAppender;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 * created by cooperyang on 2019/2/11
 * Description:
 */
@Plugin(name = "HiveSend", category = "Core", elementType = "appender", printObject = true)
public class HiveSendAppender extends SendAppender{

    private static final Logger logger = LoggerFactory.getLogger(HiveSendAppender.class);

    public HiveSendAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout,
                            final boolean ignoreExceptions){
        super(name, filter, layout, ignoreExceptions);
    }


    @Override
    public void append(LogEvent event){
        //LOGGER.info("HiveSendAppender append");
        String log =  new String(getLayout().toByteArray(event));
        List<HiveProgress> hiveProgresses =  LogHelper.checkPattern(log);
        if (hiveProgresses != null && hiveProgresses.size() > 0){
            HiveProgressHelper.storeHiveProgress(hiveProgresses);
        }
//        if (LogHelper.matchCompletedPattern(log)){
//            //If it is a failure or success, directly set the progress of a single sql to 1.0f(如果是失败或是成功，直接将单条的sql的进度设置为1.0f)
//            //LOGGER.info("on this is ");
//            //HiveProgressHelper.storeSingleSQLProgress(1.0f);
//        }
        String appid = LogHelper.getYarnAppid(log);
        if (StringUtils.isNotEmpty(appid)){
            LOGGER.info("this is appid " + appid);
            HiveProgressHelper.storeAppid(appid);
        }
        super.append(event);
    }


    @PluginFactory
    public static HiveSendAppender createAppender(@PluginAttribute("name") String name,
                                              @PluginElement("Filter") final Filter filter,
                                              @PluginElement("Layout") Layout<? extends Serializable> layout,
                                              @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        if (name == null) {
            LOGGER.error("No name provided for HiveSendAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new HiveSendAppender(name, filter, layout, ignoreExceptions);
    }


}
