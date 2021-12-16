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
 
package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface ResourceManagerMapper {
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    //@SelectKey(statement = "select last_insert_id() AS id", keyProperty = "id", before = false, resultType = int.class)
    @Insert("INSERT INTO  linkis_cg_manager_linkis_resources VALUES" +
            "(#{id},#{maxResource},#{minResource},#{usedResource},#{leftResource},#{expectedResource},#{lockedResource},#{resourceType},#{ticketId},now(),now(),#{updator},#{creator})")
    void registerResource(PersistenceResource persistenceResource);

    @Update({ "update linkis_cg_manager_linkis_resources set max_resource = #{persistenceResource.maxResource},min_resource = #{persistenceResource.minResource}, " +
            "used_resource=#{persistenceResource.usedResource},left_resource=#{persistenceResource.leftResource},expected_resource=#{persistenceResource.expectedResource}," +
            "locked_resource=#{persistenceResource.lockedResource}," +
            "update_time=#{persistenceResource.updateTime} where ticketId = #{ticketId}"})
    void nodeResourceUpdate(@Param("ticketId") String ticketId,@Param("persistenceResource") PersistenceResource persistenceResource);

    @Update({"update linkis_cg_manager_linkis_resources set max_resource = #{persistenceResource.maxResource},min_resource = #{persistenceResource.minResource}," +
            "used_resource=#{persistenceResource.usedResource},left_resource=#{persistenceResource.leftResource},expected_resource=#{persistenceResource.expectedResource}," +
            "locked_resource=#{persistenceResource.lockedResource}," +
            "update_time=#{persistenceResource.updateTime} where id = #{resourceId}"})
    void nodeResourceUpdateByResourceId(@Param("resourceId") int resourceId,@Param("persistenceResource") PersistenceResource persistenceResource);

    @Select("select id from linkis_cg_manager_linkis_resources where ticketId is null and id in ( select resource_id from linkis_cg_manager_label_resource A join linkis_cg_manager_label_service_instance B on A.label_id=B.label_id and B.service_instance=#{instance})")
    int getNodeResourceUpdateResourceId(@Param("instance") String  instance);


    @Delete("delete from linkis_cg_manager_label_resource where label_id in (select label_id from linkis_cg_manager_label_service_instance where service_instance=#{instance})")
    void deleteResourceAndLabelId(@Param("instance") String  instance);

    @Delete("delete from linkis_cg_manager_linkis_resources where id in " +
            "(select resource_id from linkis_cg_manager_label_resource A join linkis_cg_manager_label_service_instance B on A.label_id=B.label_id and B.service_instance = #{instance} )")
    void deleteResourceByInstance(@Param("instance") String  instance);

    @Delete("delete from linkis_cg_manager_linkis_resources where ticketId = #{ticketId}")
    void deleteResourceByTicketId(@Param("ticketId") String ticketId);

//    @Select("select * from linkis_cg_manager_linkis_resources where id = #{id} adn resourceType = #{resourceType}")
//    PersistenceResource getResourceByIdAndType(@Param("id") int id,@Param("resourceType") String resourceType);

    @Select("select * from linkis_cg_manager_linkis_resources where resourceType = #{resourceType} and" +
            " id in (select resource_id from linkis_cg_manager_label_resource A join linkis_cg_manager_label_service_instance B on A.label_id = B.label_id and B.service_instance=#{instance})")
    List<PersistenceResource> getResourceByInstanceAndResourceType(@Param("instance") String   instance,@Param("resourceType") String   resourceType);

    @Select("select * from linkis_cg_manager_linkis_resources where id in " +
            "(select resource_id from linkis_cg_manager_label_resource A join linkis_cg_manager_label_service_instance B on A.label_id = B.label_id and B.service_instance= #{instance})")
    List<PersistenceResource> getResourceByServiceInstance(@Param("instance") String   instance);

    @Select("select * from linkis_cg_manager_linkis_resources where ticketId = #{ticketId}")
    PersistenceResource getNodeResourceByTicketId(@Param("ticketId") String ticketId);

    @Select("select * from linkis_cg_manager_linkis_resources where id in (select resource_id from linkis_cg_manager_label_resource A join linkis_manager_lable_user B on A.label_id=B.label_id AND B.user_name=#{userName})")
    List<PersistenceResource> getResourceByUserName(@Param("userName") String  userName);

    @Select("select * from linkis_cg_manager_label where id in (select label_id from linkis_cg_manager_label_resource A join linkis_cg_manager_linkis_resources B on A.resource_id=B.id and B.ticketId=#{ticketId})")
    List<PersistenceLabel> getLabelsByTicketId(@Param("ticketId") String ticketId);

    void deleteResourceById(@Param("ids") List<Integer> ids);

    void deleteResourceRelByResourceId(@Param("ids") List<Integer> ids);

    PersistenceResource getResourceById(@Param("id") Integer id);
}
