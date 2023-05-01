package org.apache.linkis.engineconn.acessible.executor.operator.impl;

import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.engineconn.common.exception.EngineConnException;
import org.apache.linkis.engineconn.core.executor.ExecutorManager;
import org.apache.linkis.engineconn.executor.entity.Executor;
import org.apache.linkis.engineconn.executor.entity.YarnExecutor;
import org.apache.linkis.manager.common.operator.Operator;

import java.util.HashMap;
import java.util.Map;

public class EngineConnApplicationInfoOperator implements Operator {

    public static final String OPERATOR_NAME = "engineConnYarnApplication";

    @Override
    public String[] getNames() {
        return new String[]{OPERATOR_NAME};
    }

    @Override
    public Map<String, Object> apply(Map<String, Object> parameters) {
        Executor reportExecutor = ExecutorManager.getInstance().getReportExecutor();
        if ( reportExecutor instanceof YarnExecutor ) {
            YarnExecutor yarnExecutor = (YarnExecutor) reportExecutor;
            Map<String, Object> result = new HashMap<>();
            result.put("applicationId", yarnExecutor.getApplicationId());
            result.put("applicationUrl", yarnExecutor.getApplicationURL());
            result.put("queue", yarnExecutor.getQueue());
            result.put("yarnMode", yarnExecutor.getYarnMode());
            return result;
        } else {
            throw new WarnException(20301, "EngineConn is not a yarn application, cannot fetch applicaiton info.");
        }
    }
}