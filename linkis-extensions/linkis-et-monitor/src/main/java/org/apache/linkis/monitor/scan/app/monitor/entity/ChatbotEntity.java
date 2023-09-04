/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.monitor.scan.app.monitor.entity;

import org.apache.linkis.monitor.scan.app.monitor.config.MonitorConfig;

public class ChatbotEntity {
    private String keyid;
    private String content;
    private String type;
    private String userName;
    private String serviceName;

    public ChatbotEntity(String content, String userName) {
        this.keyid = MonitorConfig.CHATBOT_KEY_ID.getValue();
        this.content = content;
        this.type = MonitorConfig.CHATBOT_TYPE.getValue();
        this.userName = userName;
        this.serviceName = MonitorConfig.CHATBOT_SERVICE_NAME.getValue();
    }

    public String getKeyid() {
        return keyid;
    }

    public void setKeyid(String keyid) {
        this.keyid = keyid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceNameuserName) {
        this.serviceName = serviceNameuserName;
    }

    @Override
    public String toString() {
        return "ChatbotEntity{" +
                "keyid='" + keyid + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", userName='" + userName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}
