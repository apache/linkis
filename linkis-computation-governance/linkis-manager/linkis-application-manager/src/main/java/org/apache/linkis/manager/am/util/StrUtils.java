package org.apache.linkis.manager.am.util;

import org.springframework.util.StringUtils;

public class StrUtils {

    public static String strCheckAndDef(String str,String def){
        return StringUtils.isEmpty(str)?def:str;
    }

}
