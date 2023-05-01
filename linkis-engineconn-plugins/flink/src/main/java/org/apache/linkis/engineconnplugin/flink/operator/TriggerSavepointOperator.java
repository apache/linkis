package org.apache.linkis.engineconnplugin.flink.operator;

import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.engineconn.once.executor.creation.OnceExecutorManager;
import org.apache.linkis.engineconnplugin.flink.client.deployment.ClusterDescriptorAdapter;
import org.apache.linkis.engineconnplugin.flink.errorcode.FlinkErrorCodeSummary;
import org.apache.linkis.engineconnplugin.flink.exception.JobExecutionException;
import org.apache.linkis.engineconnplugin.flink.executor.FlinkOnceExecutor;
import org.apache.linkis.manager.common.operator.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class TriggerSavepointOperator implements Operator {

    private static final Logger logger = LoggerFactory.getLogger(TriggerSavepointOperator.class);

    @Override
    public String[] getNames() {
        return new String[]{"doSavepoint"};
    }

    @Override
    public Map<String, Object> apply(Map<String, Object> parameters) {
        String savepoint = getAsThrow(parameters, "savepointPath");
        String mode = getAsThrow(parameters, "mode");
        logger.info("try to " + mode + " savepoint with path " + savepoint + ".");

        if (OnceExecutorManager.getInstance().getReportExecutor() instanceof FlinkOnceExecutor) {
            FlinkOnceExecutor flinkExecutor = (FlinkOnceExecutor) OnceExecutorManager.getInstance().getReportExecutor();
            ClusterDescriptorAdapter clusterDescriptorAdapter = (ClusterDescriptorAdapter) flinkExecutor.getClusterDescriptorAdapter();
            String writtenSavepoint = "";
            try {
                writtenSavepoint = clusterDescriptorAdapter.doSavepoint(savepoint, mode);
            } catch (JobExecutionException e) {
                logger.info("doSavepoint failed", e);
                throw new RuntimeException(e);
            }

            Map<String, Object> stringMap = new HashMap<>();
            stringMap.put("writtenSavepoint", writtenSavepoint);
            return stringMap;
        } else {
            throw new WarnException(FlinkErrorCodeSummary.NOT_SUPPORT_SAVEPOTION.getErrorCode(),MessageFormat.format(FlinkErrorCodeSummary.NOT_SUPPORT_SAVEPOTION.getErrorDesc(),
                    OnceExecutorManager.getInstance().getReportExecutor().getClass().getSimpleName()));
        }
    }

    public  <T> T getAsThrow(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value != null) {
            try {
                return (T) value;
            } catch (Exception e) {
                throw new IllegalArgumentException("parameter " + key + " is invalid.", e);
            }
        } else {
            throw new IllegalArgumentException("parameter " + key + " is required.");
        }
    }
}