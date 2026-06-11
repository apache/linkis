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

/**
 * Token sensitive utility for masking sensitive information in logs.
 *
 * <p>This utility provides methods to mask Token information before logging to prevent sensitive
 * data leakage.
 *
 * <p>Configuration: Set linkis.log.token.mask.enable=false to disable token masking.
 */
public class TokenSensitiveUtils {

  /**
   * Mask token for logging purposes.
   *
   * <p>Masking rules:
   *
   * <ul>
   *   <li>If token length <= 6: keep first (length-3) characters + "***"
   *   <li>If token length > 6: keep first 3 characters + "***" + last 3 characters
   *   <li>If token is null or blank: return original token (fail-safe)
   *   <li>If masking is disabled via configuration: return original token
   *   <li>If any exception occurs: return original token (fail-safe)
   * </ul>
   *
   * @param token the token to be masked
   * @return masked token string, or original token if masking disabled or error occurs
   */
  public static String maskToken(String token) {
    // Return original token if masking is disabled
    if (!org.apache.linkis.common.conf.Configuration.TOKEN_MASK_ENABLED()) {
      return token;
    }

    // Return original token if null or blank (fail-safe)
    if (StringUtils.isBlank(token)) {
      return token;
    }

    try {
      int length = token.length();

      if (length <= 6) {
        // Keep first (length-3) characters + "***"
        int keepLength = Math.max(1, length - 3);
        return token.substring(0, keepLength) + "***";
      } else {
        // Keep first 3 characters + "***" + last 3 characters
        return token.substring(0, 3) + "***" + token.substring(length - 3);
      }
    } catch (Exception e) {
      // Fail-safe: return original token if any exception occurs
      return token;
    }
  }
}
