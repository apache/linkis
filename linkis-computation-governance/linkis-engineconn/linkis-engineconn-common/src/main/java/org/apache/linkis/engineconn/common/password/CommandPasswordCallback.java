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

package org.apache.linkis.engineconn.common.password;

import org.apache.commons.io.IOUtils;

import javax.security.auth.callback.PasswordCallback;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class CommandPasswordCallback extends PasswordCallback {

  private static final Charset CHARSET = Charset.defaultCharset();

  public CommandPasswordCallback(String prompt) {
    this(prompt, false);
  }

  public CommandPasswordCallback(String prompt, boolean echoOn) {
    super(prompt, echoOn);
  }

  private static String string(InputStream inputStream) throws IOException {
    try {
      return IOUtils.toString(inputStream, CHARSET);
    } finally {
      inputStream.close();
    }
  }

  private static char[] array(InputStream inputStream) throws IOException {
    try {
      return IOUtils.toCharArray(inputStream, CHARSET);
    } finally {
      inputStream.close();
    }
  }

  @Override
  public char[] getPassword() {
    Process process = null;
    String prompt = getPrompt();
    try {
      process = new ProcessBuilder().command(prompt).start();
      int ret = process.waitFor();
      if (ret != 0) throw new RuntimeException(string(process.getErrorStream()));
      return array(process.getInputStream());
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Failed to get password by command: " + prompt, e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }
}
