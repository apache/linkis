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

import org.apache.commons.codec.binary.Hex
import org.apache.commons.net.util.Base64

import javax.crypto.Cipher

import java.nio.charset.StandardCharsets
import java.security.{KeyPair, KeyPairGenerator, PrivateKey, PublicKey}

object RSAUtils {
  private implicit val keyPair = genKeyPair(1024)

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
}
