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
 
package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.Entity.BmlProject;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface BmlProjectDao {


    @Insert("insert ignore into linkis_ps_bml_project(name, `system`, source, description, creator, enabled, create_time) " +
            "values(#{bmlProject.name}, #{bmlProject.system}, #{bmlProject.source}, #{bmlProject.description}, " +
            "#{bmlProject.creator}, #{bmlProject.enabled}, #{bmlProject.createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "bmlProject.id", keyColumn = "id")
    void createNewProject(@Param("bmlProject") BmlProject bmlProject);

    @Select("select * from linkis_ps_bml_project where name = #{projectName}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "createTime", column = "create_time")
    })
    BmlProject getBmlProject(@Param("projectName") String projectName);


    @Insert({
            "<script>",
            "insert ignore into  linkis_ps_bml_project_user(project_id, username, priv, creator, create_time)",
            "values",
            "<foreach collection='usernames' item='username' open='(' separator='),(' close=')'>",
            "#{projectId}, #{username}, #{priv}, #{creator}, #{createTime}",
            "</foreach>",
            "</script>"
    })
    void setProjectPriv(@Param("projectId") Integer projectId, @Param("usernames") List<String> usernames,
                        @Param("priv") int priv, @Param("creator") String creator, @Param("createTime") Date createTime);

    @Select("select a.priv from linkis_ps_bml_project_user a join linkis_ps_bml_project b on " +
            "a.project_id = b.id and b.name = #{projectName} and a.username = #{username}")
    Integer getPrivInProject(@Param("projectName") String projectName, @Param("username") String username);


    @Insert("insert ignore into linkis_ps_bml_project_resource(project_id, resource_id) " +
            "values(#{projectId}, #{resourceId})")
    //@Options(useGeneratedKeys = true, keyProperty = "bmlProject.id", keyColumn = "id")
    void addProjectResource(@Param("projectId") Integer id, @Param("resourceId") String resourceId);

    @Select("select a.name from linkis_ps_bml_project a join " +
            " linkis_ps_bml_project_resource b on b.resource_id = #{resourceId} and a.id = b.project_id")
    String getProjectNameByResourceId(@Param("resourceId") String resourceId);

    @Select("select id from linkis_ps_bml_project where name = #{projectName}")
    Integer getProjectIdByName(@Param("projectName") String projectName);

    @Insert("insert ignore into linkis_ps_bml_project_resource(project_id, resource_id) " +
            "values(#{projectId}, #{resourceId})")
    void attachResourceAndProject(@Param("projectId") Integer projectId,
                                  @Param("resourceId") String resourceId);

    @Select("select count(*) from linkis_ps_bml_project_resource where project_id = #{resourceId} and resource_id = #{resourceId}")
    Integer checkIfExists(@Param("projectId") Integer projectId, @Param("resourceId") String resourceId);

    @Delete("delete from linkis_ps_bml_project_user where project_id = #{projectId}")
    void deleteAllPriv(@Param("projectId") Integer projectId);
}
