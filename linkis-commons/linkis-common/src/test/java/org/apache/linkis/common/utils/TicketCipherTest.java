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

import javax.crypto.AEADBadTagException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TicketCipher unit tests covering V2 (AES-256-GCM) and V1 (legacy DES) paths. */
public class TicketCipherTest {

  private static final String TEST_KEY = "test-crypt-key-24-chars-long-enough";
  private static final String TEST_PLAINTEXT = "bfs_admin,1690000000000";

  // ── V2 (AES-256-GCM) ─────────────────────────────────────────────────

  @Test
  @DisplayName("v2_encryptDecrypt_roundTrip")
  public void v2EncryptDecryptRoundTrip() throws Exception {
    TicketCipher cipher = new TicketCipher(TEST_KEY, true);
    String ct = cipher.encrypt(TEST_PLAINTEXT);
    Assertions.assertNotNull(ct);
    Assertions.assertNotEquals(TEST_PLAINTEXT, ct);
    String pt = cipher.decrypt(ct);
    Assertions.assertEquals(TEST_PLAINTEXT, pt);
  }

  @Test
  @DisplayName("v2_differentPlaintextsProduceDifferentCiphertexts")
  public void v2DifferentPlaintextsProduceDifferentCiphertexts() throws Exception {
    TicketCipher cipher = new TicketCipher(TEST_KEY, true);
    String ct1 = cipher.encrypt("bfs_admin,1000");
    String ct2 = cipher.encrypt("bfs_user,2000");
    Assertions.assertNotEquals(ct1, ct2);
  }

  @Test
  @DisplayName("v2_samePlaintextProducesDifferentCiphertexts_dueToRandomIV")
  public void v2SamePlaintextDifferentCiphertext() throws Exception {
    TicketCipher cipher = new TicketCipher(TEST_KEY, true);
    String ct1 = cipher.encrypt(TEST_PLAINTEXT);
    String ct2 = cipher.encrypt(TEST_PLAINTEXT);
    // Different IV each call → different ciphertext
    Assertions.assertNotEquals(ct1, ct2);
    // But both decrypt to same plaintext
    Assertions.assertEquals(TEST_PLAINTEXT, cipher.decrypt(ct1));
    Assertions.assertEquals(TEST_PLAINTEXT, cipher.decrypt(ct2));
  }

  @Test
  @DisplayName("v2_deterministicKeyDerivation_acrossInstances")
  public void v2DeterministicKeyDerivation() throws Exception {
    TicketCipher c1 = new TicketCipher(TEST_KEY, true);
    TicketCipher c2 = new TicketCipher(TEST_KEY, true);
    String ct = c1.encrypt(TEST_PLAINTEXT);
    // Different instances with same raw key must produce same derived key
    Assertions.assertEquals(TEST_PLAINTEXT, c2.decrypt(ct));
  }

  @Test
  @DisplayName("v2_differentKeysProduceIncompatibleCiphertexts")
  public void v2DifferentKeysIncompatible() throws Exception {
    TicketCipher c1 = new TicketCipher("key-A-16-chars-long-enough-for-test", true);
    TicketCipher c2 = new TicketCipher("key-B-16-chars-long-enough-for-test", true);
    String ct = c1.encrypt(TEST_PLAINTEXT);
    // Different keys → GCM AEAD verification fails
    Assertions.assertThrows(Exception.class, () -> c2.decrypt(ct));
  }

  @Test
  @DisplayName("v2_tamperedCiphertext_rejectedWithAEADBadTag")
  public void v2TamperedCiphertextRejected() throws Exception {
    TicketCipher cipher = new TicketCipher(TEST_KEY, true);
    String ct = cipher.encrypt(TEST_PLAINTEXT);
    // Flip a bit in the last byte (part of GCM auth tag)
    byte[] bytes = java.util.Base64.getDecoder().decode(ct);
    bytes[bytes.length - 1] ^= 0x01;
    String tampered = java.util.Base64.getEncoder().encodeToString(bytes);
    Assertions.assertThrows(AEADBadTagException.class, () -> cipher.decrypt(tampered));
  }

