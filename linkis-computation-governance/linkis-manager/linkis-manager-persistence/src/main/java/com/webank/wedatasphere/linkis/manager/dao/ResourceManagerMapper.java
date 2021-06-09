package com.webank.wedatasphere.linkis.manager.dao;

import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLabel;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceResource;
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
            "locked_resource=#{persistenceResource.lockedResource},resourceType=#{persistenceResource.resourceType}," +
            "update_time=#{persistenceResource.updateTime},create_time=#{persistenceResource.createTime} where ticketId = #{ticketId}"})
    void nodeResourceUpdate(@Param("ticketId") String ticketId,@Param("persistenceResource") PersistenceResource persistenceResource);

    @Update({"update linkis_cg_manager_linkis_resources set max_resource = #{persistenceResource.maxResource},min_resource = #{persistenceResource.minResource}," +
            "used_resource=#{persistenceResource.usedResource},left_resource=#{persistenceResource.leftResource},expected_resource=#{persistenceResource.expectedResource}," +
            "locked_resource=#{persistenceResource.lockedResource},resourceType=#{persistenceResource.resourceType},ticketId=#{persistenceResource.ticketId}," +
            "update_time=#{persistenceResource.updateTime},create_time=#{persistenceResource.createTime} where id = #{resourceId}"})
    void nodeResourceUpdateByResourceId(@Param("resourceId") int resourceId,@Param("persistenceResource") PersistenceResource persistenceResource);

    @Select("select id from linkis_cg_manager_linkis_resources where ticketId is null and id in ( select resource_id from linkis_cg_manager_label_resource A join linkis_cg_manager_label_service_instance B on A.label_id=B.label_id and B.service_instance=#{instance})")
    int getNodeResourceUpdateResourceId(@Param("instance") String  instance);

//    @Select("select * from linkis_cg_manager_linkis_resources where  id  in  \n" +
//            "(select resource_id from linkis_cg_manager_label_resource A join linkis_cg_manager_label_service_instance B on A.label_id=B.label_id  and B.service_instance_id=#{serviceInstanceId})")
//    @Results({
//            @Result(property = "maxResource", column = "max_resource"),
//            @Result(property = "minResource", column = "min_resource"),
//            @Result(property = "usedResource", column = "used_resource"),
//            @Result(property = "leftResource", column = "left_resource"),
//            @Result(property = "expectedResource", column = "expected_resource"),
//            @Result(property = "lockedResource", column = "locked_resource"),
//            @Result(property = "updateTime", column = "update_time"),
//            @Result(property = "createTime", column = "create_time"),
//
//    })
//    List<PersistenceResource> getResourceByServiceInstanceId(@Param("serviceInstanceId") int serviceInstanceId);
//
//    @Select("select * from linkis_cg_manager_linkis_resources where id = #{id}")
//    PersistenceResource getResourceById(@Param("id") int id);
//
//    @Delete("delete from linkis_cg_manager_linkis_resources where id = #{id}")
//    void deleteResourceById(@Param("id") int id);

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
}
