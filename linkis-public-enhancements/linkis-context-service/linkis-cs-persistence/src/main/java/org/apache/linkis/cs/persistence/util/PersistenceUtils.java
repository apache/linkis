/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cs.persistence.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.exception.CSWarnException;
import org.apache.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import org.apache.linkis.cs.common.serialize.helper.SerializationHelper;
import org.apache.linkis.cs.persistence.annotation.Ignore;
import org.apache.linkis.cs.persistence.entity.ExtraFieldClass;
import org.apache.linkis.cs.persistence.exception.ThrowingFunction;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public class PersistenceUtils {

    private static ObjectMapper json = BDPJettyServerHelper.jacksonJson();

    private static final Logger logger = LoggerFactory.getLogger(PersistenceUtils.class);

    private static String generateGetMethod(Field field) {
        String fieldName = field.getName();
        return String.format("get%s%s", fieldName.substring(0, 1).toUpperCase(), fieldName.substring(1));
    }

    public static String generateSetMethod(String fieldName) {
        return String.format("set%s%s", fieldName.substring(0, 1).toUpperCase(), fieldName.substring(1));
    }

    private static boolean canIgnore(Field field) {
        return field.getAnnotation(Ignore.class) != null;
    }

    private static List<String> getIgnoreFieldName(Class<?> clazz) {
        if (clazz.getAnnotation(Ignore.class) != null) {
            return Arrays.stream(clazz.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        } else {
            return Arrays.stream(clazz.getDeclaredFields())
                    .filter(PersistenceUtils::canIgnore).map(Field::getName).collect(Collectors.toList());
        }
    }

    public static <T, S> Pair<S, ExtraFieldClass> transfer(T t, Class<S> sClass) throws CSErrorException {
        try {
            ExtraFieldClass extraFieldClass = new ExtraFieldClass();
            S s = sClass.newInstance();
            BeanUtils.copyProperties(t, s);
            Class<?> tClass = t.getClass();
            extraFieldClass.setClassName(tClass.getName());
            List<String> canIgnore = getIgnoreFieldName(sClass);
            for (Field field : tClass.getDeclaredFields()) {
                if (!canIgnore.contains(field.getName())) {
                    Method method = tClass.getMethod(generateGetMethod(field));
                    if (null != method.invoke(t)) {
                        //field.getType().getName() 无法拿到子类的类型
                        Object invoke = method.invoke(t);
                        extraFieldClass.addFieldName(field.getName());
                        if (invoke == null) {
                            extraFieldClass.addFieldType(field.getType().getName());
                        } else {
                            extraFieldClass.addFieldType(invoke.getClass().getName());
                        }
                        extraFieldClass.addFieldValue(invoke);
                    }
                }
            }
            return new Pair<>(s, extraFieldClass);
        } catch (Exception e) {
            throw new CSErrorException(97000, "transfer bean failed:", e);
        }
    }

    public static <T, S> T transfer(ExtraFieldClass extraFieldClass, S s) throws CSErrorException {
        if (s == null) return null;
        try {
            Class<?> tClass = Class.forName(extraFieldClass.getClassName());
            T t = (T) tClass.newInstance();
            BeanUtils.copyProperties(s, t);
            for (int i = 0; i < extraFieldClass.getFieldNames().size(); i++) {
                Field field = tClass.getDeclaredField(extraFieldClass.getOneFieldName(i));
                field.setAccessible(true);
                if (LONG_TYP.equals(extraFieldClass.getOneFieldType(i))) {
                    Long value = new Long(extraFieldClass.getOneFieldValue(i).toString());
                    field.set(t, value);
                } else if (Enum.class.isAssignableFrom(Class.forName(extraFieldClass.getOneFieldType(i)))) {
                    //反序列化支持枚举类
                    Class<?> enumClass = Class.forName(extraFieldClass.getOneFieldType(i));
                    Method valueOf = enumClass.getMethod("valueOf", String.class);
                    Object invoke = valueOf.invoke(null, extraFieldClass.getOneFieldValue(i));
                    field.set(t, invoke);
                } else if (!BeanUtils.isSimpleProperty(Class.forName(extraFieldClass.getOneFieldType(i)))) {
                    //非基本类型的话,使用jackson进行反序列化  // TODO: 2020/3/5 这里属性的序列化and反序列化最好修改为utils的序列化器
                    Object o = json.convertValue(extraFieldClass.getOneFieldValue(i), Class.forName(extraFieldClass.getOneFieldType(i)));
                    field.set(t, o);
                } else {
                    field.set(t, extraFieldClass.getOneFieldValue(i));
                }
            }
            return t;
        } catch (Exception e) {
            throw new CSErrorException(97000, "transfer bean failed:", e);
        }
    }

    public static <T, R, E extends Exception> Function<T, R> map(
            ThrowingFunction<T, R, E> throwingFunction) {
        return i -> {
            try {
                return throwingFunction.accept(i);
            } catch (Exception e) {
                throw new CSWarnException(97000, "execute failed,reason:", e);
            }
        };
    }

    private static final String LONG_TYP = "java.lang.Long";

    // TODO: 2020/5/15  去掉重复的
    public static final SerializationHelper SERIALIZE = ContextSerializationHelper.getInstance();

    public static String serialize(Object o) throws CSErrorException {
        if (o instanceof String) {
            return (String) o;
        }
        return SERIALIZE.serialize(o);
    }

    public static <T> T deserialize(String Str) throws CSErrorException {
        return (T) SERIALIZE.deserialize(Str);
    }

}
