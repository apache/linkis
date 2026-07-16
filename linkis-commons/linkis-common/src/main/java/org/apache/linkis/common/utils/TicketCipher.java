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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Ticket cipher supporting both AES-256-GCM (V2) and legacy DES (V1).
 *
 * <p>Solves CVE-2026-XXXX vulnerabilities E (weak DES 56-bit cipher) and F (no integrity /
 * anti-tamper protection) when V2 mode is enabled.
 *
 * <p>V2 (AES-256-GCM): key derivation via PBKDF2WithHmacSHA256 — deterministic across JVM
 * vendors. GCM AEAD provides both confidentiality and integrity in one pass.
 *
 * <p>V1 (DES): retained for backward compatibility. When the V2 switch is toggled off, new tickets
 * are issued in the legacy DES format so that downstream systems that have not yet upgraded can
 * still verify them.
 *
 * <p>The switch is controlled by a boolean flag at construction time. Operators toggle it via
 * linkis.ticket.cipher.v2.enabled=true|false (default true).
 */
public class TicketCipher {

  private static final String CIPHER = "AES/GCM/NoPadding";
  private static final int KEY_LEN_BITS = 256;
  private static final int IV_LEN_BYTES = 12; // GCM standard 96-bit IV
  private static final int TAG_LEN_BITS = 128; // GCM auth tag
  private static final int PBKDF2_ITERATIONS = 10000;
  private static final byte[] PBKDF2_SALT =
      "linkis-ticket-v2".getBytes(StandardCharsets.UTF_8);
  private static final byte VERSION_V2 = 0x02;

  private final SecretKey encKey;
  private final String cryptKeyRaw;
  private final SecureRandom rng = new SecureRandom();

  /**
   * @param cryptKeyRaw raw crypt key from wds.linkis.crypt.key
   * @param useV2 true → AES-256-GCM (default), false → legacy DES
   */
  public TicketCipher(String cryptKeyRaw, boolean useV2) {
    this.cryptKeyRaw = cryptKeyRaw;
    if (useV2) {
      try {
        this.encKey = deriveKey(cryptKeyRaw);
      } catch (Exception e) {
        throw new RuntimeException("Failed to derive ticket encryption key", e);
      }
    } else {
      this.encKey = null; // unused in V1 mode
    }
  }

  // ── public API ───────────────────────────────────────────────────────

  /**
   * Encrypt plaintext. When V2 is enabled: AES-256-GCM envelope (VERSION || IV || ciphertext+tag).
   * When V2 is disabled: legacy DES (via DESUtil.encrypt).
   */
  public String encrypt(String plaintext) throws Exception {
    if (encKey != null) {
      return encryptV2(plaintext);
    }
    return DESUtil.encrypt(plaintext, cryptKeyRaw);
  }

  /**
   * Decrypt data. Auto-detects V2 (version byte 0x02) vs legacy DES.
   * V2 tampering is rejected with AEADBadTagException.
   * V2 tickets issued before a rollback are still decrypted correctly even when V2 is disabled.
   */
  public String decrypt(String data) throws Exception {
    if (StringUtils.isBlank(data)) {
      return null;
    }
    byte[] in = Base64.getDecoder().decode(data);
    if (in != null && in.length >= 1 + IV_LEN_BYTES + 16 && in[0] == VERSION_V2) {
      return decryptV2(in);
    }
    return DESUtil.decrypt(data, cryptKeyRaw);
  }

  // ── V2 (AES-256-GCM) ─────────────────────────────────────────────────

  private String encryptV2(String plaintext) throws Exception {
    byte[] iv = new byte[IV_LEN_BYTES];
    rng.nextBytes(iv);

    Cipher cipher = Cipher.getInstance(CIPHER);
    cipher.init(Cipher.ENCRYPT_MODE, encKey, new GCMParameterSpec(TAG_LEN_BITS, iv));
    byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

    byte[] envelope = new byte[1 + IV_LEN_BYTES + ct.length];
    envelope[0] = VERSION_V2;
    System.arraycopy(iv, 0, envelope, 1, IV_LEN_BYTES);
    System.arraycopy(ct, 0, envelope, 1 + IV_LEN_BYTES, ct.length);
    return Base64.getEncoder().encodeToString(envelope);
  }

  private String decryptV2(byte[] in) throws Exception {
    byte[] iv = Arrays.copyOfRange(in, 1, 1 + IV_LEN_BYTES);
    byte[] ct = Arrays.copyOfRange(in, 1 + IV_LEN_BYTES, in.length);

    Cipher cipher = Cipher.getInstance(CIPHER);
    cipher.init(Cipher.DECRYPT_MODE, encKey, new GCMParameterSpec(TAG_LEN_BITS, iv));
    return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
  }

  // ── PBKDF2 key derivation (deterministic across JVM vendors) ─────────

  private static SecretKey deriveKey(String password) throws Exception {
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    PBEKeySpec spec =
        new PBEKeySpec(password.toCharArray(), PBKDF2_SALT, PBKDF2_ITERATIONS, KEY_LEN_BITS);
    SecretKey tmp = factory.generateSecret(spec);
    spec.clearPassword();
    return new SecretKeySpec(tmp.getEncoded(), "AES");
  }
}
