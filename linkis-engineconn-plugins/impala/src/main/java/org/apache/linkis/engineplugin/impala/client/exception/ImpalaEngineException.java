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

package org.apache.linkis.engineplugin.impala.client.exception;

public class ImpalaEngineException extends RuntimeException {

  /** */
  private static final long serialVersionUID = 5045965784519089392L;

  public ImpalaEngineException(String message) {
    super(message);
  }

  public ImpalaEngineException(Exception exception) {
    super(exception);
  }

  public ImpalaEngineException(String message, Exception exception) {
    super(message, exception);
  }

  public static ImpalaEngineException of(ImpalaErrorCodeSummary code) {
    return new ImpalaEngineException(code.getErrorDesc());
  }

  public static ImpalaEngineException of(ImpalaErrorCodeSummary code, String massage) {
    return new ImpalaEngineException(String.format("%s, %s", code.getErrorDesc(), massage));
  }

  public static ImpalaEngineException of(ImpalaErrorCodeSummary code, Exception exception) {
    return new ImpalaEngineException(code.getErrorDesc(), exception);
  }

  public static ImpalaEngineException of(Exception exception) {
    return new ImpalaEngineException(exception);
  }
}
