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

package org.apache.linkis.engineplugin.spark.client.deployment.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

/**
 * This is a utility class to deal files and directories. Contains utilities for recursive deletion
 * and creation of temporary files.
 */
public final class FileUtils {

  /** Global lock to prevent concurrent directory deletes under Windows and MacOS. */
  private static final Object DELETE_LOCK = new Object();

  /** The alphabet to construct the random part of the filename from. */
  private static final char[] ALPHABET = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  /** The length of the random part of the filename. */
  private static final int RANDOM_FILE_NAME_LENGTH = 12;

  /**
   * The maximum size of array to allocate for reading. See {@code MAX_BUFFER_SIZE} in {@link Files}
   * for more.
   */
  private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

  /** The size of the buffer used for reading. */
  private static final int BUFFER_SIZE = 4096;

  private static final String JAR_FILE_EXTENSION = "jar";

  public static final String CLASS_FILE_EXTENSION = "class";

  public static final String PACKAGE_SEPARATOR = ".";

  // ------------------------------------------------------------------------

  public static void writeCompletely(WritableByteChannel channel, ByteBuffer src)
      throws IOException {
    while (src.hasRemaining()) {
      channel.write(src);
    }
  }

  // ------------------------------------------------------------------------

  /** Lists the given directory in a resource-leak-safe way. */
  public static Path[] listDirectory(Path directory) throws IOException {
    try (Stream<Path> stream = Files.list(directory)) {
      return stream.toArray(Path[]::new);
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Constructs a random filename with the given prefix and a random part generated from hex
   * characters.
   *
   * @param prefix the prefix to the filename to be constructed
   * @return the generated random filename with the given prefix
   */
  public static String getRandomFilename(final String prefix) {
    final Random rnd = new Random();
    final StringBuilder stringBuilder = new StringBuilder(prefix);

    for (int i = 0; i < RANDOM_FILE_NAME_LENGTH; i++) {
      stringBuilder.append(ALPHABET[rnd.nextInt(ALPHABET.length)]);
    }

    return stringBuilder.toString();
  }

  // ------------------------------------------------------------------------
  //  Simple reading and writing of files
  // ------------------------------------------------------------------------

  public static String readFile(File file, String charsetName) throws IOException {
    byte[] bytes = readAllBytes(file.toPath());
    return new String(bytes, charsetName);
  }

  public static String readFileUtf8(File file) throws IOException {
    return readFile(file, "UTF-8");
  }

  public static void writeFile(File file, String contents, String encoding) throws IOException {
    byte[] bytes = contents.getBytes(encoding);
    Files.write(file.toPath(), bytes, StandardOpenOption.WRITE);
  }

  public static void writeFileUtf8(File file, String contents) throws IOException {
    writeFile(file, contents, "UTF-8");
  }

  /**
   * Reads all the bytes from a file. The method ensures that the file is closed when all bytes have
   * been read or an I/O error, or other runtime exception, is thrown.
   *
   * <p>This is an implementation that follow {@link Files#readAllBytes(Path)}, and the difference
   * is that it limits the size of the direct buffer to avoid direct-buffer OutOfMemoryError. When
   * {@link Files#readAllBytes(Path)} or other interfaces in java API can do this in the future, we
   * should remove it.
   *
   * @param path the path to the file
   * @return a byte array containing the bytes read from the file
   * @throws IOException if an I/O error occurs reading from the stream
   * @throws OutOfMemoryError if an array of the required size cannot be allocated, for example the
   *     file is larger that {@code 2GB}
   */
  public static byte[] readAllBytes(Path path) throws IOException {
    try (SeekableByteChannel channel = Files.newByteChannel(path);
        InputStream in = Channels.newInputStream(channel)) {

      long size = channel.size();
      if (size > (long) MAX_BUFFER_SIZE) {
        throw new OutOfMemoryError("Required array size too large");
      }

      return read(in, (int) size);
    }
  }

  /**
   * Reads all the bytes from an input stream. Uses {@code initialSize} as a hint about how many
   * bytes the stream will have and uses {@code directBufferSize} to limit the size of the direct
   * buffer used to read.
   *
   * @param source the input stream to read from
   * @param initialSize the initial size of the byte array to allocate
   * @return a byte array containing the bytes read from the file
   * @throws IOException if an I/O error occurs reading from the stream
   * @throws OutOfMemoryError if an array of the required size cannot be allocated
   */
  private static byte[] read(InputStream source, int initialSize) throws IOException {
    int capacity = initialSize;
    byte[] buf = new byte[capacity];
    int nread = 0;
    int n;

    for (; ; ) {
      // read to EOF which may read more or less than initialSize (eg: file
      // is truncated while we are reading)
      while ((n = source.read(buf, nread, Math.min(capacity - nread, BUFFER_SIZE))) > 0) {
        nread += n;
      }

      // if last call to source.read() returned -1, we are done
      // otherwise, try to read one more byte; if that failed we're done too
      if (n < 0 || (n = source.read()) < 0) {
        break;
      }

      // one more byte was read; need to allocate a larger buffer
      if (capacity <= MAX_BUFFER_SIZE - capacity) {
        capacity = Math.max(capacity << 1, BUFFER_SIZE);
      } else {
        if (capacity == MAX_BUFFER_SIZE) {
          throw new OutOfMemoryError("Required array size too large");
        }
        capacity = MAX_BUFFER_SIZE;
      }
      buf = Arrays.copyOf(buf, capacity);
      buf[nread++] = (byte) n;
    }
    return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
  }

  /** Private default constructor to avoid instantiation. */
  private FileUtils() {}
}
