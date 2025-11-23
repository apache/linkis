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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

  /**
   * @param plaintext
   * @return
   * @throws NoSuchAlgorithmException
   */
  public static String encrypt(String plaintext) throws NoSuchAlgorithmException {
    // 使用 MD5 算法创建 MessageDigest 对象
    MessageDigest md = MessageDigest.getInstance("MD5");
    // 更新 MessageDigest 对象中的字节数据
    md.update(plaintext.getBytes());
    // 对更新后的数据计算哈希值，存储在 byte 数组中
    byte[] digest = md.digest();
    // 将 byte 数组转换为十六进制字符串
    StringBuilder sb = new StringBuilder();
    for (byte b : digest) {
      sb.append(String.format("%02x", b & 0xff));
    }
    // 返回十六进制字符串
    return sb.toString();
  }
}
