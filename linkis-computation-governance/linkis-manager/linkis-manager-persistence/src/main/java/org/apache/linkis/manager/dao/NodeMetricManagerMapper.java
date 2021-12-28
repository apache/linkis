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

import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeMetrics;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeMetricsEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;


public interface NodeMetricManagerMapper {

    @Insert("insert into  linkis_cg_manager_service_instance_metrics (instance, instance_status, overload, heartbeat_msg,healthy_status,update_time,create_time)" +
            "values(#{nodeMetrics.instance},#{nodeMetrics.status},#{nodeMetrics.overLoad},#{nodeMetrics.heartBeatMsg},#{nodeMetrics.healthy},now(),now())")
    void addNodeMetrics(@Param("nodeMetrics") PersistenceNodeMetrics nodeMetrics);

    @Select("select count(id) from  linkis_cg_manager_service_instance_metrics met inner join linkis_cg_manager_service_instance ins" +
            " on met.instance = #{instance} and ins.instance = #{instance} and met.instance = ins.instance")
    int checkInstanceExist(@Param("instance") String  instance);


    @Select("<script>" +
            "select * from linkis_cg_manager_service_instance_metrics where instance in("
            +"<foreach collection='instances' separator=',' item='instance'>"
            + "#{instance}"
            + "</foreach>"
            +")</script>")
    @Results({
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "instance", column = "instance"),
            @Result(property = "heartBeatMsg", column = "heartbeat_msg"),
            @Result(property = "status", column = "instance_status"),
            @Result(property = "healthy", column = "healthy_status")
    })
    List<PersistenceNodeMetrics> getNodeMetricsByInstances(@Param("instances") List<String > instances);

    @Select("select * from linkis_cg_manager_service_instance_metrics where instance = #{instance}")
    @Results({
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "instance", column = "instance"),
            @Result(property = "heartBeatMsg", column = "heartbeat_msg"),
            @Result(property = "status", column = "instance_status"),
            @Result(property = "healthy", column = "healthy_status")
    })
    PersistenceNodeMetrics getNodeMetricsByInstance(@Param("instance") String instance);

    @Update({"<script> update linkis_cg_manager_service_instance_metrics" +
            "<trim prefix=\"set\" suffixOverrides=\",\">" +
            "<if test=\"nodeMetrics.status != null\">instance_status = #{nodeMetrics.status},</if>" +
            "<if test=\"nodeMetrics.overLoad != null\"> overload = #{nodeMetrics.overLoad},</if>" +
            "<if test=\"nodeMetrics.heartBeatMsg != null\">  heartbeat_msg = #{nodeMetrics.heartBeatMsg},</if>" +
            "<if test=\"nodeMetrics.healthy != null\">  healthy_status=#{nodeMetrics.healthy}, </if>" +
            "<if test=\"nodeMetrics.updateTime != null\">  update_time=#{nodeMetrics.updateTime},</if>" +
            "</trim> where instance = #{instance}" +
            "</script>"})
    void updateNodeMetrics(@Param("nodeMetrics") PersistenceNodeMetrics nodeMetrics, @Param("instance") String instance);

    @Delete("delete from linkis_cg_manager_service_instance_metrics where instance in (select instance from linkis_cg_manager_service_instance where instance=#{instance})")
    void deleteNodeMetrics(@Param("instance") String instance);

    @Delete("delete from linkis_cg_manager_service_instance_metrics where instance = #{instance}")
    void deleteNodeMetricsByInstance(@Param("instance") String instance);

    @Select("select A.name,B.* from linkis_cg_manager_service_instance A join linkis_cg_manager_service_instance_metrics B where A.instance =  B.instance")
    @Results({
            @Result(property = "instance", column = "instance"),
            @Result(property = "heartBeatMsg", column = "heartbeat_msg"),
            @Result(property = "status", column = "instance_status"),
            @Result(property = "healthy", column = "healthy_status"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time")
    })
    @ResultType(PersistenceNodeMetricsEntity.class)
    List<PersistenceNodeMetricsEntity>  getAllNodeMetrics();
}
