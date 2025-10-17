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

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.exception.ErrorException;

import org.apache.commons.net.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author cr949
 * @description 字符串加密 生成xx位加密串
 */
public class AESUtils {

  /** key 加密算法 */
  private static final String KEY_ALGORITHM = "AES";

  /** 固定值 */
  private static final String SECRET_RANDOM = "SHA1PRNG";

  /** 编码方式 */
  public static final String ENCODING_TYPE = "UTF-8";

  /** 默认的加密算法 */
  private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

  public static final String PASSWORD = "password";

  public static final String IS_ENCRYPT = "isEncrypt";

  public static final String DECRYPT = "0";

  public static final String ENCRYPT = "1";

  public static final CommonVars<String> LINKIS_DATASOURCE_AES_KEY =
      CommonVars.apply("linkis.datasource.aes.secretkey", "");

  public static final CommonVars<Boolean> LINKIS_DATASOURCE_AES_SWITCH =
      CommonVars.apply("linkis.datasource.aes.switch", false);

  /**
   * 加密
   *
   * @param content
   * @param password
   * @return
   */
  public static String encrypt(String content, String password) {
    try {
      // 创建密码器
      Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

      byte[] byteContent = content.getBytes(ENCODING_TYPE);
      // 初始化为加密模式的密码器
      cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password));
      // 加密
      byte[] result = cipher.doFinal(byteContent);
      // 通过Base64转码返回
      return Base64.encodeBase64String(result).trim();
    } catch (Exception e) {
      throw new ErrorException(21304, "AES加密加密失败");
    }
  }

  public static String encrypt(byte[] content, String password) {
    try {
      // 创建密码器
      Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
      // 初始化为加密模式的密码器
      cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password));
      // 加密
      byte[] result = cipher.doFinal(content);
      // 通过Base64转码返回
      return Base64.encodeBase64String(result).trim();
    } catch (Exception e) {
      throw new ErrorException(21304, "AES加密加密失败");
    }
  }

  /**
   * AES 解密操作
   *
   * @param content
   * @param password
   * @return
   */
  public static String decrypt(String content, String password) {
    try {
      // 实例化
      Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
      // 使用密钥初始化，设置为解密模式
      cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password));
      // 执行操作
      byte[] result = cipher.doFinal(Base64.decodeBase64(content));
      return new String(result, ENCODING_TYPE);
    } catch (Exception e) {
      throw new ErrorException(21304, "AES加密解密失败");
    }
  }

  /**
   * AES 解密操作
   *
   * @param content
   * @param password
   * @return
   */
  public static byte[] decrypt(byte[] content, String password) {
    try {
      // 实例化
      Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
      // 使用密钥初始化，设置为解密模式
      cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password));
      // 执行操作
      return cipher.doFinal(Base64.decodeBase64(content));
    } catch (Exception e) {
      throw new ErrorException(21304, "AES加密解密失败");
    }
  }

  /**
   * 生成加密秘钥
   *
   * @return
   */
  private static SecretKeySpec getSecretKey(String password) {
    // 返回生成指定算法密钥生成器的 KeyGenerator 对象
    KeyGenerator kg;
    try {
      kg = KeyGenerator.getInstance(KEY_ALGORITHM);
      SecureRandom secureRandom = SecureRandom.getInstance(SECRET_RANDOM);
      secureRandom.setSeed(password.getBytes());
      // AES 要求密钥长度为 128
      kg.init(128, secureRandom);
      // 生成一个密钥
      SecretKey secretKey = kg.generateKey();
      // 转换为AES专用密钥
      return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new ErrorException(21304, "AES生成加密秘钥失败");
    }
  }

  public static String isDecryptByConf(String password) {
    if (AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()) {
      // decrypt
      password = AESUtils.decrypt(password, AESUtils.LINKIS_DATASOURCE_AES_KEY.getValue());
    }
    return password;
  }
}
