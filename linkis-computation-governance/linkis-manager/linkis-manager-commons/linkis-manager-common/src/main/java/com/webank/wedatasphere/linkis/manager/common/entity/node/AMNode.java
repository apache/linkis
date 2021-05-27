package com.webank.wedatasphere.linkis.manager.common.entity.node;

import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeHealthyInfo;
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeOverLoadInfo;
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeTaskInfo;

import java.util.Date;

/**
 * @author peacewong
 * @date 2020/6/10 16:17
 */
public interface AMNode extends Node {

    NodeTaskInfo getNodeTaskInfo();

    void setNodeTaskInfo(NodeTaskInfo nodeTaskInfo);

    void setNodeOverLoadInfo(NodeOverLoadInfo nodeOverLoadInfo);

    NodeOverLoadInfo getNodeOverLoadInfo();

    NodeHealthyInfo getNodeHealthyInfo();

    void setNodeHealthyInfo(NodeHealthyInfo nodeHealthyInfo);

    Date getStartTime();

    void setStartTime(Date startTime);


}
