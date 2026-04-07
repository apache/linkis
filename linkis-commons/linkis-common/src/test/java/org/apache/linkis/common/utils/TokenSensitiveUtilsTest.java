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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for TokenSensitiveUtils. */
public class TokenSensitiveUtilsTest {

  @Test
  @DisplayName("Test masking token with length <= 6")
  public void testMaskTokenShort() {
    // Test case 1: length = 3
    String result1 = TokenSensitiveUtils.maskToken("abc");
    assertEquals("a***", result1, "Token length 3 should be masked as 'a***'");

    // Test case 2: length = 4
    String result2 = TokenSensitiveUtils.maskToken("abcd");
    assertEquals("a***", result2, "Token length 4 should be masked as 'a***'");

    // Test case 3: length = 6
    String result3 = TokenSensitiveUtils.maskToken("abc123");
    assertEquals("abc***", result3, "Token length 6 should be masked as 'abc***'");
  }

  @Test
  @DisplayName("Test masking token with length > 6")
  public void testMaskTokenLong() {
    // Test case 1: length = 12
    String result1 = TokenSensitiveUtils.maskToken("abc123def456");
    assertEquals("abc***456", result1, "Token length 12 should be masked as 'abc***456'");

    // Test case 2: length = 20
    String result2 = TokenSensitiveUtils.maskToken("abc123def456ghi789jk");
    assertEquals("abc***9jk", result2, "Token length 20 should keep first 3 and last 3 chars");

    // Test case 3: length = 7
    String result3 = TokenSensitiveUtils.maskToken("abc123d");
    assertEquals("abc***23d", result3, "Token length 7 should be masked as 'abc***23d'");
  }

  @Test
  @DisplayName("Test masking null or empty token (fail-safe: return original)")
  public void testMaskTokenNullOrEmpty() {
    // Test case 1: null token - return null (fail-safe)
    String result1 = TokenSensitiveUtils.maskToken(null);
    assertNull(result1, "Null token should return null (fail-safe)");

    // Test case 2: empty token - return empty (fail-safe)
    String result2 = TokenSensitiveUtils.maskToken("");
    assertEquals("", result2, "Empty token should return empty (fail-safe)");

    // Test case 3: blank token (spaces) - return original (fail-safe)
    String result3 = TokenSensitiveUtils.maskToken("   ");
    assertEquals("   ", result3, "Blank token should return original (fail-safe)");
  }

  @Test
  @DisplayName("Test masking preserves token length differentiation")
  public void testMaskTokenLengthDifferentiation() {
    // Different tokens should produce different masked results
    String token1 = "abc123";
    String token2 = "xyz789";
    String result1 = TokenSensitiveUtils.maskToken(token1);
    String result2 = TokenSensitiveUtils.maskToken(token2);

    assertNotEquals(result1, result2, "Different tokens should produce different masked results");
    assertEquals("abc***", result1, "Token length 6 should be masked as 'abc***'");
    assertEquals("xyz***", result2, "Token length 6 should be masked as 'xyz***'");

    // Test longer tokens also preserve differentiation
    String token3 = "abc123def456";
    String token4 = "xyz789ghi012";
    String result3 = TokenSensitiveUtils.maskToken(token3);
    String result4 = TokenSensitiveUtils.maskToken(token4);
    assertNotEquals(
        result3, result4, "Different long tokens should produce different masked results");
    assertTrue(result3.startsWith("abc"), "Masked token should start with first 3 chars");
    assertTrue(result3.endsWith("456"), "Masked token should end with last 3 chars");
  }

  @Test
  @DisplayName("Test maskToken handles edge cases")
  public void testMaskTokenEdgeCases() {
    // Test case 1: single character
    String result1 = TokenSensitiveUtils.maskToken("a");
    assertEquals("a***", result1, "Single char token should be 'a***'");

    // Test case 2: two characters
    String result2 = TokenSensitiveUtils.maskToken("ab");
    assertEquals("a***", result2, "Two char token should be 'a***'");

    // Test case 3: very long token
    String longToken = "abcdefghij1234567890abcdefghij";
    String result3 = TokenSensitiveUtils.maskToken(longToken);
    assertTrue(result3.startsWith("abc"), "Long token should start with first 3 chars");
    assertTrue(result3.endsWith("hij"), "Long token should end with last 3 chars");
    assertTrue(result3.contains("***"), "Long token should contain '***' in the middle");
  }

  @Test
  @DisplayName("Test maskToken handles special characters")
  public void testMaskTokenSpecialCharacters() {
    // Test case 1: token with hyphens
    String result1 = TokenSensitiveUtils.maskToken("abc-123-def");
    assertTrue(result1.startsWith("abc"), "Token with hyphens should start with first 3 chars");
    assertTrue(result1.contains("***"), "Token with hyphens should contain '***'");

    // Test case 2: token with underscores
    String result2 = TokenSensitiveUtils.maskToken("abc_123_def");
    assertTrue(result2.startsWith("abc"), "Token with underscores should start with first 3 chars");
    assertTrue(result2.contains("***"), "Token with underscores should contain '***'");

    // Test case 3: token with dots
    String result3 = TokenSensitiveUtils.maskToken("abc.123.def");
    assertTrue(result3.startsWith("abc"), "Token with dots should start with first 3 chars");
    assertTrue(result3.contains("***"), "Token with dots should contain '***'");
  }

  @Test
  @DisplayName("Test maskToken is thread-safe (no shared state)")
  public void testMaskTokenThreadSafe() {
    // Since maskToken is a static method with no shared state, it should be thread-safe
    // This test verifies that multiple calls produce consistent results
    String token = "abc123def456";
    String result1 = TokenSensitiveUtils.maskToken(token);
    String result2 = TokenSensitiveUtils.maskToken(token);
    assertEquals(result1, result2, "Multiple calls with same token should produce same result");
    assertEquals("abc***456", result1, "Masked token should match expected pattern");
  }
}
