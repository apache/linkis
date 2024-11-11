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

package org.apache.linkis.udf.conf;

public class Constants {
  public static final String FILE_EXTENSION_PY = ".py";
  public static final String FILE_EXTENSION_ZIP = ".zip";
  public static final String FILE_EXTENSION_TAR_GZ = ".tar.gz";
  public static final String FILE_PERMISSION = "770";
  public static final String DELIMITER_COMMA = ",";
  public static final String HDFS_PATH_UDF = "hdfs:///appcom/linkis/udf/";
  public static final int MAX_FILE_SIZE_MB = 50 * 1024 * 1024;
  public static final String[] OPERATORS = {"==", ">=", "<=", ">", "<", "~="};
}
