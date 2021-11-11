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
 
package org.apache.linkis.bml.common

import org.apache.linkis.common.exception.ErrorException

case class BmlAuthorityException() extends ErrorException(60036, "Store cannot be accessed without login or expired login(未登录或登录过期，无法访问物料库)")

case class UploadResourceException() extends ErrorException(60050, "The first upload of the resource failed(首次上传资源失败)")

case class UpdateResourceException() extends ErrorException(60051, "Failed to update resources(更新资源失败)")

