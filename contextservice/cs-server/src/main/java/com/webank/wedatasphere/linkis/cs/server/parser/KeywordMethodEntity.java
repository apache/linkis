package com.webank.wedatasphere.linkis.cs.server.parser;

import java.lang.reflect.Method;

/**
 * @author peacewong
 * @date 2020/2/9 16:41
 */
public class KeywordMethodEntity {

    private Method method;

    private String splitter;

    private String regex;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getSplitter() {
        return splitter;
    }

    public void setSplitter(String splitter) {
        this.splitter = splitter;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return method.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
