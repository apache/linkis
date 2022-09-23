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

package org.apache.linkis.bml.common

import org.apache.linkis.bml.client.errorcode.LinkisBmlClientErrorCodeSummary._
import org.apache.linkis.common.exception.ErrorException

case class POSTActionFailException()
    extends ErrorException(MATERIAL_HOUSE.getErrorCode, MATERIAL_HOUSE.getErrorDesc) {}

case class POSTResultNotMatchException()
    extends ErrorException(RETURNED_BY_THE.getErrorCode, RETURNED_BY_THE.getErrorDesc)

case class IllegalPathException()
    extends ErrorException(CATALOG_PASSED.getErrorCode, CATALOG_PASSED.getErrorDesc)

case class BmlResponseErrorException(errorMessage: String)
    extends ErrorException(BML_ERROR_EXCEP.getErrorCode, errorMessage)

case class GetResultNotMatchException()
    extends ErrorException(RETURNED_CLIENT_MATCH.getErrorCode, RETURNED_CLIENT_MATCH.getErrorDesc)

case class BmlClientFailException(errorMsg: String)
    extends ErrorException(AN_ERROR_OCCURRED.getErrorCode, AN_ERROR_OCCURRED.getErrorDesc)
