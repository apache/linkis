package com.webank.wedatasphere.linkis.engine.io.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by johnnwang on 2018/11/8.
 */
public class ReflectionUtils {
    public static Object invoke(Object object, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(object, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
