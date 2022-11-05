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

package org.apache.linkis.jobhistory.cache.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class MD5Util {

  private static final int SIXTY_FOUR = 64;
  private static final int SIXTEEN = 16;

  /**
   * MD5加密
   *
   * @param src 需要加密的字符串
   * @param isUpper 大小写
   * @param bit 加密长度（16,32,64）
   * @return
   */
  public static String getMD5(String src, boolean isUpper, Integer bit) {
    String md5 = new String();
    try {
      // 创建加密对象
      MessageDigest md = MessageDigest.getInstance("md5");
      if (bit == SIXTY_FOUR) {
        md5 =
            Base64.getMimeEncoder().encodeToString(md.digest(src.getBytes(StandardCharsets.UTF_8)));
      } else {
        // 计算MD5函数
        md.update(src.getBytes(StandardCharsets.UTF_8));
        byte b[] = md.digest();
        int i;
        StringBuffer sb = new StringBuffer("");
        for (int offset = 0; offset < b.length; offset++) {
          i = b[offset];
          if (i < 0) i += 256;
          if (i < 16) sb.append("0");
          sb.append(Integer.toHexString(i));
        }
        md5 = sb.toString();
        if (bit == SIXTEEN) {
          // 截取32位md5为16位
          String md16 = md5.substring(8, 24);
          md5 = md16;
          if (isUpper) {
            md5 = md5.toUpperCase();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (isUpper) {
      md5 = md5.toUpperCase();
    }
    return md5;
  }
}
