package com.webank.wedatasphere.linkis.manager.common.entity.node;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus;
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;

/**
 * @author peacewong
 * @date 2020/6/7 18:17
 */
public interface Node extends RequestProtocol {


    ServiceInstance getServiceInstance();

    void setServiceInstance(ServiceInstance serviceInstance);


    NodeStatus getNodeStatus();

    void setNodeStatus(NodeStatus status);

    String getOwner();

    String getMark();


}