  @Test
  @DisplayName("v2_blankInputDecryptReturnsNull")
  public void v2BlankInputReturnsNull() throws Exception {
    TicketCipher cipher = new TicketCipher(TEST_KEY, true);
    Assertions.assertNull(cipher.decrypt(""));
    Assertions.assertNull(cipher.decrypt(null));
  }

  // ── V1 (legacy DES) ──────────────────────────────────────────────────

  @Test
  @DisplayName("v1_encryptDecrypt_roundTrip")
  public void v1EncryptDecryptRoundTrip() throws Exception {
    TicketCipher cipher = new TicketCipher(TEST_KEY, false);
    String ct = cipher.encrypt(TEST_PLAINTEXT);
    Assertions.assertNotNull(ct);
    String pt = cipher.decrypt(ct);
    Assertions.assertEquals(TEST_PLAINTEXT, pt);
  }

  @Test
  @DisplayName("v1_returnsSameCiphertext_samePlaintext_deterministicDES")
  public void v1DeterministicDES() throws Exception {
    TicketCipher cipher = new TicketCipher(TEST_KEY, false);
    String ct1 = cipher.encrypt(TEST_PLAINTEXT);
    String ct2 = cipher.encrypt(TEST_PLAINTEXT);
    // DES is deterministic with the same key+plaintext (no random IV in existing DESUtil)
    Assertions.assertEquals(ct1, ct2);
  }

  // ── cross-version compatibility ──────────────────────────────────────

  @Test
  @DisplayName("v2Decrypt_v1Ticket_accepted")
  public void v2DecryptV1Ticket() throws Exception {
    TicketCipher v1cipher = new TicketCipher(TEST_KEY, false);
    String v1ticket = v1cipher.encrypt(TEST_PLAINTEXT);
    TicketCipher v2cipher = new TicketCipher(TEST_KEY, true);
    // V2 cipher can still decrypt V1 (legacy DES) tickets
    String pt = v2cipher.decrypt(v1ticket);
    Assertions.assertEquals(TEST_PLAINTEXT, pt);
  }

  @Test
  @DisplayName("v1Decrypt_v2Ticket_accepted")
  public void v1DecryptV2Ticket() throws Exception {
    TicketCipher v2cipher = new TicketCipher(TEST_KEY, true);
    String v2ticket = v2cipher.encrypt(TEST_PLAINTEXT);
    TicketCipher v1cipher = new TicketCipher(TEST_KEY, false);
    // V1 cipher can still decrypt V2 tickets (auto-detects V2 format via version byte)
    String pt = v1cipher.decrypt(v2ticket);
    Assertions.assertEquals(TEST_PLAINTEXT, pt);
  }

  @Test
  @DisplayName("v2Ticket_startWithVersionByte02")
  public void v2TicketStartsWithVersionByte() throws Exception {
    TicketCipher cipher = new TicketCipher(TEST_KEY, true);
    String ct = cipher.encrypt(TEST_PLAINTEXT);
    byte[] bytes = java.util.Base64.getDecoder().decode(ct);
    Assertions.assertEquals(0x02, bytes[0], "V2 ticket must start with version byte 0x02");
  }

  // ── unicode / special chars ──────────────────────────────────────────

  @Test
  @DisplayName("v2_encryptDecrypt_unicodeUsername")
  public void v2UnicodeUsernameTest() throws Exception {
    TicketCipher cipher = new TicketCipher(TEST_KEY, true);
    String plain = "bfs_用户测试,1690000000000";
    String ct = cipher.encrypt(plain);
    Assertions.assertEquals(plain, cipher.decrypt(ct));
  }

  @Test
  @DisplayName("v2_encryptDecrypt_longPlaintext")
  public void v2LongPlaintextTest() throws Exception {
    TicketCipher cipher = new TicketCipher(TEST_KEY, true);
    StringBuilder sb = new StringBuilder("bfs_");
    for (int i = 0; i < 100; i++) sb.append("user-name-");
    String plain = sb.toString();
    String ct = cipher.encrypt(plain);
    Assertions.assertEquals(plain, cipher.decrypt(ct));
  }
}
