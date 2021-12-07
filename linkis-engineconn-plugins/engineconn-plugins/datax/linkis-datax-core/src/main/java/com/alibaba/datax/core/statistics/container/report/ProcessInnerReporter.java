package com.alibaba.datax.core.statistics.container.report;

import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;
import com.alibaba.datax.core.statistics.communication.LocalTGCommunicationManager;
import com.alibaba.datax.core.util.HttpClientUtil;
import com.alibaba.datax.core.util.container.CoreConstant;
import com.webank.wedatasphere.linkis.datax.common.GsonUtil;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ProcessInnerReporter extends AbstractReporter {

    private static final String REPORT_RESP_DATA_PATH = "data";

    private static final Logger LOG = LoggerFactory.getLogger(ProcessInnerReporter.class);

    private Configuration configuration;

    public ProcessInnerReporter(Configuration configuration){
        this.configuration = configuration;
    }

    /**
     * Updated by davidhua@webank.com
     * @param jobId
     * @param communication
     */
    @Override
    @SuppressWarnings("unchecked")
    public void reportJobCommunication(Long jobId, Communication communication) {
        try {
            /*Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("currentByteSpeed", communication.getLongCounter(CommunicationTool.BYTE_SPEED));
            requestBody.put("taskId", configuration.getLong(CoreConstant.DATAX_CORE_CONTAINER_JOB_ID));
            StringEntity entity = new StringEntity(GsonUtil.toJson(requestBody));
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            HttpPost post = HttpClientUtil.getPostRequest(configuration.getString(CoreConstant.DATAX_CORE_DATAXSERVER_PROTOCOL)
                            + "://" + configuration.getString(CoreConstant.DATAX_CORE_DATAXSERVER_ADDRESS)
                            + configuration.getString(CoreConstant.DATAX_CORE_DATAXSERVER_ENDPOINT_REPORT_STATE),
                    entity,
                    "Content-Type", "application/json;charset=UTF-8");
            String body = HttpClientUtil.getHttpClientUtil().executeAndGet(post, String.class);
            Map<String, Object> response = GsonUtil.fromJson(body, Map.class, String.class, Object.class);
            if(response.get(REPORT_RESP_DATA_PATH) != null) {
                Map runtimeParams = (Map) response.get(REPORT_RESP_DATA_PATH);
                Long maxByteSpeed = new BigDecimal(String.valueOf(runtimeParams
                        .getOrDefault("maxByteSpeed", 0))).longValue();
                if (maxByteSpeed > 0) {
                    //Update the speed configuration
                    this.configuration.set(CoreConstant.DATAX_JOB_SETTING_SPEED_BYTE, maxByteSpeed);
                }
            }*/
        }catch(Exception e){
            LOG.info(e.getMessage(), e);
            //Do nothing
        }
    }

    @Override
    public void reportTGCommunication(Integer taskGroupId, Communication communication) {
        LocalTGCommunicationManager.updateTaskGroupCommunication(taskGroupId, communication);
    }

}