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

package org.apache.linkis.manager.engineplugin.hbase.shell;

public final class Result {
  private final boolean success;
  private final String result;

  private final Throwable e;

  public Result(boolean success, String result, Throwable e) {
    this.success = success;
    this.result = result;
    this.e = e;
  }

  private static Result of(boolean success, String message, Throwable e) {
    return new Result(success, message, e);
  }

  public static Result ok(String message) {
    return Result.of(true, message, null);
  }

  public static Result ok() {
    return Result.of(true, "ok", null);
  }

  public static Result failed(String message, Throwable e) {
    return Result.of(false, message, e);
  }

  public static Result failed(Throwable e) {
    return Result.of(false, "error", e);
  }

  public boolean isSuccess() {
    return success;
  }

  public String getResult() {
    return result;
  }

  public Throwable getThrowable() {
    return e;
  }

  @Override
  public String toString() {
    return "Result{" + "success=" + success + ", result='" + result + '\'' + '}';
  }
}
