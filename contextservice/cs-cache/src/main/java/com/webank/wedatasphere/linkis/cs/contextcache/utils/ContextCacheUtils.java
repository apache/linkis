package com.webank.wedatasphere.linkis.cs.contextcache.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author peacewong
 * @date 2020/2/9 17:31
 */
public class ContextCacheUtils {

    public static Set<String> getString(String s, String regex) {

        Set<String> keywords = new HashSet<>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        while(m.find()) {
            keywords.add(m.group());

        }
        return keywords;
    }
}
