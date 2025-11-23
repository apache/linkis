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

package org.apache.linkis.engineplugin.trino.interceptor;

import javax.security.auth.callback.PasswordCallback;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Response;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

public class PasswordInterceptor implements Interceptor {
  private final String user;
  private final PasswordCallback callback;
  private volatile String credentials = null;
  private volatile long expiredTimestamp = 0;

  public PasswordInterceptor(String user, PasswordCallback callback) {
    this.user = user;
    this.callback = callback;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    return chain.proceed(chain.request().newBuilder().header(AUTHORIZATION, credentials()).build());
  }

  private synchronized String credentials() {
    long timeMillis = System.currentTimeMillis();
    if (credentials == null || timeMillis > expiredTimestamp) {
      /* expired for 10 minutes */
      expiredTimestamp = timeMillis + 600000L;
      credentials = Credentials.basic(user, new String(callback.getPassword()));
    }
    return credentials;
  }
}
