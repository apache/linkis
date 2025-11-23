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

package org.apache.linkis.datasourcemanager.common;

import org.apache.linkis.common.conf.CommonVars;

/** Store error code map */
public class ServiceErrorCode {

  public static CommonVars<Integer> TRANSFORM_FORM_ERROR =
      CommonVars.apply("wds.linkis.server.dsm.error-code.transform", 99987);

  public static CommonVars<Integer> BML_SERVICE_ERROR =
      CommonVars.apply("wds.linkis.server.dsm.error-code.bml", 99982);

  public static CommonVars<Integer> REMOTE_METADATA_SERVICE_ERROR =
      CommonVars.apply("wds.linkis.server.dsm.error-code.metadata", 99983);

  public static CommonVars<Integer> PARAM_VALIDATE_ERROR =
      CommonVars.apply("wds.linkis.server.dsm.error-code.param-validate", 99986);

  public static CommonVars<Integer> DATASOURCE_NOTFOUND_ERROR =
      CommonVars.apply("wds.linkis.server.dsm.error-code.datasource-not-found", 99988);
}
