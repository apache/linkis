package com.webank.wedatasphere.linkis.cs.client.service;

import com.webank.wedatasphere.linkis.cs.common.entity.resource.BMLResource;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

import java.util.List;
import java.util.Map;

/**
 * @Author alexyang
 * @Date 2020/3/9
 */
public interface ResourceService {

    /**
     * 通过ContextID和NodeName，获取上游的所有Resource数据
     * @param contextIDStr
     * @param nodeName
     * @return
     */
    Map<ContextKey, BMLResource> getAllUpstreamBMLResource(String contextIDStr, String nodeName) throws CSErrorException;

    List<BMLResource> getUpstreamBMLResource(String contextIDStr, String nodeName) throws CSErrorException;
}
