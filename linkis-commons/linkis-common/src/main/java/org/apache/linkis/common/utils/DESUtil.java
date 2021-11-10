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
 
package org.apache.linkis.common.utils;

import org.apache.commons.lang.StringUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

public class DESUtil {
    private final static String DES = "DES";
    private final static String XBYTE = "X";

    /**
     * Description Encryption based on key values(Description 根据键值进行加密)
     * @param data
     * @param key Encryption key byte array(加密键byte数组)
     * @return Ciphertext(密文)
     * @throws Exception
     */
    public static String encrypt(String data, String key) throws Exception {
        if(StringUtils.isNotBlank(key) && key.length() < 8){
            int i = key.length();
            while((8-i) > 0){
                key += XBYTE;
                i++;
            }
        }
        byte[] bt = encrypt(data.getBytes(), key.getBytes());
        String strs = new BASE64Encoder().encode(bt);
        return strs;
    }

    /**
     * Description Encryption based on key values(Description 根据键值进行加密)
     * @param data
     * @param key Encryption key byte array(加密键byte数组)
     * @return Ciphertext(密文)
     * @throws Exception
     */
    public static String decrypt(String data, String key) throws Exception {
        if (StringUtils.isBlank(data)){
            return null;
        }
        if(StringUtils.isNotBlank(key) && key.length() < 8){
            int i = key.length();
            while((8-i) > 0){
                key += XBYTE;
                i++;
            }
        }
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] buf = decoder.decodeBuffer(data);
        byte[] bt = decrypt(buf, key.getBytes());
        return new String(bt);
    }

    /**
     * Description Encryption based on key values(Description 根据键值进行加密)
     * @param data
     * @param key Encryption key byte array(加密键byte数组)
     * @return Ciphertext(密文)
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // Generate a trusted random number source(生成一个可信任的随机数源)
        SecureRandom sr = new SecureRandom();

        // Create a DESKeySpec object from the original key data（从原始密钥数据创建DESKeySpec对象）
        DESKeySpec dks = new DESKeySpec(key);

        // Create a key factory and use it to convert the DESKeySpec to a SecretKey object（创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象）
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // The Cipher object actually completes the encryption operation.（Cipher对象实际完成加密操作）
        Cipher cipher = Cipher.getInstance(DES);

        // Initialize a Cipher object with a key（用密钥初始化Cipher对象）
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }

    /**
     * Description Encryption based on key values(Description 根据键值进行加密)
     * @param data
     * @param key Encryption key byte array(加密键byte数组)
     * @return Ciphertext(密文)
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // Generate a trusted random number source（生成一个可信任的随机数源）
        SecureRandom sr = new SecureRandom();

        // Create a DESKeySpec object from the original key data（从原始密钥数据创建DESKeySpec对象）
        DESKeySpec dks = new DESKeySpec(key);

        // Create a key factory and use it to convert the DESKeySpec to a SecretKey object（创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象）
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // The Cipher object actually completes the decryption operation.（Cipher对象实际完成解密操作）
        Cipher cipher = Cipher.getInstance(DES);

        // Initialize a Cipher object with a key（用密钥初始化Cipher对象）
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }
    public static void main(String[] args) throws Exception {
    }
}
