/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.datax.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * @author davidhua
 * 2019/10/4
 */
public class GsonUtil {
    private static Gson gson;
    static{
        GsonBuilder builder = new GsonBuilder();
        gson = builder.enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();
    }

    /**
     * use gson.fromJson(json, type) simplify
     * @param json json string
     * @param clazz type
     * @param <T> actual need type
     * @return deserialized object
     */
    public static <T>T fromJson(String json, Class<?> clazz ){
        if(json.startsWith("[") && json.endsWith("]")){
            return gson.fromJson(json, TypeToken.getParameterized(List.class, clazz).getType());
        }
        return gson.fromJson(json, TypeToken.getParameterized(clazz).getType());
    }

    /**
     * use gson.fromJson(json, type) simplify
     * @param json json string
     * @param rawClass raw class
     * @param genericArguments generic arguments
     * @param <T>
     * @return
     */
    public static <T>T fromJson(String json, Class<?> rawClass, Class<?>... genericArguments){
        return gson.fromJson(json, TypeToken.getParameterized(rawClass, genericArguments).getType());
    }

    /**
     * use gson.toJson(src) simplify
     * @param src source obj
     * @return json
     */
    public static String toJson(Object src){
        return gson.toJson(src);
    }
}
