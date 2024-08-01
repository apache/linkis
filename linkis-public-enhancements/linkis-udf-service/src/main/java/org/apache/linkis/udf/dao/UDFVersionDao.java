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

package org.apache.linkis.udf.dao;

import org.apache.linkis.udf.entity.UDFVersion;
import org.apache.linkis.udf.vo.UDFVersionVo;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UDFVersionDao {
  void addUdfVersion(UDFVersion udfVersion);

  UDFVersion selectLatestByUdfId(@Param("udfId") long udfId);

  UDFVersion selectByUdfIdAndVersion(@Param("udfId") long udfId, @Param("version") String version);

  void updatePublishStatus(
      @Param("udfId") long udfId,
      @Param("version") String version,
      @Param("isPublished") boolean isPublished);

  List<UDFVersionVo> getAllVersionByUdfId(@Param("udfId") long udfId);

  List<UDFVersion> getAllVersions(@Param("udfId") long udfId);

  void deleteVersionByUdfId(@Param("udfId") long udfId);

  int getSameJarCount(@Param("userName") String userName, @Param("jarName") String jarName);

  int getOtherSameJarCount(
      @Param("userName") String userName,
      @Param("jarName") String jarName,
      @Param("udfId") long udfId);

  void updateResourceIdByUdfId(
      @Param("udfId") long udfId,
      @Param("resourceId") String resourceId,
      @Param("oldUser") String oldUser,
      @Param("newUser") String newUser);

  void updateUDFVersion(UDFVersion udfVersion);

  UDFVersionVo getUdfVersionInfoByName(
      @Param("udfName") String udfName, @Param("createUser") String createUser);
}
