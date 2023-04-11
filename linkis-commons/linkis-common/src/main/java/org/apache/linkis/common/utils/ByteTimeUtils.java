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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

public class ByteTimeUtils {

  /** Returns a hash consistent with Spark's Utils.nonNegativeHash(). */
  public static int nonNegativeHash(Object obj) {
    if (obj == null) {
      return 0;
    }
    int hash = obj.hashCode();
    return hash != Integer.MIN_VALUE ? Math.abs(hash) : 0;
  }

  /**
   * Delete a file or directory and its contents recursively. Don't follow directories if they are
   * symlinks. Throws an exception if deletion is unsuccessful.
   */
  public static void deleteRecursively(File file) throws IOException {
    if (file == null) {
      return;
    }

    if (file.isDirectory() && !isSymlink(file)) {
      IOException savedIOException = null;
      for (File child : listFilesSafely(file)) {
        try {
          deleteRecursively(child);
        } catch (IOException e) {
          // In case of multiple exceptions, only last one will be thrown
          savedIOException = e;
        }
      }
      if (savedIOException != null) {
        throw savedIOException;
      }
    }

    boolean deleted = file.delete();
    // Delete can also fail if the file simply did not exist.
    if (!deleted && file.exists()) {
      throw new IOException("Failed to delete: " + file.getAbsolutePath());
    }
  }

  private static File[] listFilesSafely(File file) throws IOException {
    if (file.exists()) {
      File[] files = file.listFiles();
      if (files == null) {
        throw new IOException("Failed to list files for dir: " + file);
      }
      return files;
    } else {
      return new File[0];
    }
  }

  private static boolean isSymlink(File file) throws IOException {
    File fileInCanonicalDir = null;
    if (file.getParent() == null) {
      fileInCanonicalDir = file;
    } else {
      fileInCanonicalDir = new File(file.getParentFile().getCanonicalFile(), file.getName());
    }
    return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
  }

  private static final ImmutableMap<String, TimeUnit> timeSuffixes =
      ImmutableMap.<String, TimeUnit>builder()
          .put("us", TimeUnit.MICROSECONDS)
          .put("ms", TimeUnit.MILLISECONDS)
          .put("s", TimeUnit.SECONDS)
          .put("m", TimeUnit.MINUTES)
          .put("min", TimeUnit.MINUTES)
          .put("h", TimeUnit.HOURS)
          .put("d", TimeUnit.DAYS)
          .build();

  private static final ImmutableMap<String, ByteUnit> byteSuffixes =
      ImmutableMap.<String, ByteUnit>builder()
          .put("b", ByteUnit.BYTE)
          .put("k", ByteUnit.KiB)
          .put("kb", ByteUnit.KiB)
          .put("m", ByteUnit.MiB)
          .put("mb", ByteUnit.MiB)
          .put("g", ByteUnit.GiB)
          .put("gb", ByteUnit.GiB)
          .put("t", ByteUnit.TiB)
          .put("tb", ByteUnit.TiB)
          .put("p", ByteUnit.PiB)
          .put("pb", ByteUnit.PiB)
          .build();

  /**
   * Convert a passed time string (e.g. 50s, 100ms, or 250us) to a time count for internal use. If
   * no suffix is provided a direct conversion is attempted.
   */
  private static long parseTimeString(String str, TimeUnit unit) {
    String lower = str.toLowerCase().trim();

    try {
      Matcher m = Pattern.compile("(-?[0-9]+)([a-z]+)?").matcher(lower);
      if (!m.matches()) {
        throw new NumberFormatException("Failed to parse time string: " + str);
      }

      long val = Long.parseLong(m.group(1));
      String suffix = m.group(2);

      // Check for invalid suffixes
      if (suffix != null && !timeSuffixes.containsKey(suffix)) {
        throw new NumberFormatException("Invalid suffix: \"" + suffix + "\"");
      }

      // If suffix is valid use that, otherwise none was provided and use the default passed
      return unit.convert(val, suffix != null ? timeSuffixes.get(suffix) : unit);
    } catch (NumberFormatException e) {
      String timeError =
          "Time "
              + str
              + " must be specified as seconds (s), "
              + "milliseconds (ms), microseconds (us), minutes (m or min), hour (h), or day (d). "
              + "E.g. 50s, 100ms, or 250us.";

      throw new NumberFormatException(timeError + "\n" + e.getMessage());
    }
  }

