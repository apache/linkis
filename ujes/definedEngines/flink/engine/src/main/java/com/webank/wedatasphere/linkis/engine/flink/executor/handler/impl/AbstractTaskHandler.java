package com.webank.wedatasphere.linkis.engine.flink.executor.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext;
import com.webank.wedatasphere.linkis.engine.flink.client.config.entries.ExecutionEntry;
import com.webank.wedatasphere.linkis.engine.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.SessionManager;
import com.webank.wedatasphere.linkis.engine.flink.client.utils.SqlCommandParser;
import com.webank.wedatasphere.linkis.engine.flink.conf.FlinkConfiguration;
import com.webank.wedatasphere.linkis.engine.flink.exception.IllegalArgumentException;
import com.webank.wedatasphere.linkis.engine.flink.executor.FlinkResultListener;
import com.webank.wedatasphere.linkis.engine.flink.executor.handler.TaskHandler;
import com.webank.wedatasphere.linkis.engine.flink.util.ConfigurationParseUtils;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @program: flink-parent
 * @description:
 * @author: hui zhu
 * @create: 2020-12-18 14:50
 */
public abstract class AbstractTaskHandler implements TaskHandler {

     private Logger LOG = LoggerFactory.getLogger(getClass());

    public static final List<String> AVAILABLE_PLANNERS = Arrays.asList(
            ExecutionEntry.EXECUTION_PLANNER_VALUE_OLD,
            ExecutionEntry.EXECUTION_PLANNER_VALUE_BLINK);

    public static final List<String> AVAILABLE_EXECUTION_TYPES = Arrays.asList(
            ExecutionEntry.EXECUTION_TYPE_VALUE_BATCH,
            ExecutionEntry.EXECUTION_TYPE_VALUE_STREAMING);


    protected EngineExecutorContext engineExecutorContext;

    protected ExecutionContext executionContext;

    protected SessionManager sessionManager;

    protected String jobID;

    protected Long taskID;

    protected String applicationId;

    protected String webUrl;

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public Long getTaskID() {
        return taskID;
    }

    public void setTaskID(Long taskID) {
        this.taskID = taskID;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

}
