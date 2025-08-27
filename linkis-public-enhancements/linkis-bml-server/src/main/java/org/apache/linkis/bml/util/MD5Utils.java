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

package org.apache.linkis.bml.util;

import org.apache.linkis.bml.common.Constant;

import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MD5Utils {

  private static final char[] HEX_DIGITS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };

  private static final Logger logger = LoggerFactory.getLogger(MD5Utils.class);

  public static String getMD5(String s) {
    try {
      byte[] btInput = s.getBytes(Constant.UTF8_ENCODE);
      MessageDigest mdInst = MessageDigest.getInstance("MD5");
      mdInst.update(btInput);
      byte[] md = mdInst.digest();
      int j = md.length;
      char str[] = new char[j * 2];
      int k = 0;
      for (int i = 0; i < j; i++) {
        byte byte0 = md[i];
        str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
        str[k++] = HEX_DIGITS[byte0 & 0xf];
      }
      return new String(str);
    } catch (Exception e) {
      logger.error("create MD5 for failed, reason:", e);
      return null;
    }
  }

  public static String getMD5(byte[] btInput) {
    try {
      MessageDigest mdInst = MessageDigest.getInstance("MD5");
      mdInst.update(btInput);
      byte[] md = mdInst.digest();
      int j = md.length;
      char str[] = new char[j * 2];
      int k = 0;
      for (int i = 0; i < j; i++) {
        byte byte0 = md[i];
        str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
        str[k++] = HEX_DIGITS[byte0 & 0xf];
      }
      return new String(str);
    } catch (Exception e) {
      logger.error("create MD5 for failed, reason:", e);
      return null;
    }
  }
}
