package com.webank.wedatasphere.linkis.cs.client.service;

import com.webank.wedatasphere.linkis.common.io.MetaData;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

import java.util.Map;

/**
 * @Author alexyang
 * @Date 2020/3/9
 */
public interface MetaDataService {

    /**
     * 通过ContextID和NodeName，获取上游的所有Metadata数据
     * @param contextIDStr
     * @param nodeName
     * @return
     * @throws CSErrorException
     */
    Map<ContextKey, MetaData> getAllUpstreamMetaData(String contextIDStr, String nodeName) throws CSErrorException;

}
