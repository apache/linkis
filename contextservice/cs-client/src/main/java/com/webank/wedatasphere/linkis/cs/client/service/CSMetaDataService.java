package com.webank.wedatasphere.linkis.cs.client.service;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.common.io.MetaData;
import com.webank.wedatasphere.linkis.cs.client.utils.SerializeHelper;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.exception.ErrorCode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @Author alexyang
 * @Date 2020/3/9
 */
public class CSMetaDataService implements MetaDataService {

    private static final Logger logger = LoggerFactory.getLogger(CSMetaDataService.class);

    private static CSMetaDataService csMetaDataService;

    private CSMetaDataService() {

    }

    public static CSMetaDataService getInstance() {
        if (null == csMetaDataService) {
            synchronized (CSMetaDataService.class) {
                if (null == csMetaDataService) {
                    csMetaDataService = new CSMetaDataService();
                }
            }
        }
        return csMetaDataService;
    }

    @Override
    public Map<ContextKey, MetaData> getAllUpstreamMetaData(String contextIDStr, String nodeName) throws CSErrorException {
        if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
            return null;
        }
        try {
            ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
            return DefaultSearchService.getInstance().searchUpstreamContextMap(contextID, nodeName, Integer.MAX_VALUE, MetaData.class);
        } catch (ErrorException e) {
            logger.error("Deserialize contextid error. contextID : " + contextIDStr + ", e ", e);
            throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "Deserialize contextid error. contextID : " + contextIDStr + ", e " + e.getDesc());
        }
    }
}
