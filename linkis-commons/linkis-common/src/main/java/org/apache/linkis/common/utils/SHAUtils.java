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

package org.apache.linkis.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class SHAUtils {

  /**
   * 对字符串加密,默认使用SHA-256
   *
   * @param strSrc 要加密的字符串
   * @param encName 加密类型
   * @return
   * @throws UnsupportedEncodingException
   */
  public static String Encrypt(String strSrc, String encName) throws UnsupportedEncodingException {
    MessageDigest md = null;
    String strDes = null;
    byte[] bt = strSrc.getBytes("utf-8");
    try {
      if (encName == null || encName.equals("")) {
        encName = "SHA-256";
      }
      md = MessageDigest.getInstance(encName);
      md.update(bt);
      strDes = bytes2Hex(md.digest()); // to HexString
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
    return strDes;
  }

  public static String bytes2Hex(byte[] bts) {
    String des = "";
    String tmp = null;
    for (int i = 0; i < bts.length; i++) {
      tmp = (Integer.toHexString(bts[i] & 0xFF));
      if (tmp.length() == 1) {
        des += "0";
      }
      des += tmp;
    }
    return des;
  }

  public static void main(String[] args) throws IOException {
    String applicationId = args[0];
    String app_id = args[1];
    String token = args[2];
    String nonce = args[3];
    if (StringUtils.isBlank(applicationId)) {
      throw new LinkageError("Invalid applicationId cannot be empty");
    }
    if (StringUtils.isBlank(app_id)) {
      throw new LinkageError("Invalid app_id cannot be empty");
    }
    if (StringUtils.isBlank(token)) {
      throw new LinkageError("Invalid token cannot be empty");
    }
    if (StringUtils.isBlank(nonce)) {
      throw new LinkageError("Invalid nonce cannot be empty");
    }
    Map<String, String> parms = new HashMap<>();
    String timestampStr = String.valueOf(System.currentTimeMillis());
    parms.put("applicationId", applicationId);
    parms.put("app_id", app_id);
    parms.put("timestamp", timestampStr);
    parms.put("nonce", nonce);
    if (StringUtils.isNotBlank(token)) {
      String signature =
          Encrypt(Encrypt(parms.get("app_id") + nonce + timestampStr, null) + token, null);
      parms.put("signature", signature);
    }
    System.out.println(parms);
  }
}
