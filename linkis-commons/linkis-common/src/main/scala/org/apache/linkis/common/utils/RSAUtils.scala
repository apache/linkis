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

package org.apache.linkis.common.utils

import org.apache.linkis.common.conf.Configuration

import org.apache.commons.codec.binary.Hex
import org.apache.commons.net.util.Base64

import javax.crypto.Cipher

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.{KeyFactory, KeyPair, KeyPairGenerator, PrivateKey, PublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

object RSAUtils extends Logging {
  private implicit val keyPair = genKeyPair(2048)

  implicit val PREFIX = "{RSA}"

  def genKeyPair(keyLength: Int): KeyPair = {
    val keyPair = KeyPairGenerator.getInstance("RSA")
    keyPair.initialize(keyLength)
    keyPair.generateKeyPair()
  }

  def getDefaultPublicKey(): String = {
    new String(Base64.encodeBase64(keyPair.getPublic.getEncoded), StandardCharsets.UTF_8)
  }

  def getDefaultPrivateKey(): String = {
    new String(Base64.encodeBase64(keyPair.getPrivate.getEncoded), StandardCharsets.UTF_8)
  }

  def encrypt(data: Array[Byte], publicKey: PublicKey): Array[Byte] = {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    cipher.doFinal(data)
  }

  def encrypt(data: Array[Byte]): Array[Byte] = encrypt(data, keyPair.getPublic)

  def decrypt(data: String, privateKey: PrivateKey): Array[Byte] = {
    val dataBytes = Hex.decodeHex(data.toCharArray)
    decrypt(dataBytes, privateKey)
  }

  def decrypt(data: String): Array[Byte] = decrypt(data, keyPair.getPrivate)

  def decrypt(data: Array[Byte], privateKey: PrivateKey): Array[Byte] = {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    cipher.doFinal(data)
  }

  def decrypt(data: Array[Byte]): Array[Byte] = decrypt(data, keyPair.getPrivate)

  /**
   * 将字符串形式的公钥转换为 PublicKey 对象。
   *
   * @param publicKeyStr
   *   公钥字符串，Base64 编码
   * @return
   *   转换后的 PublicKey 对象
   */
  def stringToPublicKey(publicKeyStr: String): PublicKey = {
    val keyBytes = Base64.decodeBase64(publicKeyStr)
    val keySpec = new X509EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    keyFactory.generatePublic(keySpec)
  }

  /**
   * 将字符串形式的私钥转换为 PrivateKey 对象。
   *
   * @param privateKeyStr
   *   私钥字符串，Base64 编码
   * @return
   *   转换后的 PrivateKey 对象
   */
  def stringToPrivateKey(privateKeyStr: String): PrivateKey = {
    val keyBytes = Base64.decodeBase64(privateKeyStr)
    val keySpec = new PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    keyFactory.generatePrivate(keySpec)
  }

  /**
   * 使用 Linkis 配置文件中的公钥对数据进行加密。
   *
   * @param data
   *   需要加密的原始数据字符串
   * @return
   *   加密后的数据字符串，带有前缀
   */
  def encryptWithLinkisPublicKey(data: String): String = {
    // 从配置文件中获取公钥和私钥字符串
    val publicKey = Configuration.LINKIS_RSA_PUBLIC_KEY.getValue
    val privateKey = Configuration.LINKIS_RSA_PRIVATE_KEY.getValue
    // 将公钥和私钥字符串转换为 KeyPair 对象
    val keyPair =
      new KeyPair(RSAUtils.stringToPublicKey(publicKey), RSAUtils.stringToPrivateKey(privateKey))
    // 使用公钥对数据进行加密
    val encryptedData = RSAUtils.encrypt(data.getBytes, keyPair.getPublic)
    // 将加密后的数据进行 Base64 编码，并添加前缀
    val encodedEncryptedData =
      PREFIX + new String(Base64.encodeBase64URLSafe(encryptedData))
    encodedEncryptedData
  }

  /**
   * 使用 Linkis 配置文件中的私钥对数据进行解密。
   *
   * @param data
   *   需要解密的加密数据字符串，带有前缀
   * @return
   *   解密后的原始数据字符串
   */
  def dncryptWithLinkisPublicKey(data: String): String = {
    // 从配置文件中获取公钥和私钥字符串
    val publicKey = Configuration.LINKIS_RSA_PUBLIC_KEY.getValue
    val privateKey = Configuration.LINKIS_RSA_PRIVATE_KEY.getValue
    val decodedData = URLDecoder.decode(data, "UTF-8")
    // 将公钥和私钥字符串转换为 KeyPair 对象
    val keyPair =
      new KeyPair(RSAUtils.stringToPublicKey(publicKey), RSAUtils.stringToPrivateKey(privateKey))
    // 检查数据是否以指定前缀开头
    if (decodedData.startsWith(PREFIX)) {
      // 去掉前缀，获取加密数据部分
      val dataSub = decodedData.substring(5)
      // 将加密数据进行 Base64 解码
      val decodedEncryptedData = Base64.decodeBase64(dataSub)
      // 使用私钥对数据进行解密
      val decryptedData = RSAUtils.decrypt(decodedEncryptedData, keyPair.getPrivate)
      // 将解密后的数据转换为字符串
      val decryptedString = new String(decryptedData)
      decryptedString
    } else {
      logger.warn(s"token信息非$PREFIX 开头，不执行解密！")
      data
    }
  }

  /**
   * 从给定的 token 中提取前半部分字符串。
   *
   * @param token
   *   输入的完整 token 字符串。
   * @return
   *   提取的 token 后半部分字符串。
   */
  def tokenSubRule(token: String): String = {
    val lowerToken = token.toLowerCase()
    // 判断条件：
    // 1. 以 "-auth" 结尾（不区分大小写）且长度 < 12
    // 2. 或者长度 < 10
    if ((lowerToken.endsWith("-auth") && lowerToken.length < 12) || lowerToken.length < 10) {
      token // 不截取，原样返回
    } else {
      // 否则，取后半部分（原逻辑）
      token.substring(token.length / 2, token.length)
    }
  }

}
