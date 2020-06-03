package com.webank.wedatasphere.linkis.cs.persistence.conf;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;

/**
 * Created by patinousward on 2020/2/13.
 */
public class PersistenceConf {

    public static final CommonVars<String> TUNING_CLASS = CommonVars.apply("wds.linkis.cs.ha.class", "com.webank.wedatasphere.linkis.cs.highavailable.DefaultContextHAManager");
    //public static final CommonVars<String> TUNING_CLASS = CommonVars.apply("wds.linkis.cs.ha.class","com.webank.wedatasphere.linkis.cs.persistence.ProxyMethodA");

    public static final CommonVars<String> TUNING_METHOD = CommonVars.apply("wds.linkis.cs.ha.proxymethod", "getContextHAProxy");
}
