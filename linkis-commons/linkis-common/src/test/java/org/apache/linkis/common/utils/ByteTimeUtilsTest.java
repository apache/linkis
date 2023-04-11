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

import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ByteTimeUtilsTest {

  private static final ImmutableMap<String, Function<String, Long>> opFunction =
      ImmutableMap.<String, Function<String, Long>>builder()
          .put("byteStringAsBytes", tar -> ByteTimeUtils.byteStringAsBytes(tar))
          .put("byteStringAsKb", tar -> ByteTimeUtils.byteStringAsKb(tar))
          .put("byteStringAsMb", tar -> ByteTimeUtils.byteStringAsMb(tar))
          .put("byteStringAsGb", tar -> ByteTimeUtils.byteStringAsGb(tar))
          .build();

  private static final ImmutableMap<String, Long> convertToByte =
      ImmutableMap.<String, Long>builder()
          .put("1", 1l)
          .put("1b", 1l)
          .put("1B", 1l)
          .put("1k", 1024l)
          .put("1K", 1024l)
          .put("1kb", 1024l)
          .put("1Kb", 1024l)
          .put("1kB", 1024l)
          .put("1KB", 1024l)
          .put("1m", 1024l * 1024l)
          .put("1M", 1024l * 1024l)
          .put("1mb", 1024l * 1024l)
          .put("1Mb", 1024l * 1024l)
          .put("1mB", 1024l * 1024l)
          .put("1MB", 1024l * 1024l)
          .put("1g", 1024l * 1024l * 1024l)
          .put("1G", 1024l * 1024l * 1024l)
          .put("1gb", 1024l * 1024l * 1024l)
          .put("1gB", 1024l * 1024l * 1024l)
          .put("1Gb", 1024l * 1024l * 1024l)
          .put("1GB", 1024l * 1024l * 1024l)
          .put("1t", 1024l * 1024l * 1024l * 1024l)
          .put("1T", 1024l * 1024l * 1024l * 1024l)
          .put("1tb", 1024l * 1024l * 1024l * 1024l)
          .put("1Tb", 1024l * 1024l * 1024l * 1024l)
          .put("1tB", 1024l * 1024l * 1024l * 1024l)
          .put("1TB", 1024l * 1024l * 1024l * 1024l)
          .put("1p", 1024l * 1024l * 1024l * 1024l * 1024l)
          .put("1P", 1024l * 1024l * 1024l * 1024l * 1024l)
          .put("1pb", 1024l * 1024l * 1024l * 1024l * 1024l)
          .put("1Pb", 1024l * 1024l * 1024l * 1024l * 1024l)
          .put("1pB", 1024l * 1024l * 1024l * 1024l * 1024l)
          .put("1PB", 1024l * 1024l * 1024l * 1024l * 1024l)
          .build();

  private static final ImmutableMap<String, Long> convertToKB =
      ImmutableMap.<String, Long>builder()
          .put("1", 1l)
          .put("1024b", 1l)
          .put("1024B", 1l)
          .put("1k", 1l)
          .put("1K", 1l)
          .put("1kb", 1l)
          .put("1Kb", 1l)
          .put("1kB", 1l)
          .put("1KB", 1l)
          .put("1m", 1024l)
          .put("1M", 1024l)
          .put("1mb", 1024l)
          .put("1Mb", 1024l)
          .put("1mB", 1024l)
          .put("1MB", 1024l)
          .put("1g", 1024l * 1024l)
          .put("1G", 1024l * 1024l)
          .put("1gb", 1024l * 1024l)
          .put("1gB", 1024l * 1024l)
          .put("1Gb", 1024l * 1024l)
          .put("1GB", 1024l * 1024l)
          .build();

  private static final ImmutableMap<String, Long> convertToMB =
      ImmutableMap.<String, Long>builder()
          .put("1", 1l)
          .put("1024k", 1l)
          .put("1024K", 1l)
          .put("1024kb", 1l)
          .put("1024Kb", 1l)
          .put("1024kB", 1l)
          .put("1024KB", 1l)
          .put("1m", 1l)
          .put("1M", 1l)
          .put("1mb", 1l)
          .put("1Mb", 1l)
          .put("1mB", 1l)
          .put("1MB", 1l)
          .put("1g", 1024l)
          .put("1G", 1024l)
          .put("1gb", 1024l)
          .put("1gB", 1024l)
          .put("1Gb", 1024l)
          .put("1GB", 1024l)
          .build();

  private static final ImmutableMap<String, Long> convertToGB =
      ImmutableMap.<String, Long>builder()
          .put("1", 1l)
          .put("1024m", 1l)
          .put("1024M", 1l)
          .put("1024mb", 1l)
          .put("1024Mb", 1l)
          .put("1024mB", 1l)
          .put("1024MB", 1l)
          .put("1g", 1l)
          .put("1G", 1l)
          .put("1gb", 1l)
          .put("1gB", 1l)
          .put("1Gb", 1l)
          .put("1GB", 1l)
          .put("1t", 1024l)
          .put("1T", 1024l)
          .put("1tb", 1024l)
          .put("1Tb", 1024l)
          .put("1tB", 1024l)
          .put("1TB", 1024l)
          .build();

  @Test
  void byteStringAsBytes() {
    convertToByte.forEach(
        (k, v) -> Assertions.assertEquals(opFunction.get("byteStringAsBytes").apply(k), v));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> opFunction.get("byteStringAsBytes").apply("1A"));
  }

  @Test
  void byteStringAsKb() {
    convertToKB.forEach(
        (k, v) -> Assertions.assertEquals(opFunction.get("byteStringAsKb").apply(k), v));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> opFunction.get("byteStringAsKb").apply("1a"));
  }

  @Test
  void byteStringAsMb() {
    convertToMB.forEach(
        (k, v) -> Assertions.assertEquals(opFunction.get("byteStringAsMb").apply(k), v));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> opFunction.get("byteStringAsMb").apply("1c"));
  }

  @Test
  void byteStringAsGb() {
    convertToGB.forEach(
        (k, v) -> Assertions.assertEquals(opFunction.get("byteStringAsGb").apply(k), v));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> opFunction.get("byteStringAsGb").apply("1C"));
  }
}
