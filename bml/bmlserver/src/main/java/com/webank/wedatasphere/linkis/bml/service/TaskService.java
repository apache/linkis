package com.webank.wedatasphere.linkis.bml.service;

import com.webank.wedatasphere.linkis.bml.Entity.ResourceTask;

import org.apache.ibatis.annotations.Param;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author v_wblwyan
 * @date 2019-9-16
 */
public interface TaskService {

  ResourceTask createUploadTask(FormDataMultiPart form, String user, Map<String, Object> properties)
      throws Exception;

  ResourceTask createUpdateTask(String resourceId, String user, FormDataMultiPart formDataMultiPart, Map<String, Object> properties)
      throws Exception;

  ResourceTask createDownloadTask(String resourceId, String version, String user, String clientIp);

  /**
   * 更新任务状态
   * @param taskId 任务ID
   * @param state 执行状态
   * @param updateTime 操作时间
   */
  void updateState(long taskId, String state, Date updateTime);

  /**
   * 更新任务状态为失败
   * @param taskId 任务ID
   * @param state 执行状态
   * @param updateTime 操作时间
   * @param errMsg 异常信息
   */
  void updateState2Failed( long taskId, String state, Date updateTime, String errMsg);

  ResourceTask createDeleteVersionTask(String resourceId, String version, String user, String ip);

  ResourceTask createDeleteResourceTask(String resourceId, String user, String ip);

  ResourceTask createDeleteResourcesTask(List<String> resourceIds, String user, String ip);
}