  /**
   * Convert a time parameter such as (50s, 100ms, or 250us) to milliseconds for internal use. If no
   * suffix is provided, the passed number is assumed to be in ms.
   */
  public static long timeStringAsMs(String str) {
    return parseTimeString(str, TimeUnit.MILLISECONDS);
  }

  /**
   * Convert a time parameter such as (50s, 100ms, or 250us) to seconds for internal use. If no
   * suffix is provided, the passed number is assumed to be in seconds.
   */
  public static long timeStringAsSec(String str) {
    return parseTimeString(str, TimeUnit.SECONDS);
  }

  /** Returns a human-readable string representing a duration such as "35ms" */
  public static String msDurationToString(long ms) {
    int second = 1000;
    int minute = 60 * second;
    int hour = 60 * minute;
    Locale locale = Locale.US;
    if (ms < second) {
      return String.format(locale, "%d ms", ms);
    } else if (ms < minute) {
      return String.format(locale, "%.1f s", ms * 1f / second);
    } else if (ms < hour) {
      return String.format(locale, "%.1f m", ms * 1f / minute);
    } else {
      return String.format(locale, "%.2f h", ms * 1f / hour);
    }
  }

  /**
   * Convert a passed byte string (e.g. 50b, 100kb, or 250mb) to a ByteUnit for internal use. If no
   * suffix is provided a direct conversion of the provided default is attempted.
   */
  private static long parseByteString(String str, ByteUnit unit) {
    String lower = str.toLowerCase().trim();

    try {
      Matcher m = Pattern.compile("([0-9]+)\\s?([a-zA-Z]+)?").matcher(lower);
      //      Matcher fractionMatcher =
      // Pattern.compile("([0-9]+\\.[0-9]+)\\s?([a-z]+)?").matcher(lower);
      Matcher fractionMatcher =
          Pattern.compile("([0-9]+\\.[0-9]+)\\s?([a-zA-Z]{1,2})?").matcher(lower);

      long size = 0;
      int sub = 1;
      String suffix;
      if (fractionMatcher.matches()) {
        double val = Double.parseDouble(fractionMatcher.group(1));
        size = (long) (val * 100);
        suffix = fractionMatcher.group(2);
        sub = 100;
      } else if (m.matches()) {
        size = Long.parseLong(m.group(1));
        suffix = m.group(2);
      } else {
        throw new NumberFormatException("Failed to parse byte string: " + str);
      }
      // Check for invalid suffixes
      if (suffix != null && !byteSuffixes.containsKey(suffix)) {
        throw new NumberFormatException("Invalid suffix: \"" + suffix + "\"");
      }
      // If suffix is valid use that, otherwise none was provided and use the default passed
      return unit.convertFrom(size, suffix != null ? byteSuffixes.get(suffix) : unit) / sub;

    } catch (NumberFormatException e) {
      String timeError =
          "Error size string "
              + str
              + ". Size must be specified as bytes (b), "
              + "kibibytes (k), mebibytes (m), gibibytes (g), tebibytes (t), or pebibytes(p). "
              + "E.g. 50b, 100k, or 250m.";

      throw new IllegalArgumentException(timeError, e);
    }
  }

  /** Convert a quantity in bytes to a human-readable string such as "4.0 MB". */
  public static String bytesToString(long size) {
    long TB = 1L << 40;
    long GB = 1L << 30;
    long MB = 1L << 20;
    long KB = 1L << 10;

    double value;
    String unit;
    if (size >= 2 * TB || -2 * TB >= size) {
      value = size * 1f / TB;
      unit = "TB";
    } else if (size >= 2 * GB || -2 * GB >= size) {
      value = size * 1f / GB;
      unit = "GB";
    } else if (size >= 2 * MB || -2 * MB >= size) {
      value = size * 1f / MB;
      unit = "MB";
    } else if (size >= 2 * KB || -2 * KB >= size) {
      value = size * 1f / KB;
      unit = "KB";
    } else {
      value = size * 1f;
      unit = "B";
    }
    return String.format(Locale.US, "%.1f %s", value, unit);
  }

