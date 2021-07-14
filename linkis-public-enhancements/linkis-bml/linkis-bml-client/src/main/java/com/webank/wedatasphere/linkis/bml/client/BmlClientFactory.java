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
package com.webank.wedatasphere.linkis.bml.client;

import com.webank.wedatasphere.linkis.bml.client.impl.HttpBmlClient;
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig;

import java.util.Map;

public class BmlClientFactory {

    public static BmlClient createBmlClient(){
        return new HttpBmlClient();
    }

    public static BmlClient createBmlClient(String serverUrl){
        return new HttpBmlClient(serverUrl);
    }

    public static BmlClient createBmlClient(DWSClientConfig clientConfig){
        return new HttpBmlClient(clientConfig);
    }

    public static BmlClient createBmlClient(String serverUrl, Map<String, Object> properties){
        return new HttpBmlClient(serverUrl, properties);
    }

}
