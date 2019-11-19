package com.webank.wedatasphere.linkis.bml.client;

import com.webank.wedatasphere.linkis.bml.client.impl.HttpBmlClient;

import java.util.Map;

/**
 * created by cooperyang on 2019/5/15
 * Description:
 */
public class BmlClientFactory {
    public static BmlClient createBmlClient(){
        return createBmlClient(null, null);
    }


    public static BmlClient createBmlClient(String user){
        return createBmlClient(user, null);
    }


    public static BmlClient createBmlClient(String user, Map<String, Object> properties){
        AbstractBmlClient bmlClient = new HttpBmlClient();
        bmlClient.setUser(user);
        bmlClient.setProperties(properties);
        return bmlClient;
    }


}
