package com.webank.wedatasphere.linkis.manager.dao;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.label.LabelKeyValue;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLabel;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceNode;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface LabelManagerMapper {

    @Options(useGeneratedKeys = true, keyProperty = "persistenceLabel.id", keyColumn = "id")
    @Insert("INSERT INTO  linkis_cg_manager_label (label_key,label_value,label_feature,label_value_size,update_time,create_time) VALUES" +
            "(#{persistenceLabel.labelKey},#{persistenceLabel.stringValue},#{persistenceLabel.feature},#{persistenceLabel.labelValueSize},now(), now())")
    void registerLabel(@Param("persistenceLabel") PersistenceLabel persistenceLabel);

    @Insert({"<script>" +
            "INSERT INTO linkis_cg_manager_label_value_relation (label_value_key,label_value_content,label_id,update_time,create_time) VALUES"
            + "<foreach collection='labelValueKeyAndContent.entrySet()' separator=',' index='valueKey' item='valueContent'>"
            + "(#{valueKey}, #{valueContent},#{labelId},now(),now())"
            + "</foreach>"
            + "</script>"})
    void registerLabelKeyValues(@Param("labelValueKeyAndContent") Map<String, String> labelValueKeyAndContent, @Param("labelId") int labelId);

    @Select("select * from linkis_cg_manager_label where id=#{id}")
    @Results({
            @Result(property = "labelKey", column = "label_key"),
            @Result(property = "feature", column = "label_feature"),
            @Result(property = "labelValueSize", column = "label_value_size"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "stringValue", column = "label_value")
    })
    PersistenceLabel getLabel(@Param("id") int id);

    @Delete("delete from linkis_cg_manager_label  where id =#{id}")
    void deleteLabel(@Param("id") int id);

    @Delete("delete from linkis_cg_manager_label  where label_key =#{labelKey} and label_value=#{labelStringValue} ")
    void deleteByLabel(@Param("labelKey") String labelKey, @Param("labelStringValue") String labelStringValue);

    @Delete("delete from linkis_cg_manager_label_value_relation where label_id =#{id}")
    void deleteLabelKeyVaules(@Param("id") int id);

    @Update("update linkis_cg_manager_label set label_key = #{persistenceLabel.labelKey},label_value = #{persistenceLabel.stringValue}," +
            "label_feature=#{persistenceLabel.feature},label_value_size=#{persistenceLabel.labelValueSize},update_time=#{persistenceLabel.updateTime} where id=#{id}")
    void updateLabel(@Param("id") int id, @Param("persistenceLabel") PersistenceLabel persistenceLabel);


    @Insert({
            "<script>" +
                    "insert into linkis_cg_manager_label_service_instance(label_id, service_instance, update_time,create_time) values " +
                    "<foreach collection='labelIds' item='labelId' index='index' separator=','>" +
                    "(#{labelId}, #{instance},now(),now())" +
                    "</foreach>" +
                    "</script>"
    })
    void addLabelServiceInstance(@Param("instance") String instance, @Param("labelIds") List<Integer> labelIds);

    @Select("<script> select b.* from " +
            "( select label_id, count(1) as label_value_size from linkis_cg_manager_label_value_relation where (label_value_key,label_value_content) IN"
            + "<foreach collection='labelKeyValues.entrySet()' separator=',' index='key' item='value' open='(' close=')'>"
            + "(#{key},#{value})"
            + "</foreach> group by label_id) as a join linkis_cg_manager_label as b on a.label_id = b.label_id  and a.label_value_size = b.label_value_size"
            + "</script>")
    @Results({
            @Result(property = "labelKey", column = "label_key"),
            @Result(property = "feature", column = "label_feature"),
            @Result(property = "labelValueSize", column = "label_value_size"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "stringValue", column = "label_value")
    })
    List<PersistenceLabel> getLabelsByLabelKeyValues(@Param("labelKeyValues") Map<String, String> labelKeyValues);


    @Select("<script> select b.* from " +
            "( select label_id,count(1) as label_value_size from linkis_cg_manager_label_value_relation where (label_value_key,label_value_content) IN"
            + "<foreach collection='labelKeyValues.entrySet()' separator=',' index='key' item='value' open='(' close=')'>"
            + "(#{key},#{value})"
            + "</foreach> group by label_id ) as a join linkis_cg_manager_label as b on a.label_id = b.label_id and a.label_value_size = b.label_value_size and b.label_key=#{labelKey} "
            + "</script>")
    @Results({
            @Result(property = "labelKey", column = "label_key"),
            @Result(property = "feature", column = "label_feature"),
            @Result(property = "labelValueSize", column = "label_value_size"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "stringValue", column = "label_value")
    })
    PersistenceLabel getLabelByLabelKeyValues(@Param("labelKey") String labelKey, @Param("labelKeyValues") Map<String, String> labelKeyValues);

    @Select("select * from linkis_cg_manager_label where id in (select label_id from linkis_cg_manager_label_service_instance where service_instance=#{instance})")
    @Results({
            @Result(property = "labelKey", column = "label_key"),
            @Result(property = "feature", column = "label_feature"),
            @Result(property = "labelValueSize", column = "label_value_size"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "stringValue", column = "label_value")
    })
    List<PersistenceLabel> getLabelByServiceInstance(@Param("instance") String instance);

    @Select("<script>" +
            "select distinct label_id from linkis_cg_manager_label_value_relation where (label_value_key,label_value_content) IN"
            + "<foreach collection='labelKeyValues.entrySet()' separator=',' index='key' item='value' open='(' close=')'>"
            + "(#{key},#{value})"
            + "</foreach>"
            + "</script>")
    List<Integer> getLabelByLabelKeyValuesOverload(@Param("labelKeyValues") Map<String, String> labelKeyValues);


    @Select("select * from linkis_cg_manager_label where id in (select label_id from linkis_cg_manager_label_resource A join linkis_cg_manager_linkis_resources B on  A.resource_id = B.id  where A.resource_id = #{persistenceResource.id} )")
    @Results({
            @Result(property = "labelKey", column = "label_key"),
            @Result(property = "feature", column = "label_feature"),
            @Result(property = "labelValueSize", column = "label_value_size"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "stringValue", column = "label_value")
    })
    List<PersistenceLabel> getLabelByResource(@Param("persistenceResource") PersistenceResource persistenceResource);

//    @Insert("insert into linkis_cg_manager_label_resource (label_id, resource_id, update_time, create_time)" +
//            "values(#{labelId}, #{resourceId}, now(), now())")
//   void addLabelResource(@Param("labelId")int labelId,@Param("resourceId")int resourceId);

    @Insert({
            "<script>",
            "insert into linkis_cg_manager_label_resource(label_id, resource_id,update_time, create_time) values ",
            "<foreach collection='labelIds' item='labelId' index='index' separator=','>",
            "(#{labelId},#{resourceId},now(), now())",
            "</foreach>",
            "</script>"
    })
    void addLabelsAndResource(@Param("resourceId") int resourceId, @Param("labelIds") List<Integer> labelIds);

//    @Insert({
//            "<script>",
//            "insert into linkis_cg_manager_label_resource(label_id, resource_id,update_time, create_time) values ",
//            "<foreach collection='resourceIds' item='resourceId' index='index' separator=','>",
//            "(#{labelId},#{resourceId},now(), now())",
//            "</foreach>",
//            "</script>"
//    })
//    void addLabelAndResources(@Param("labelId")int labelId,@Param("resourceIds")List<Integer> resourceIds);

//    @Select("select id from linkis_cg_manager_label where creator = #{creator}")
//    List<Integer> getLabelIds (@Param("creator") String  creator);
//
//    @Select("select resource_id from linkis_cg_manager_label_resource where label_id = #{labelId}")
//    List<Integer> getResourceIdsByLabelId (@Param("labelId") int  labelId);
//
//    @Select("<script>" +
//            "select * from linkis_cg_manager_label_resource where id in("
//            +"<foreach collection='labelIds' separator=',' item='labelId'>"
//            + "#{labelId} "
//            + "</foreach> "
//            +")</script>")
//    List<PersistenceResource> getResourcesByLabels (@Param("labelIds") List<Integer>  labelIds);

    @Select("select * from linkis_cg_manager_linkis_resources A join linkis_cg_manager_label_resource B  on A.id = B.resource_id " +
            "and B.label_id in (select id from linkis_cg_manager_label where label_key=#{labelKey} and label_value=#{stringValue})")
    @Results({
            @Result(property = "maxResource", column = "max_resource"),
            @Result(property = "minResource", column = "min_resource"),
            @Result(property = "usedResource", column = "used_resource"),
            @Result(property = "leftResource", column = "left_resource"),
            @Result(property = "expectedResource", column = "expected_resource"),
            @Result(property = "lockedResource", column = "locked_resource"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time")
    })
    List<PersistenceResource> getResourcesByLabel(@Param("labelKey") String labelKey, @Param("stringValue") String stringValue);

    @Select("select label_id from linkis_cg_manager_label_service_instance where service_instance = #{instance}")
    List<Integer> getLabelIdsByInstance(@Param("instance") String instance);

    @Select("select * from linkis_cg_manager_label A join linkis_cg_manager_label_service_instance B on A.id = B.label_id where B.service_instance = #{instance}")
    List<PersistenceLabel> getLabelsByInstance(@Param("instance") String instance);

    @Select("select * from linkis_cg_manager_service_instance A join linkis_cg_manager_label_service_instance B on A.instance = B.service_instance where B.label_id=#{labelId}")
    @Results({
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time")
    })
    List<PersistenceNode> getInstanceByLabelId(@Param("labelId") int labelId);


    @Select("<script>" +
            "select service_instance from linkis_cg_manager_label_service_instance where label_id in ("
            + "<foreach collection='labelIds' separator=',' item='labelId'>"
            + "#{labelId} "
            + "</foreach> "
            + ")</script>")
    List<String> getInstanceIdsByLabelIds(@Param("labelIds") List<Integer> labelIds);

    @Select("<script>" +
            "select * from linkis_cg_manager_label where id in ("
            + "<foreach collection='labelIds' separator=',' item='labelId'>"
            + "#{labelId} "
            + "</foreach> "
            + ")</script>")
    @Results({
            @Result(property = "labelKey", column = "label_key"),
            @Result(property = "feature", column = "label_feature"),
            @Result(property = "labelValueSize", column = "label_value_size"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "stringValue", column = "label_value")
    })
    List<PersistenceLabel> getLabelsByLabelIds(@Param("labelIds") List<Integer> labelIds);

    @Insert({
            "<script>",
            "insert into linkis_cg_manager_label_user(username, label_id,update_time, create_time) values ",
            "<foreach collection='labelIds' item='labelId' index='index' separator=','>",
            "(#{userName},#{labelId},now(), now())",
            "</foreach>",
            "</script>"
    })
    void addLabelsByUser(@Param("userName") String userName, @Param("labelIds") List<Integer> labelIds);

    @Select("select username from linkis_cg_manager_label_user where label_id = #{labelId}")
    List<String> getUserNameByLabelId(@Param("labelId") int labelId);

    @Select("<script>" +
            "select distinct username from linkis_cg_manager_label_user where label_id in ("
            + "<foreach collection='labelIds' separator=',' item='labelId'>"
            + "#{labelId} "
            + "</foreach> "
            + ")</script>")
    List<String> getUserNamesByLabelIds(@Param("labelIds") List<Integer> labelIds);

    @Select("select * from linkis_cg_manager_label A join linkis_cg_manager_label_user B on A.id = B.label_id and B.username = #{userName}")
    @Results({
            @Result(property = "labelKey", column = "label_key"),
            @Result(property = "feature", column = "label_feature"),
            @Result(property = "labelValueSize", column = "label_value_size"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "stringValue", column = "label_value")
    })
    List<PersistenceLabel> getLabelsByUser(@Param("userName") String userName);

    @Select("select * from linkis_cg_manager_label where label_key = #{labelKey}")
    @Results({
            @Result(property = "labelKey", column = "label_key"),
            @Result(property = "feature", column = "label_feature"),
            @Result(property = "labelValueSize", column = "label_value_size"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "stringValue", column = "label_value")
    })
    List<PersistenceLabel> getLabelsByLabelKey(@Param("labelKey") String labelKey);

    @Delete("delete from linkis_cg_manager_label_resource A join linkis_cg_manager_linkis_resources B on A.resource_id = B.id and B.ticketId=#{ticketId}")
    void deleteLabelResourceByByTicketId(@Param("ticketId") String ticketId);


    @Delete({"<script>" +
            "delete from linkis_cg_manager_label_service_instance where service_instance = #{instance} and label_id in "
            + "<foreach collection='labelIds' item='labelId' index='index'  open='(' separator=',' close=')'>"
            + "#{labelId}"
            + "</foreach>"
            + "</script>"
    })
    void deleteLabelIdsAndInstance(@Param("instance") String instance, @Param("labelIds") List<Integer> labelIds);


    @Delete({"<script>" +
            "delete from linkis_cg_manager_label_user where username = #{userName} and label_id in "
            + "<foreach collection='labelIds' item='labelId' index='index'  open='(' separator=',' close=')'>"
            + "#{labelId}"
            + "</foreach>"
            + "</script>"
    })
    void deleteLabelIdsByUser(@Param("userName") String userName, @Param("labelIds") List<Integer> labelIds);

    @Delete("delete from linkis_cg_manager_label_user where label_id=#{labelId}")
    void deleteUserById(int labelId);

    /**
     * 通过lavel value 同时返回instance信息和label信息
     *
     * @param labelKeyAndValuesMap
     * @return
     */
    List<Map<String, Object>> dimListNodeRelationsByKeyValueMap(@Param("keyValueMap") Map<String, Map<String, String>> labelKeyAndValuesMap, @Param("valueRelation") String name);

    /**
     * 通过instance信息，同时返回instance信息和label信息
     *
     * @param serviceInstances
     * @return
     */
    List<Map<String, Object>> listLabelRelationByServiceInstance(@Param("nodes") List<ServiceInstance> serviceInstances);

    /**
     * 通过labelkey 和StringValue找到唯一的label
     *
     * @param labelKey
     * @param stringValue
     * @return
     */
    PersistenceLabel getLabelByKeyValue(@Param("labelKey") String labelKey, @Param("stringValue") String stringValue);

    List<ServiceInstance> getNodeByLabelKeyValue(@Param("labelKey") String labelKey, @Param("stringValue") String stringValue);

    /**
     * @param values,可能是多个label的散列 all 和 and 类型的 查询
     * @return
     */
    List<PersistenceLabel> listResourceLabelByValues(@Param("values") List<LabelKeyValue> values);

    /**
     * 通过labelId获取到resource
     *
     * @param labelId
     * @return
     */
    List<PersistenceResource> listResourceByLaBelId(Integer labelId);

    /**
     * 通过label的keyvalues再找到resource
     * 和{@link LabelManagerMapper#dimListLabelByKeyValueMap(Map, String)} 区别只是多了关联和资源表,并且返回是resource
     *
     * @param keyValueMap
     * @return
     */
    List<PersistenceResource> dimListResourceBykeyValueMap(@Param("keyValueMap") Map<String, Map<String, String>> keyValueMap, @Param("valueRelation") String name);

    /**
     * 通过labelId删除资源，还有资源和label的关联，并不会删除label表记录
     *
     * @param labelId
     */
    void deleteResourceByLabelId(Integer labelId);
    void deleteResourceByLabelIdInDirect(Integer labelId);

    /**
     * 同 {@link LabelManagerMapper#deleteResourceByLabelId(Integer)}
     * 和下面的batchdelete有点重复，主要是sql给的是= batch 的话给的是in
     *
     * @param singletonMap
     */
    Integer selectLabelIdByLabelKeyValuesMaps(@Param("labelKeyValues") Map<String, Map<String, String>> singletonMap);
    void deleteResourceByLabelKeyValuesMaps(Integer id);
    void deleteResourceByLabelKeyValuesMapsInDirect(Integer id);

    /**
     * 批量删除 不删除label
     *
     * @param notBlankIds
     */
    void batchDeleteResourceByLabelId(@Param("labelIds") List<Integer> notBlankIds);
    void batchDeleteResourceByLabelIdInDirect(@Param("labelIds") List<Integer> notBlankIds);

    /**
     * 批量删除 不删除label
     *
     * @param keyValueMaps
     */
    void batchDeleteResourceByLabelKeyValuesMaps(@Param("labelKeyValues") Map<String, Map<String, String>> keyValueMaps);

    /**
     * ALL,AND搜索，根据key，value找到label集合，不对instance和resource进行关联，效率稍比{@link LabelManagerMapper#dimListLabelByKeyValueMap(Map, String)}
     *
     * @param keyValueMap
     * @return
     */
    @Deprecated
    List<PersistenceLabel> listLabelByKeyValueMap(@Param("keyValueMap") Map<String, Map<String, String>> keyValueMap);

    /**
     * 模糊搜索，根据value，valueRelation 找到label集合，不对instance和resource进行关联
     *
     * @param valueList
     * @return
     */
    List<PersistenceLabel> dimListLabelByValueList(@Param("valueList") List<Map<String, String>> valueList, @Param("valueRelation") String valueRelation);

    /**
     * 模糊搜索，根据key，value，valueRelation 找到label集合，不对instance和resource进行关联
     *
     * @param keyValueMap
     * @param name
     * @return
     */
    List<PersistenceLabel> dimListLabelByKeyValueMap(@Param("keyValueMap") Map<String, Map<String, String>> keyValueMap, @Param("valueRelation") String name);

    /**
     * 和{@link LabelManagerMapper#dimListLabelByKeyValueMap(Map, String)} 区别只是多了关联和资源表
     *
     * @param labelKeyAndValuesMap
     * @param name
     * @return
     */
    List<PersistenceLabel> dimlistResourceLabelByKeyValueMap(@Param("keyValueMap") Map<String, Map<String, String>> labelKeyAndValuesMap, @Param("valueRelation") String name);
}