  /**
   * Convert a passed byte string (e.g. 50b, 100k, or 250m) to bytes for internal use.
   *
   * <p>If no suffix is provided, the passed number is assumed to be in bytes.
   */
  public static long byteStringAsBytes(String str) {
    return parseByteString(str, ByteUnit.BYTE);
  }

  /**
   * Convert a passed byte string (e.g. 50b, 100k, or 250m) to kibibytes for internal use.
   *
   * <p>If no suffix is provided, the passed number is assumed to be in kibibytes.
   */
  public static long byteStringAsKb(String str) {
    return parseByteString(str, ByteUnit.KiB);
  }

  /**
   * Convert a passed byte string (e.g. 50b, 100k, or 250m) to mebibytes for internal use.
   *
   * <p>If no suffix is provided, the passed number is assumed to be in mebibytes.
   */
  public static long byteStringAsMb(String str) {
    return parseByteString(str, ByteUnit.MiB);
  }

  /**
   * Convert a passed byte string (e.g. 50b, 100k, or 250m) to gibibytes for internal use.
   *
   * <p>If no suffix is provided, the passed number is assumed to be in gibibytes.
   */
  public static long byteStringAsGb(String str) {
    return parseByteString(str, ByteUnit.GiB);
  }

  /**
   * Convert a passed byte string (e.g. -50b, -100k, or -250m) to gibibytes for internal use.
   *
   * <p>If no suffix is provided, the passed number is assumed to be in gibibytes.
   */
  public static long negativeByteStringAsGb(String str) {
    if (str.startsWith("-")) {
      return Math.negateExact(parseByteString(str.substring(1), ByteUnit.GiB));
    }
    return parseByteString(str, ByteUnit.GiB);
  }

  /**
   * Returns a byte array with the buffer's contents, trying to avoid copying the data if possible.
   */
  public static byte[] bufferToArray(ByteBuffer buffer) {
    if (buffer.hasArray()
        && buffer.arrayOffset() == 0
        && buffer.array().length == buffer.remaining()) {
      return buffer.array();
    } else {
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      return bytes;
    }
  }

  enum ByteUnit {
    BYTE(1),
    KiB(1024L),
    MiB((long) Math.pow(1024L, 2L)),
    GiB((long) Math.pow(1024L, 3L)),
    TiB((long) Math.pow(1024L, 4L)),
    PiB((long) Math.pow(1024L, 5L));

    private ByteUnit(long multiplier) {
      this.multiplier = multiplier;
    }

    // Interpret the provided number (d) with suffix (u) as this unit type.
    // E.g. KiB.interpret(1, MiB) interprets 1MiB as its KiB representation = 1024k
    public long convertFrom(long d, ByteUnit u) {
      return u.convertTo(d, this);
    }

    // Convert the provided number (d) interpreted as this unit type to unit type (u).
    public long convertTo(long d, ByteUnit u) {
      if (multiplier > u.multiplier) {
        long ratio = multiplier / u.multiplier;
        if (Long.MAX_VALUE / ratio < d) {
          throw new IllegalArgumentException(
              "Conversion of "
                  + d
                  + " exceeds Long.MAX_VALUE in "
                  + name()
                  + ". Try a larger unit (e.g. MiB instead of KiB)");
        }
        return d * ratio;
      } else {
        // Perform operations in this order to avoid potential overflow
        // when computing d * multiplier
        return d / (u.multiplier / multiplier);
      }
    }

    public double toBytes(long d) {
      if (d < 0) {
        throw new IllegalArgumentException("Negative size value. Size must be positive: " + d);
      }
      return d * multiplier;
    }

    public long toKiB(long d) {
      return convertTo(d, KiB);
    }

    public long toMiB(long d) {
      return convertTo(d, MiB);
    }

    public long toGiB(long d) {
      return convertTo(d, GiB);
    }

    public long toTiB(long d) {
      return convertTo(d, TiB);
    }

    public long toPiB(long d) {
      return convertTo(d, PiB);
    }

    private final long multiplier;
  }
}
