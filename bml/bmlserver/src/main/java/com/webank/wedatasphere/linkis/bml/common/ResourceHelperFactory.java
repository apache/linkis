package com.webank.wedatasphere.linkis.bml.common;

/**
 * created by cooperyang on 2019/5/21
 * Description:
 */
public class ResourceHelperFactory {
    public static ResourceHelper getResourceHelper(){
        return new HdfsResourceHelper();
    }
}
