/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cli.application.interactor.var;

import org.apache.linkis.cli.application.entity.command.ParamItem;
import org.apache.linkis.cli.application.entity.command.Params;
import org.apache.linkis.cli.application.entity.var.VarAccess;
import org.apache.linkis.cli.application.exception.VarAccessException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.command.SpecialMap;
import org.apache.linkis.cli.application.interactor.command.template.converter.AbstractStringConverter;
import org.apache.linkis.cli.application.interactor.command.template.converter.PredefinedStringConverters;
import org.apache.linkis.cli.application.interactor.properties.ClientProperties;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VarAccessImpl implements VarAccess {
  private static Logger logger = LoggerFactory.getLogger(VarAccessImpl.class);
  private Params cmdParams;
  private ClientProperties userConf;
  private ClientProperties defaultConf;
  private Map<String, String> subMapCache;

  public VarAccessImpl setCmdParams(Params cmdParams) {
    this.cmdParams = cmdParams;
    return this;
  }

  public Params getSubParam(String identifier) {
    return this.cmdParams;
  }

  public VarAccessImpl setUserConf(ClientProperties userConf) {
    this.userConf = userConf;
    return this;
  }

  public ClientProperties getUserConf(String identifier) {
    return this.userConf;
  }

  public VarAccessImpl setDefaultConf(ClientProperties defaultConf) {
    this.defaultConf = defaultConf;
    return this;
  }

  public ClientProperties getDefaultConf(String identifier) {
    return this.defaultConf;
  }

  public VarAccessImpl init() {
    this.subMapCache = new HashMap<>();
    putSubMapCache(subMapCache, cmdParams);
    return this;
  }

  private void putSubMapCache(Map<String, String> subMapCache, Params param) {
    for (ParamItem item : param.getParamItemMap().values()) {
      // scan through all map type value and try get value for key
      if (item.getValue() != null
          && item.hasVal()
          && item.getValue() instanceof Map
          && !(item.getValue() instanceof SpecialMap)) {
        try {
          Map<String, String> subMap = (Map<String, String>) item.getValue();
          for (Map.Entry<String, String> entry : subMap.entrySet()) {
            if (subMapCache.containsKey(item.getKey())) {
              logger.warn(
                  "Value of duplicated key \"{}\" in subMap \"{}\" will be ignored.",
                  item.getKey(),
                  item.getKey());
            } else if (StringUtils.isNotBlank(entry.getKey())
                && StringUtils.isNotBlank(entry.getValue())) {
              subMapCache.put(entry.getKey(), entry.getValue());
            }
          }
        } catch (ClassCastException e) {
          logger.warn(
              "Param: {} has an unsupported Map type(not Map<String, String>). It wiil be ignored",
              item.getKey());
        }
      }
    }
  }

  public void checkInit() {
    if (this.cmdParams == null || this.defaultConf == null || this.subMapCache == null) {
      throw new VarAccessException(
          "VA0002",
          ErrorLevel.ERROR,
          CommonErrMsg.VarAccessInitErr,
          "stdVarAccess is not inited. "
              + "cmdParams: "
              + cmdParams
              + "defaultConf: "
              + defaultConf
              + "subMapCache: "
              + subMapCache);
    }
  }

  @Override
  public <T> T getVarOrDefault(Class<T> clazz, String key, T defaultValue) {

    if (StringUtils.isBlank(key)) {
      return null;
    }

    T val = getVar(clazz, key);

    return val != null ? val : defaultValue;
  }

  @Override
  public <T> T getVar(Class<T> clazz, String key) {
    checkInit();
    if (key == null || StringUtils.isBlank(key)) {
      return null;
    }
    T p1 = getVarFromParam(clazz, key, cmdParams);

    T pd1 = getDefaultVarFromParam(clazz, key, cmdParams);

    T c1 = getVarFromCfg(clazz, key, userConf);
    T c2 = getVarFromCfg(clazz, key, defaultConf);

    return p1 != null ? p1 : c1 != null ? c1 : c2 != null ? c2 : pd1;
  }

  private <T> T getVarFromParam(Class<T> clazz, String key, Params params) {
    if (params == null || StringUtils.isBlank(key)) {
      return null;
    }

    Object v1 =
        params.getParamItemMap().containsKey(key) && params.getParamItemMap().get(key).hasVal()
            ? setNullIfEmpty(params.getParamItemMap().get(key).getValue())
            : null;

    Object v2 = setNullIfEmpty(convertStringVal(clazz, subMapCache.getOrDefault(key, null)));

    // extraParam has lower priority
    Object v3 =
        params.getExtraProperties() == null
            ? null
            : setNullIfEmpty(params.getExtraProperties().getOrDefault(key, null));

    Object retObj = v1 != null ? v1 : v2 != null ? v2 : v3;

    return clazz.cast(retObj);
  }

  private boolean paramHasVar(String key, Params params) {
    boolean b1 =
        params.getParamItemMap().containsKey(key) && params.getParamItemMap().get(key).hasVal();
    boolean b2 = subMapCache.containsKey(key);
    boolean b3 = params.getExtraProperties().containsKey(key);
    return b1 || b2 || b3;
  }

  private <T> T getDefaultVarFromParam(Class<T> clazz, String key, Params params) {
    if (params == null || StringUtils.isBlank(key) || !params.getParamItemMap().containsKey(key)) {
      return null;
    }

    Object vd = setNullIfEmpty(params.getParamItemMap().get(key).getDefaultValue());

    return clazz.cast(vd);
  }

  private <T> T getVarFromCfg(Class<T> clazz, String key, ClientProperties conf) {

    if (conf == null) {
      return null;
    }
    Object val = conf.get(key);
    if (val == null) {
      return null;
    }
    String strVal;
    try {
      strVal = (String) val;
    } catch (ClassCastException e) {
      throw new VarAccessException(
          "VA0003",
          ErrorLevel.ERROR,
          CommonErrMsg.VarAccessErr,
          "Cannot getVar \"" + key + "\" from config. Cause: value is not String");
    }

    return convertStringVal(clazz, strVal);
  }

  private Object setNullIfEmpty(Object obj) {
    Object ret;
    if (obj instanceof String && StringUtils.isBlank((String) obj)) {
      ret = null;
    } else if (obj instanceof Map && ((Map<?, ?>) obj).size() == 0) {
      ret = null;
    } else if (obj instanceof Collections && ((Collection<?>) obj).size() == 0) {
      ret = null;
    } else {
      ret = obj;
    }
    return ret;
  }

  private boolean cfgHasVar(String key, ClientProperties conf) {
    return conf == null ? false : conf.containsKey(key);
  }

  private <T> T convertStringVal(Class<T> clazz, String strVal) {
    Object ret;
    if (StringUtils.isBlank(strVal)) {
      return null;
    }
    if (clazz == Object.class) {
      ret = strVal;
    } else if (clazz == String.class) {
      ret = convertGivenConverter(strVal, PredefinedStringConverters.NO_CONVERTER);
    } else if (clazz == Integer.class) {
      ret = convertGivenConverter(strVal, PredefinedStringConverters.INT_CONVERTER);
    } else if (clazz == Long.class) {
      ret = convertGivenConverter(strVal, PredefinedStringConverters.LONG_CONVERTER);
    } else if (clazz == Boolean.class) {
      ret = convertGivenConverter(strVal, PredefinedStringConverters.BOOLEAN_CONVERTER);
    } else if (Map.class.isAssignableFrom(clazz)) {
      // TODO: throw or return null if not string map
      ret = null;
      //          convertGivenConverter(strVal,
      // PredefinedStringConverters.STRING_MAP_CONVERTER);
    } else if (clazz == String[].class) {
      ret = null;
      //      ret = convertGivenConverter(strVal,
      // PredefinedStringConverters.STR_ARRAY_CONVERTER);
    } else {
      throw new VarAccessException(
          "VA0004",
          ErrorLevel.ERROR,
          CommonErrMsg.VarAccessErr,
          "Cannot convertStringVal \""
              + strVal
              + "\" to "
              + clazz.getCanonicalName()
              + ": designated type is not supported");
    }
    return clazz.cast(ret);
  }

  private <T> T convertGivenConverter(String strVal, AbstractStringConverter<T> converter) {
    return converter.convert(strVal);
  }

  @Override
  public String[] getAllVarKeys() {
    List<String> varKeys = new ArrayList<>();

    addParamVarKeys(varKeys, cmdParams);

    addPropsVarKeys(varKeys, userConf);
    addPropsVarKeys(varKeys, defaultConf);

    return varKeys.toArray(new String[varKeys.size()]);
  }

  private void addParamVarKeys(List<String> varKeys, Params param) {
    if (param != null) {
      for (String key : param.getParamItemMap().keySet()) {
        if (!varKeys.contains(key)) {
          varKeys.add(key);
        }
      }
      for (String key : subMapCache.keySet()) {
        // scan through all map type value and try add key
        if (!varKeys.contains(key)) {
          varKeys.add(key);
        }
      }
      for (String key : param.getExtraProperties().keySet()) {
        if (!varKeys.contains(key)) {
          varKeys.add(key);
        }
      }
    }
  }

  private void addPropsVarKeys(List<String> varKeys, ClientProperties props) {
    if (props != null) {
      for (Object key : props.keySet()) {
        if (!varKeys.contains(key)) {
          varKeys.add((String) key);
        }
      }
    }
  }

  @Override
  public boolean hasVar(String key) {
    boolean b1 = paramHasVar(key, cmdParams);
    boolean b2 = cfgHasVar(key, userConf);
    boolean b3 = cfgHasVar(key, defaultConf);
    return b1 || b2 || b3;
  }
}
