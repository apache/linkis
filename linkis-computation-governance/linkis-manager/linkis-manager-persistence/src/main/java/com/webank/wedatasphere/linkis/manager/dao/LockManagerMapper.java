package com.webank.wedatasphere.linkis.manager.dao;

import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLock;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LockManagerMapper {
    @Insert("insert into linkis_cg_manager_lock (lock_object, time_out, update_time, create_time)" +
            "values(#{jsonObject}, #{timeOut}, now(), now())")
    void lock(@Param("jsonObject") String jsonObject, @Param("timeOut") Long timeOut);

    @Delete("delete  from linkis_cg_manager_lock where lock_object = #{jsonObject}")
    void unlock(String jsonObject);

    @Select("select * from  linkis_cg_manager_lock")
    @Results({
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "createTime", column = "create_time")
    })
    List<PersistenceLock> getAll();

}
