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

import org.apache.linkis.common.conf.Configuration;

import org.apache.commons.lang3.StringUtils;

/**
 * Code masking utility for security logging. Prevents sensitive user code from being logged in
 * plain text.
 */
public class CodeUtils {

  /** Default preview length for code snippet (first N characters) */
  private static final int DEFAULT_CODE_SNIPPET_LENGTH = 6;

  private static final String MSG_EMPTY_CODE = "[empty code]";

  /**
   * Mask code for logging - only shows length and line count
   *
   * @param code the code to mask
   * @return masked code information
   */
  public static String maskCode(String code) {
    if (!Configuration.CODE_MASK_ENABLED()) {
      return code;
    }
    if (StringUtils.isBlank(code)) {
      return MSG_EMPTY_CODE;
    }
    int length = code.length();
    int lines = code.split("\n", -1).length;
    return String.format(
        "[code length: %d, lines: %d, snippet: %s]",
        length, lines, getCodeSnippet(code, DEFAULT_CODE_SNIPPET_LENGTH));
  }

  /**
   * Mask code for logging - shows type, length, line count, and code snippet
   *
   * @param code the code to mask
   * @param codeType the type of code (e.g., "SQL", "Scala", "Python")
   * @return masked code information with snippet
   */
  public static String maskCode(String code, String codeType) {
    if (!Configuration.CODE_MASK_ENABLED()) {
      return code;
    }
    if (StringUtils.isBlank(code)) {
      return MSG_EMPTY_CODE + codeType;
    }
    int length = code.length();
    int lines = code.split("\n", -1).length;
    return String.format(
        "[%s code, length: %d, lines: %d, snippet: %s]",
        codeType, length, lines, getCodeSnippet(code, DEFAULT_CODE_SNIPPET_LENGTH));
  }

  /**
   * Get code snippet (first N characters) for debugging
   *
   * @param code the code
   * @param length number of characters to show
   * @return code snippet (always truncated for security)
   */
  public static String getCodeSnippet(String code, int length) {
    if (StringUtils.isBlank(code)) {
      return "";
    }
    String trimmed = code.trim();
    // Always truncate for security, even if code is short
    if (trimmed.length() <= length) {
      return trimmed + "...";
    }
    return trimmed.substring(0, length) + "...";
  }
}
