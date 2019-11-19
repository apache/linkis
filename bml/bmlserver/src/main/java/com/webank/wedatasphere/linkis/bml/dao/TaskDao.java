package com.webank.wedatasphere.linkis.bml.dao;

import com.webank.wedatasphere.linkis.bml.Entity.ResourceTask;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * @author v_wblwyan
 * @date 2019-9-16
 */
public interface TaskDao {

  /**
   * 插入任务信息
   * @param resourceTask 任务信息
   * @return 主键ID
   */
    long insert(ResourceTask resourceTask);

  /**
   * 更新任务状态
   * @param taskId 任务ID
   * @param state 执行状态
   * @param updateTime 操作时间
   */
    void updateState(@Param("taskId") long taskId, @Param("state") String state, @Param("updateTime") Date updateTime);

  /**
   * 更新任务状态为失败
   * @param taskId 任务ID
   * @param state 执行状态
   * @param updateTime 操作时间
   * @param errMsg 异常信息
   */
    void updateState2Failed(@Param("taskId") long taskId, @Param("state") String state, @Param("updateTime") Date updateTime, @Param("errMsg") String errMsg);

  /**
   * 获取资源最大版本号
   * @param resourceId 资源ID
   * @return 资源最大版本号
   */
    String getNewestVersion(@Param("resourceId") String resourceId);
}
