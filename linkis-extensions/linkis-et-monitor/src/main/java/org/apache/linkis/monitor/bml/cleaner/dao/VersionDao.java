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

package org.apache.linkis.monitor.bml.cleaner.dao;

import org.apache.linkis.monitor.bml.cleaner.entity.CleanedResourceVersion;
import org.apache.linkis.monitor.bml.cleaner.entity.ResourceVersion;
import org.apache.linkis.monitor.bml.cleaner.vo.CleanResourceVo;

import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

public interface VersionDao {

  @Select(
      "select resource_id, count(resource_id) as version_count, max(version) as max_version from "
          + "linkis_ps_bml_resources_version lpbrv where start_time < #{startTime} GROUP BY resource_id HAVING count(resource_id) > #{maxVersionNum} limit #{limitNum}")
  List<CleanResourceVo> getAllNeedCleanResource(
      @Param("maxVersionNum") Integer maxVersionNum,
      @Param("startTime") Date startTime,
      @Param("limitNum") int num);

  @Select(
      "select * from linkis_ps_bml_resources_version where resource_id = #{resourceId} and version < #{minKeepVersion} and version <> 'v000001'")
  List<ResourceVersion> getCleanVersionsByResourceId(
      @Param("resourceId") String resourceId, @Param("minKeepVersion") String minKeepVersion);

  @Insert({
    "insert into linkis_ps_bml_cleaned_resources_version(`resource_id`,`file_md5`,`version`,`size`,`start_byte`, `end_byte`,`resource`,`description`,"
        + "`start_time`,`end_time`,`client_ip`,`updator`,`enable_flag`,`old_resource`) values(#{resourceId},#{fileMd5},#{version},#{size},#{startByte},#{endByte}"
        + ",#{resource},#{description},#{startTime},#{endTime},#{clientIp},#{updator},#{enableFlag},#{oldResource})"
  })
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insertCleanResourceVersion(CleanedResourceVersion cleanedResourceVersion);

  @Delete("delete from linkis_ps_bml_resources_version where id=#{id}")
  void deleteResourceVersionById(@Param("id") long id);

  @Select(
      "select version from linkis_ps_bml_resources_version where resource_id =#{resourceId} and version <= #{maxVersion} order by version desc limit #{keepNum},1")
  String getMinKeepVersion(
      @Param("resourceId") String resourceId,
      @Param("maxVersion") String maxVersion,
      @Param("keepNum") int keepNum);
}
