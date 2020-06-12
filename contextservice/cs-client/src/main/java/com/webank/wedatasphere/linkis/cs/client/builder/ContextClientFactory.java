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
package com.webank.wedatasphere.linkis.cs.client.builder;

import com.webank.wedatasphere.linkis.cs.client.ContextClient;
import com.webank.wedatasphere.linkis.cs.client.http.HttpContextClient;

/**
 * created by cooperyang on 2020/2/10
 * Description:
 */
public class ContextClientFactory {


    private static final ContextClientConfig DEFAULT_CONTEXT_CLIENT_CONFIG;

    static{
        DEFAULT_CONTEXT_CLIENT_CONFIG = new HttpContextClientConfig();
    }

    public static ContextClient getOrCreateContextClient(){
        return getOrCreateContextClient(DEFAULT_CONTEXT_CLIENT_CONFIG);
    }

    public static ContextClient getOrCreateContextClient(ContextClientConfig contextClientConfig){
        return HttpContextClient.getInstance(contextClientConfig);
    }



}
