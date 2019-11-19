package com.webank.wedatasphere.linkis.bml.dao;

import com.webank.wedatasphere.linkis.bml.Entity.Resource;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * created by cooperyang on 2019/5/14
 * Description:
 */
public interface ResourceDao {

    List<Resource> getResources(Map paramMap);

    void deleteResource(@Param("resourceId") String resourceId);

    void batchDeleteResources(@Param("resourceIds") List<String> resourceIds);

    long uploadResource(Resource resource);


    @Select("select exists(select * from `linkis_resources` where resource_id = #{resourceId}  and enable_flag = 1 )")
    int checkExists(@Param("resourceId") String resourceId);

    Resource getResource(@Param("resourceId") String resourceId);

    @Select("select owner from `linkis_resources` where resource_id = #{resourceId} ")
    String getUserByResourceId(@Param("resourceId") String resourceId);
}
