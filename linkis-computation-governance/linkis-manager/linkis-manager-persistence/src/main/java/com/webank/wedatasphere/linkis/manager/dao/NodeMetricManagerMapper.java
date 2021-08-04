package com.webank.wedatasphere.linkis.manager.dao;

import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceNodeMetrics;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceNodeMetricsEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;


public interface NodeMetricManagerMapper {

    @Insert("insert into  linkis_cg_manager_service_instance_metrics (instance, instance_status, overload, heartbeat_msg,healthy_status,update_time,create_time)" +
            "values(#{nodeMetrics.instance},#{nodeMetrics.status},#{nodeMetrics.overLoad},#{nodeMetrics.heartBeatMsg},#{nodeMetrics.healthy},now(),now())")
    void addNodeMetrics(@Param("nodeMetrics") PersistenceNodeMetrics nodeMetrics);

    @Select("select count(instance) from  linkis_cg_manager_service_instance_metrics where instance = #{instance}")
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
            "<if test=\"nodeMetrics.createTime != null\">   create_time=#{nodeMetrics.createTime},</if>" +
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
