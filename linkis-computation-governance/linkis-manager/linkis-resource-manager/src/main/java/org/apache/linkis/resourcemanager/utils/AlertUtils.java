/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.resourcemanager.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.apache.linkis.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AlertUtils {

    private static Logger logger = LoggerFactory.getLogger(AlertUtils.class);
    private static Cache<String, String> titleCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite((Long) RMConfiguration.ALERT_DUPLICATION_INTERVAL().getValue(), TimeUnit.SECONDS)
                    .maximumSize(1000)
                    .build();
    private static Map<String, String> alertContact = Maps.newHashMap();



    static {
        String[] contactMappings = RMConfiguration.ALERT_CONTACT_GROUP().getValue().split(",");
        for(String contactMapping : contactMappings){
            String[] queueToGroup = contactMapping.split("/");
            alertContact.put(queueToGroup[0], queueToGroup[1]);
        }
    }

    public static void sendAlertAsync(String title, String info){

        Utils.defaultScheduler().submit(new Runnable() {
            @Override
            public void run() {

                String parsedTitle = null;
                String parsedInfo = null;
                try {
                    parsedTitle = URLEncoder.encode(title, "utf-8");
                    parsedInfo = URLEncoder.encode(info, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error("failed to parse alert information", e);
                }


                if(titleCache.getIfPresent(parsedTitle) == null){
                    String ip = "";

                    try {
                        InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        logger.error("failed to get local host address", e);
                    }

                    titleCache.put(parsedTitle, parsedInfo);
                }
            }
        });

    }

    public static String getContactByQueue(String queue){
        for(Map.Entry<String, String> contactEntry : alertContact.entrySet()){
            if(queue.startsWith(contactEntry.getKey())){
                return contactEntry.getValue();
            }
        }
        return RMConfiguration.ALERT_DEFAULT_CONTACT().getValue();
    }

}
