/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.bml.dao;

import com.webank.wedatasphere.linkis.bml.Entity.ResourceVersion;
import com.webank.wedatasphere.linkis.bml.Entity.Version;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * created by cooperyang on 2019/5/14
 * Description:
 */
public interface VersionDao {

    Version getVersion(@Param("resourceId") String resourceId, @Param("version") String version);

//    List<Version> getVersions(@Param("resourceId") String resourceId, @Param("versions") List<String> versions);

    List<Version> getVersions(@Param("resourceId") String resourceId);


    List<ResourceVersion> getResourcesVersions(Map paramMap);

    void deleteVersion(@Param("resourceId") String resourceId, @Param("version") String version);

    void deleteVersions(@Param("resourceId") String resourceId);

    void bathDeleteVersions(@Param("resourceIds") List<String> resourceIds, @Param("versions") List<String> versions);

    long insertNewVersion(ResourceVersion resourceVersion);

    String getResourcePath(@Param("resourceId") String resourceId);


    String getNewestVersion(@Param("resourceId") String resourceId);


    @Select("select start_byte from linkis_resources_version where resource_id = #{resourceId} and version = #{version}")
    long getStartByteForResource(@Param("resourceId") String resourceId, @Param("version")String version);



    @Select("select end_byte from linkis_resources_version where resource_id = #{resourceId} and version = #{version}")
    long getEndByte(@Param("resourceId") String resourceId, @Param("version") String version);

    @Select("select * from linkis_resources_version where resource_id=#{resourceId} and version = #{version} and enable_flag = 1")
    ResourceVersion findResourceVersion(@Param("resourceId") String resourceId,
                                        @Param("version") String version);

    List<ResourceVersion> getAllResourcesViaSystem(@Param("system") String system, @Param("user") String user);
    List<ResourceVersion> selectResourcesViaSystemByPage(@Param("system") String system, @Param("user") String user);


    @Select("select exists(select * from `linkis_resources_version` where resource_id=#{resourceId} and version=#{version} and enable_flag = 1)")
    int checkVersion(@Param("resourceId") String resourceId, @Param("version") String version);


    @Select("select enable_flag from `linkis_resources_version` where resource_id = #{resourceId} and version = #{version}")
    int selectResourceVersionEnbleFlag(@Param("resourceId") String resourceId, @Param("version")String version);

    /**
     * 将resourceId对应的所有版本的enable_flag设为0，这样就不能继续访问该资源的任意版本
     * @param resourceId resourceId
     */
    @Update("update `linkis_resources_version` set enable_flag = 0 where resource_id = #{resourceId}")
    void deleteResource(@Param("resourceId")String resourceId);


    void batchDeleteResources(@Param("resourceIds") List<String> resourceIds);


    ResourceVersion getResourceVersion(@Param("resourceId") String resourceId, @Param("version") String version);



    List<Version> selectVersionByPage(@Param("resourceId")String resourceId);

}
