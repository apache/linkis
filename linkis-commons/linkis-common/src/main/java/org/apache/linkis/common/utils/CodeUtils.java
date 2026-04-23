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
 * Code masking utility for security logging. Prevents sensitive user code from being logged in
 * plain text.
 */
public class CodeUtils {

  /** Default maximum preview length */
  private static final int DEFAULT_MAX_PREVIEW_LENGTH = 50;

  /** Default preview length for code snippet (first N characters) */
  private static final int DEFAULT_CODE_SNIPPET_LENGTH = 6;

  /** Maximum lines to preview */
  private static final int MAX_PREVIEW_LINES = 3;

  /**
   * Mask code for logging - only shows length and line count
   *
   * @param code the code to mask
   * @return masked code information
   */
  public static String maskCode(String code) {
    if (StringUtils.isBlank(code)) {
      return "[empty code]";
    }
    int length = code.length();
    int lines = code.split("\n", -1).length;
    return String.format("[code length: %d, lines: %d]", length, lines);
  }

  /**
   * Mask code for logging - shows type, length, line count, and code snippet
   *
   * @param code the code to mask
   * @param codeType the type of code (e.g., "SQL", "Scala", "Python")
   * @return masked code information with snippet
   */
  public static String maskCode(String code, String codeType) {
    if (StringUtils.isBlank(code)) {
      return "[empty " + codeType + " code]";
    }
    int length = code.length();
    int lines = code.split("\n", -1).length;

    // Get code snippet (first N characters for debugging)
    String snippet = getCodeSnippet(code, DEFAULT_CODE_SNIPPET_LENGTH);

    return String.format(
        "[%s code, length: %d, lines: %d, snippet: %s]", codeType, length, lines, snippet);
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

  /**
   * Mask code for logging - shows type, length, line count, and preview
   *
   * @param code the code to mask
   * @param codeType the type of code (e.g., "SQL", "Scala", "Python")
   * @param maxPreviewLength maximum characters to preview
   * @return masked code information
   */
  public static String maskCode(String code, String codeType, int maxPreviewLength) {
    if (StringUtils.isBlank(code)) {
      return "[empty " + codeType + " code]";
    }
    int length = code.length();
    int lines = code.split("\n", -1).length;

    if (maxPreviewLength <= 0) {
      return String.format("[%s code, length: %d, lines: %d]", codeType, length, lines);
    }

    String preview = getPreview(code, maxPreviewLength);
    return String.format(
        "[%s code, length: %d, lines: %d, preview: %s]", codeType, length, lines, preview);
  }

  /**
   * Get a safe preview of the code (truncated and cleaned)
   *
   * @param code the code to preview
   * @param maxLength maximum length of preview
   * @return safe preview string
   */
  public static String getPreview(String code, int maxLength) {
    if (StringUtils.isBlank(code)) {
      return "";
    }

    // Remove sensitive patterns (passwords, tokens, etc.)
    String cleaned = removeSensitiveInfo(code);

    // Truncate to max length
    if (cleaned.length() <= maxLength) {
      return cleaned;
    }

    return cleaned.substring(0, maxLength) + "...";
  }

  /**
   * Remove sensitive information from code preview
   *
   * @param code the code to clean
   * @return cleaned code
   */
  private static String removeSensitiveInfo(String code) {
    // Remove common sensitive patterns
    String cleaned = code;

    // Remove password values
    cleaned =
        cleaned.replaceAll(
            "(?i)(password|passwd|pwd)\\s*['\"]?\\s*[:=]\\s*['\"][^'\"]*['\"]", "$1='***'");
    cleaned = cleaned.replaceAll("(?i)(password|passwd|pwd)\\s*[:=]\\s*[^\\s'\"]+", "$1=***");

    // Remove token/key values
    cleaned =
        cleaned.replaceAll(
            "(?i)(token|api[_-]?key|secret|access[_-]?key)\\s*['\"]?\\s*[:=]\\s*['\"][^'\"]*['\"]",
            "$1='***'");
    cleaned =
        cleaned.replaceAll(
            "(?i)(token|api[_-]?key|secret|access[_-]?key)\\s*[:=]\\s*[^\\s'\"]+", "$1=***");

    // Remove connection strings (jdbc:, mysql:, postgres:, etc.)
    cleaned =
        cleaned.replaceAll("(?i)(jdbc:[^\\s'\"]+|mysql://[^\\s'\"]+|postgres://[^\\s'\"]+)", "***");

    return cleaned;
  }

  /**
   * Get only the first few lines of code (for preview)
   *
   * @param code the code to preview
   * @param maxLines maximum lines to show
   * @return preview string
   */
  public static String getLinePreview(String code, int maxLines) {
    if (StringUtils.isBlank(code)) {
      return "";
    }

    String[] lines = code.split("\n", -1);
    StringBuilder preview = new StringBuilder();

    for (int i = 0; i < Math.min(maxLines, lines.length); i++) {
      if (i > 0) {
        preview.append("\n");
      }
      preview.append(lines[i]);
    }

    if (lines.length > maxLines) {
      preview.append("\n... (").append(lines.length - maxLines).append(" more lines)");
    }

    return preview.toString();
  }

  /**
   * Get code type from file extension or code content
   *
   * @param code the code
   * @param fileType file extension (e.g., ".sql", ".scala", ".py")
   * @return detected code type
   */
  public static String detectCodeType(String code, String fileType) {
    if (StringUtils.isNotBlank(fileType)) {
      switch (fileType.toLowerCase()) {
        case ".sql":
          return "SQL";
        case ".scala":
          return "Scala";
        case ".py":
          return "Python";
        case ".java":
          return "Java";
        case ".sh":
        case ".bash":
          return "Shell";
        case "hql":
          return "HiveQL";
        default:
          break;
      }
    }

    // Try to detect from content
    if (StringUtils.isBlank(code)) {
      return "Unknown";
    }

    String trimmed = code.trim().toUpperCase();

    if (trimmed.startsWith("SELECT")
        || trimmed.startsWith("INSERT")
        || trimmed.startsWith("UPDATE")
        || trimmed.startsWith("DELETE")
        || trimmed.startsWith("CREATE")
        || trimmed.startsWith("ALTER")
        || trimmed.startsWith("DROP")
        || trimmed.startsWith("WITH")) {
      return "SQL";
    }

    if (trimmed.contains("DEF ") || trimmed.contains("CLASS ") || trimmed.startsWith("IMPORT ")) {
      if (code.contains("println") || code.contains("Array(")) {
        return "Scala";
      }
      return "Java";
    }

    if (trimmed.startsWith("IMPORT ") || trimmed.contains("PRINT ") || trimmed.contains("DEF ")) {
      return "Python";
    }

    return "Unknown";
  }
}
