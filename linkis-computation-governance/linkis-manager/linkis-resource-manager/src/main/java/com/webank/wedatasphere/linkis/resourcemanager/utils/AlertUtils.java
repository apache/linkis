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

package com.webank.wedatasphere.linkis.resourcemanager.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.tools.ims.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        Alert.register(RMConfiguration.ALERT_SUB_SYSTEM_ID().getValue(), RMConfiguration.ALERT_IMS_URL().getValue(), RMConfiguration.ALERT_DEFAULT_UM().getValue());
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

                    Set<AlertWay> way = new HashSet<>();
                    way.add(Email$.MODULE$);
                    AlertAction action =  new AlertAction(RMConfiguration.ALERT_SUB_SYSTEM_ID().getValue(), parsedTitle, "DSS", parsedInfo, MINOR$.MODULE$, ip, 0, way, new HashSet<>());
                    Alert.addAlert(action);
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
