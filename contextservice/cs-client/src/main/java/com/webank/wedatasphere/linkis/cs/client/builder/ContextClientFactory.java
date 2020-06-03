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
