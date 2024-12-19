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

package org.apache.linkis.engineplugin.spark.executor;

import org.apache.commons.lang3.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Based on Commons Lang3 RandomStringUtils, but with a SecureRandom.
 *
 * <p>For internal Apache Linkis use only.
 */
final class SecureRandomStringUtils {
  // Based on
  // https://github.com/apache/commons-lang/blob/5e07d873e6b45714d29bf47634adffa3b5aef098/src/main/java/org/apache/commons/lang3/RandomStringUtils.java

  private static SecureRandom RANDOM_INSTANCE;

  private static SecureRandom random() {
    if (RANDOM_INSTANCE != null) {
      return RANDOM_INSTANCE;
    }
    try {
      RANDOM_INSTANCE = SecureRandom.getInstanceStrong();
      return RANDOM_INSTANCE;
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Cannot create SecureRandom.getInstanceStrong()", e);
    }
  }

  // Random
  /**
   * Creates a random string whose length is the number of characters specified.
   *
   * <p>Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z) and the
   * digits 0-9.
   *
   * @param count the length of random string to create
   * @return the random string
   * @throws IllegalArgumentException if {@code count} &lt; 0.
   */
  public static String randomAlphanumeric(final int count) {
    return random(count, true, true);
  }

  /**
   * Creates a random string whose length is the number of characters specified.
   *
   * <p>Characters will be chosen from the set of alpha-numeric characters as indicated by the
   * arguments.
   *
   * @param count the length of random string to create
   * @param letters if {@code true}, generated string may include alphabetic characters
   * @param numbers if {@code true}, generated string may include numeric characters
   * @return the random string
   * @throws IllegalArgumentException if {@code count} &lt; 0.
   */
  private static String random(final int count, final boolean letters, final boolean numbers) {
    return random(count, 0, 0, letters, numbers);
  }

  /**
   * Creates a random string whose length is the number of characters specified.
   *
   * <p>Characters will be chosen from the set of alpha-numeric characters as indicated by the
   * arguments.
   *
   * @param count the length of random string to create
   * @param start the position in set of chars to start at
   * @param end the position in set of chars to end before
   * @param letters if {@code true}, generated string may include alphabetic characters
   * @param numbers if {@code true}, generated string may include numeric characters
   * @return the random string
   * @throws IllegalArgumentException if {@code count} &lt; 0.
   */
  private static String random(
      final int count,
      final int start,
      final int end,
      final boolean letters,
      final boolean numbers) {
    return random(count, start, end, letters, numbers, null, random());
  }

  /**
   * Creates a random string based on a variety of options, using supplied source of randomness.
   *
   * <p>If start and end are both {@code 0}, start and end are set to {@code ' '} and {@code 'z'},
   * the ASCII printable characters, will be used, unless letters and numbers are both {@code
   * false}, in which case, start and end are set to {@code 0} and {@link Character#MAX_CODE_POINT}.
   *
   * <p>If set is not {@code null}, characters between start and end are chosen.
   *
   * <p>This method accepts a user-supplied {@link SecureRandom} instance to use as a source of
   * randomness. By seeding a single {@link SecureRandom} instance with a fixed seed and using it
   * for each call, the same random sequence of strings can be generated repeatedly and predictably.
   *
   * @param count the length of random string to create
   * @param start the position in set of chars to start at (inclusive)
   * @param end the position in set of chars to end before (exclusive)
   * @param letters if {@code true}, generated string may include alphabetic characters
   * @param numbers if {@code true}, generated string may include numeric characters
   * @param chars the set of chars to choose randoms from, must not be empty. If {@code null}, then
   *     it will use the set of all chars.
   * @param random a source of randomness.
   * @return the random string
   * @throws ArrayIndexOutOfBoundsException if there are not {@code (end - start) + 1} characters in
   *     the set array.
   * @throws IllegalArgumentException if {@code count} &lt; 0 or the provided chars array is empty.
   */
  private static String random(
      int count,
      int start,
      int end,
      final boolean letters,
      final boolean numbers,
      final char[] chars,
      final SecureRandom random) {
    if (count == 0) {
      return StringUtils.EMPTY;
    }
    if (count < 0) {
      throw new IllegalArgumentException(
          "Requested random string length " + count + " is less than 0.");
    }
    if (chars != null && chars.length == 0) {
      throw new IllegalArgumentException("The chars array must not be empty");
    }

    if (start == 0 && end == 0) {
      if (chars != null) {
        end = chars.length;
      } else if (!letters && !numbers) {
        end = Character.MAX_CODE_POINT;
      } else {
        end = 'z' + 1;
        start = ' ';
      }
    } else if (end <= start) {
      throw new IllegalArgumentException(
          "Parameter end (" + end + ") must be greater than start (" + start + ")");
    }

    final int zeroDigitAscii = 48;
    final int firstLetterAscii = 65;

    if (chars == null && (numbers && end <= zeroDigitAscii || letters && end <= firstLetterAscii)) {
      throw new IllegalArgumentException(
          "Parameter end ("
              + end
              + ") must be greater then ("
              + zeroDigitAscii
              + ") for generating digits "
              + "or greater then ("
              + firstLetterAscii
              + ") for generating letters.");
    }

    final StringBuilder builder = new StringBuilder(count);
    final int gap = end - start;

    while (count-- != 0) {
      final int codePoint;
      if (chars == null) {
        codePoint = random.nextInt(gap) + start;

        switch (Character.getType(codePoint)) {
          case Character.UNASSIGNED:
          case Character.PRIVATE_USE:
          case Character.SURROGATE:
            count++;
            continue;
        }

      } else {
        codePoint = chars[random.nextInt(gap) + start];
      }

      final int numberOfChars = Character.charCount(codePoint);
      if (count == 0 && numberOfChars > 1) {
        count++;
        continue;
      }

      if (letters && Character.isLetter(codePoint)
          || numbers && Character.isDigit(codePoint)
          || !letters && !numbers) {
        builder.appendCodePoint(codePoint);

        if (numberOfChars == 2) {
          count--;
        }

      } else {
        count++;
      }
    }
    return builder.toString();
  }

  private SecureRandomStringUtils() {
    // empty
  }
}
