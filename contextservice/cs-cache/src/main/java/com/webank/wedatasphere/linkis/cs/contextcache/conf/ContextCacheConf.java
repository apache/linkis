package com.webank.wedatasphere.linkis.cs.contextcache.conf;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;

/**
 * @author peacewong
 * @date 2020/2/9 17:04
 */
public class ContextCacheConf {

    public final static String KEYWORD_SCAN_PACKAGE = CommonVars.apply("wds.linkis.cs.keyword.scan.package","com.webank.wedatasphere.linkis.cs").getValue();
    public final static String KEYWORD_SPLIT = CommonVars.apply("wds.linkis.cs.keyword.split",",").getValue();

}
