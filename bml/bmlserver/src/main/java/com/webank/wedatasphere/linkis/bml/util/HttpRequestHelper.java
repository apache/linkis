package com.webank.wedatasphere.linkis.bml.util;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * created by cooperyang on 2019/5/23
 * Description:
 */
public class HttpRequestHelper {

    private static final String UN_KNOWN = "unKnown";

    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(StringUtils.isNotEmpty(ip) && !UN_KNOWN.equalsIgnoreCase(ip)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if(index != -1){
                return ip.substring(0,index);
            }else{
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if(StringUtils.isNotEmpty(ip) && !UN_KNOWN.equalsIgnoreCase(ip)){
            return ip;
        }
        return request.getRemoteAddr();
    }
}
