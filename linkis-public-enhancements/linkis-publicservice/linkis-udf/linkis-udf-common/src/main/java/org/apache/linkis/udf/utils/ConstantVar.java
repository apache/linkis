/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.udf.utils;


public class ConstantVar {

    public final static int UDF_JAR = 0;
    public final static int UDF_PY = 1;
    public final static int UDF_SCALA = 2;
    public final static int FUNCTION_PY = 3;
    public final static int FUNCTION_SCALA = 4;

    public final static String FUNCTION = "function";
    public final static String UDF = "udf";
    public final static String ALL = "all";

    public final static String SYS = "sys";
    public final static String BDP = "bdp";
    public final static String SELF = "self";
    public final static String SHARE = "share";
    public final static String[] specialTypes = new String[]{SYS, BDP, SHARE};

}
