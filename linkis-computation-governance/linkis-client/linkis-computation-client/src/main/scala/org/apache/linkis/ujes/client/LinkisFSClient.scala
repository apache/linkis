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

package org.apache.linkis.ujes.client

import org.apache.linkis.ujes.client.request.{
  CreateNewDirAction,
  IsPathExistAction,
  UploadFileAction
}
import org.apache.linkis.ujes.client.response.{
  CreateNewDirResult,
  IsPathExistResult,
  UploadFileResult
}

class LinkisFSClient(client: UJESClient) {

  def isPathExist(isPathExistAction: IsPathExistAction): Boolean = {
    val result = client.executeUJESJob(isPathExistAction).asInstanceOf[IsPathExistResult]
    result.isExist
  }

  def createNewDir(makeDirAction: CreateNewDirAction): CreateNewDirResult = {
    client.executeUJESJob(makeDirAction).asInstanceOf[CreateNewDirResult]
  }

  def upload(uploadFileAction: UploadFileAction): UploadFileResult = {
    client.executeUJESJob(uploadFileAction).asInstanceOf[UploadFileResult]
  }

}
