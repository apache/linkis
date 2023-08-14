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

package org.apache.linkis.cs.common.utils;

import org.apache.linkis.cs.common.entity.source.CommonHAContextID;
import org.apache.linkis.cs.common.entity.source.HAContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.exception.ErrorCode;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSHighAvailableUtils {

  private static final Logger logger = LoggerFactory.getLogger(CSHighAvailableUtils.class);
  private static final String HAID_PART_DELEMETER = "--";
  private static final String HAID_INS_LEN_DELEMETER = "-";
  private static final int HAID_PARTS_NUM = 2;
  private static final Gson gson = new Gson();

  private static final int TWO = 2;

  public static boolean checkHAIDBasicFormat(String haid) {
    if (StringUtils.isBlank(haid)) {
      return false;
    }
    String[] arr = haid.split(HAID_PART_DELEMETER);
    if (null != arr && arr.length == HAID_PARTS_NUM) {
      int insLen = 0;
      String[] lenArr = arr[0].split(HAID_INS_LEN_DELEMETER);
      if (null == lenArr || lenArr.length < TWO) {
        return false;
      }
      try {
        for (String len : lenArr) {
          insLen += Integer.parseInt(len);
        }
      } catch (NumberFormatException e) {
        return false;
      }
      if (insLen < arr[1].length()) {
        String id = arr[1].substring(insLen);
        if (StringUtils.isNumeric(id)) {
          return true;
        }
      }
    }
    return false;
  }

  public static String encodeHAIDKey(
      String contextID, String instance, List<String> backupInstanceList) throws CSErrorException {
    if (!StringUtils.isNumeric(contextID)
        || StringUtils.isBlank(instance)
        || null == backupInstanceList
        || backupInstanceList.isEmpty()) {
      logger.error(
          "Cannot encodeHAIDKey, contextID : "
              + contextID
              + ", instance : "
              + instance
              + ", backupInstanceList : "
              + gson.toJson(backupInstanceList));
      throw new CSErrorException(
          ErrorCode.INVALID_HAID_ENCODE_PARAMS,
          "Cannot encodeHAIDKey, contextID : "
              + contextID
              + ", instance : "
              + instance
              + ", backupInstanceList : "
              + gson.toJson(backupInstanceList));
    }
    StringBuilder idBuilder = new StringBuilder("");
    StringBuilder instBuilder = new StringBuilder("");
    idBuilder.append(instance.length());
    instBuilder.append(instance);
    for (String ins : backupInstanceList) {
      idBuilder.append(HAID_INS_LEN_DELEMETER).append(ins.length());
      instBuilder.append(ins);
    }
    idBuilder.append(HAID_PART_DELEMETER).append(instBuilder).append(contextID);
    return idBuilder.toString();
  }

  public static HAContextID decodeHAID(String haid) throws CSErrorException {
    if (StringUtils.isBlank(haid)) {
      throw new CSErrorException(ErrorCode.INVALID_NULL_STRING, "HAIDKey cannot be empty.");
    }
    if (!checkHAIDBasicFormat(haid)) {
      logger.error("Invalid haid : " + haid);
      throw new CSErrorException(ErrorCode.INVALID_HAID_STRING, "Invalid haid : " + haid);
    }
    String[] partArr = haid.split(HAID_PART_DELEMETER);
    String[] insArr = partArr[0].split(HAID_INS_LEN_DELEMETER);
    String contextID = null;
    List<String> instanceList = new ArrayList<>();
    String insStr = partArr[1];
    try {
      int index = 0, tmp = 0;
      for (String len : insArr) {
        tmp = Integer.parseInt(len);
        instanceList.add(insStr.substring(index, index + tmp));
        index += tmp;
      }
      contextID = insStr.substring(index);
    } catch (NumberFormatException e) {
      logger.error("Invalid haid : " + haid + ", " + e.getMessage());
      throw new CSErrorException(
          ErrorCode.INVALID_HAID_STRING, "Invalid haid : " + haid + ", " + e.getMessage());
    }
    String instance = instanceList.remove(0);
    return new CommonHAContextID(instance, instanceList.get(0), contextID);
  }

  public static void main(String[] args) throws Exception {
    String haid1 = "24--24--YmRwaGRwMTFpZGUwMTo5MTE2YmRwaGRwMTFpZGUwMTo5MTE084835";
    System.out.println(checkHAIDBasicFormat(haid1));
    String id = "8798";
    String instance = "jslfjslfjlsdjfljsdf==+";
    String backupInstance = "sjljsljflsdjflsjd";
    List<String> list = new ArrayList<>();
    list.add(backupInstance);
    list.add(instance);
    String haid2 = encodeHAIDKey(id, instance, list);
    System.out.println(haid2);
    System.out.println(checkHAIDBasicFormat(haid2));
    if (checkHAIDBasicFormat(haid2)) {
      System.out.println(gson.toJson(decodeHAID(haid2)));
    }
    String haid3 = "24-24--YmRwaGRwMTFpZGUwMTo5MTE0YmRwaGRwMTFpZGUwMTo5MTE084855";
    if (checkHAIDBasicFormat(haid3)) {
      System.out.println(gson.toJson(decodeHAID(haid3)));
    } else {
      System.out.println("Invalid haid3 : " + haid3);
    }
  }
}
