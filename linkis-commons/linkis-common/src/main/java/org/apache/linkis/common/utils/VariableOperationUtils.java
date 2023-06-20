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

import org.apache.linkis.common.exception.VariableOperationFailedException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** support variable operation #{yyyyMMdd%-1d}/#{yyyy-MM-01%-2M} Date: 2021/5/7 11:10 */
public class VariableOperationUtils {

  private static final String DOLLAR = "&";
  private static final String PLACEHOLDER_SPLIT = "%";
  private static final String PLACEHOLDER_LEFT = "{";
  private static final String LEFT = DOLLAR + PLACEHOLDER_LEFT;
  private static final String PLACEHOLDER_RIGHT = "}";
  private static final String CYCLE_YEAR = "y";
  private static final String CYCLE_MONTH = "M";
  private static final String CYCLE_DAY = "d";
  private static final String CYCLE_HOUR = "H";
  private static final String CYCLE_MINUTE = "m";
  private static final String CYCLE_SECOND = "s";
  private static final String[] CYCLES =
      new String[] {CYCLE_YEAR, CYCLE_MONTH, CYCLE_DAY, CYCLE_HOUR, CYCLE_MINUTE, CYCLE_SECOND};

  /**
   * yyyy-MM-dd HH:mm:ss
   *
   * @param date
   * @return
   */
  public static ZonedDateTime toZonedDateTime(Date date, ZoneId zoneId) {
    Instant instant = date.toInstant();
    LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
    return ZonedDateTime.of(localDateTime, zoneId);
  }

  /**
   * yyyy-MM-dd HH:mm:ss
   *
   * @param date
   * @return
   */
  public static ZonedDateTime toZonedDateTime(Date date) {
    return toZonedDateTime(date, ZoneId.systemDefault());
  }

  /**
   * json support variable operation
   *
   * @param dateTime
   * @param str
   * @return
   */
  public static String replaces(ZonedDateTime dateTime, String str)
      throws VariableOperationFailedException {
    return replaces(dateTime, str, true);
  }

  /**
   * json support variable operation
   *
   * @param dateTime
   * @param str
   * @param format
   * @return
   */
  public static String replaces(ZonedDateTime dateTime, String str, boolean format)
      throws VariableOperationFailedException {
    try {
      JsonNode rootNode = JsonUtils.jackson().readTree(str);
      if (rootNode.isArray() || rootNode.isObject()) {
        replaceJson(dateTime, rootNode);
        return rootNode.toString();
      }
    } catch (Exception e) {
      return replace(dateTime, str);
    }
    return replace(dateTime, str);
  }

  /**
   * @param dateTime
   * @param str
   * @return
   */
  private static String replace(ZonedDateTime dateTime, String str)
      throws VariableOperationFailedException {
    StringBuilder buffer = new StringBuilder(str);
    int startIndex = str.indexOf(LEFT);

    while (startIndex != -1) {
      int endIndex = buffer.indexOf(PLACEHOLDER_RIGHT, startIndex);
      if (endIndex != -1) {
        String placeHolder = buffer.substring(startIndex, endIndex + 1);
        String content = placeHolder.replace(LEFT, "").replace(PLACEHOLDER_RIGHT, "").trim();
        String[] parts = content.split(PLACEHOLDER_SPLIT);
        try {
          ZonedDateTime ndt = dateTime;
          for (int i = 1; i < parts.length; i++) {
            ndt = changeDateTime(ndt, parts[i]);
          }

          String newContent = ndt.format(DateTimeFormatter.ofPattern(parts[0]));
          if (buffer.substring(startIndex, endIndex + 1).contains(DOLLAR)) {
            buffer.replace(startIndex, endIndex + 1, newContent);
          }
          startIndex = buffer.indexOf(LEFT, startIndex + newContent.length());
        } catch (IllegalArgumentException e1) {
          startIndex = buffer.indexOf(LEFT, endIndex);
        } catch (Exception e2) {
          throw new VariableOperationFailedException(
              20050, "variable operation expression" + e2.getMessage(), e2);
        }
      } else {
        startIndex = -1; // leave while
      }
    }
    return buffer.toString();
  }

  /**
   * @param dateTime
   * @param str
   * @return
   */
  private static ZonedDateTime changeDateTime(ZonedDateTime dateTime, String str) {
    if (str == null || str.isEmpty()) {
      return dateTime;
    }

    for (String cycle : CYCLES) {
      if (str.contains(cycle)) {
        switch (cycle) {
          case CYCLE_DAY:
            return dateTime.plusDays(Integer.parseInt(str.replace(CYCLE_DAY, "")));
          case CYCLE_HOUR:
            return dateTime.plusHours(Integer.parseInt(str.replace(CYCLE_HOUR, "")));
          case CYCLE_MINUTE:
            return dateTime.plusMinutes(Integer.parseInt(str.replace(CYCLE_MINUTE, "")));
          case CYCLE_MONTH:
            return dateTime.plusMonths(Integer.parseInt(str.replace(CYCLE_MONTH, "")));
          case CYCLE_SECOND:
            return dateTime.plusSeconds(Integer.parseInt(str.replace(CYCLE_SECOND, "")));
          case CYCLE_YEAR:
            return dateTime.plusYears(Integer.parseInt(str.replace(CYCLE_YEAR, "")));
          default:
            break;
        }
      }
    }

    return dateTime;
  }

  /**
   * json support variable operation
   *
   * @param dateTime
   * @param object
   */
  @SuppressWarnings("DuplicatedCode")
  private static void replaceJson(ZonedDateTime dateTime, JsonNode object)
      throws VariableOperationFailedException {
    if (object.isArray()) {
      ArrayNode arrayNode = (ArrayNode) object;
      for (int i = 0; i < arrayNode.size(); i++) {
        final JsonNode temp = arrayNode.get(i);
        if (temp.isArray()) {
          replaceJson(dateTime, temp);
        } else if (temp.isObject()) {
          replaceJson(dateTime, temp);
        } else {
          arrayNode.remove(i);
          arrayNode.insert(i, replace(dateTime, temp.toString()));
        }
      }
    } else if (object.isObject()) {
      ObjectNode objectNode = (ObjectNode) object;
      final Iterator<Map.Entry<String, JsonNode>> fields = object.fields();
      while (fields.hasNext()) {
        final Map.Entry<String, JsonNode> field = fields.next();
        final JsonNode temp = field.getValue();
        if (temp.isArray()) {
          replaceJson(dateTime, temp);
        } else if (temp.isObject()) {
          replaceJson(dateTime, temp);
        } else {
          objectNode.put(field.getKey(), replace(dateTime, temp.toString()));
        }
      }
    }
  }
}
