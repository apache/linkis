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
package org.apache.linkis.manager.am.util;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import org.apache.linkis.manager.am.restful.EMRestfulApi;
import org.apache.linkis.manager.am.vo.ResourceVo;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ECResourceInfoUtils {

    private static Logger logger = LoggerFactory.getLogger(ECResourceInfoUtils.class);

    public static String NAME_REGEX = "^[a-zA-Z\\d_\\.]+$";

    public static boolean checkNameValid(String creator) {
        return Pattern.compile(NAME_REGEX).matcher(creator).find();
    }

    public static String strCheckAndDef(String str, String def) {
        return StringUtils.isBlank(str) ? def : str;
    }

    public static ResourceVo getStringToMap(String str, ECResourceInfoRecord info) {
        ResourceVo resourceVo = null;
        Map<String, Object> map = new Gson().fromJson(str, new HashMap<>().getClass());
        if (MapUtils.isNotEmpty(map)) {
            resourceVo = new ResourceVo();
            if (info.getLabelValue().contains("spark") || (info.getLabelValue().contains("flink"))) {
                if (null != map.get("driver")) {
                    Map<String, Object> divermap = MapUtils.getMap(map, "driver");
                    resourceVo.setInstance(((Double) divermap.get("instance")).intValue());
                    resourceVo.setCores(((Double) divermap.get("cpu")).intValue());
                    resourceVo.setMemory(memorySizeChange(divermap.get("memory").toString()));
                    return resourceVo;
                } else {
                    return null;//兼容老数据
                }
            }
            resourceVo.setInstance(((Double) map.get("instance")).intValue());
            resourceVo.setMemory(memorySizeChange(map.get("memory").toString()));
            Double core = null == map.get("cpu") ? (Double) map.get("cores") :(Double) map.get("cpu");
            resourceVo.setCores(core.intValue());
        }
        return resourceVo;
    }

    public static long memorySizeChange(String memory) {
        if (memory.contains("GB")) {
            String strip = StringUtils.strip(memory, " GB");
            BigDecimal bigDecimal = new BigDecimal(strip);
            BigDecimal gb = new BigDecimal("1073741824");//1024^3
            BigDecimal multiply = bigDecimal.multiply(gb);
            return multiply.longValue();
        }
        if (memory.contains("MB")) {
            String strip = StringUtils.strip(memory, " MB");
            BigDecimal bigDecimal = new BigDecimal(strip);
            BigDecimal gb = new BigDecimal("1048576");//1024^2
            BigDecimal multiply = bigDecimal.multiply(gb);
            return multiply.longValue();
        }
        if (memory.contains("KB")) {
            String strip = StringUtils.strip(memory, " KB");
            BigDecimal bigDecimal = new BigDecimal(strip);
            BigDecimal gb = new BigDecimal("1024");//1024^1
            BigDecimal multiply = bigDecimal.multiply(gb);
            return multiply.longValue();
        }
        BigDecimal gb = new BigDecimal(memory);
        return gb.longValue();
    }
}
