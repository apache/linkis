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

package org.apache.linkis.udf.utils;

public class ConstantVar {

  // udf type
  public static final int UDF_JAR = 0;
  public static final int UDF_PY = 1;
  public static final int UDF_SCALA = 2;
  public static final int FUNCTION_PY = 3;
  public static final int FUNCTION_SCALA = 4;

  // category
  public static final String FUNCTION = "function";
  public static final String UDF = "udf";
  public static final String ALL = "all";

  // user type
  public static final String SYS_USER = "sys";
  public static final String BDP_USER = "bdp";
  public static final String SELF_USER = "self";
  public static final String SHARE_USER = "share";
  public static final String EXPIRE_USER = "expire";

  public static final String[] specialTypes =
      new String[] {SYS_USER, BDP_USER, SHARE_USER, EXPIRE_USER};
}
