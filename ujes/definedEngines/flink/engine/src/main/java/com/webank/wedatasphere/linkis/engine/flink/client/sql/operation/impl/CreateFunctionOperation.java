package com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.impl;

import com.webank.wedatasphere.linkis.engine.flink.client.config.Environment;
import com.webank.wedatasphere.linkis.engine.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.context.SessionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.NonJobOperation;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.OperationUtil;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlExecutionException;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.client.config.entries.FunctionEntry;
import org.apache.flink.table.functions.UserDefinedFunction;

/**
 * @author gongzhiyao
 * @date 2021-01-29 23:27
 * @Description:
 */
public class CreateFunctionOperation implements NonJobOperation {
    private final ExecutionContext<?> context;
    private final String              functionName;
    private       String              functionClass;

    public CreateFunctionOperation(
            SessionContext context,
            String functionName,
            String functionClass) {
        this.context = context.getExecutionContext();
        this.functionName  = functionName;
        this.functionClass = functionClass;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public String getFunctionClass() {
        return this.functionClass;
    }

    @Override
    public ResultSet execute() {
        Environment   env        = context.getEnvironment();
        FunctionEntry functionEntry = env.getFunctions().get(functionName);
        if (functionEntry instanceof FunctionEntry) {
            throw new SqlExecutionException("Cannot create function '" + functionName + "'  because a function with this name is already registered.");
        }
        TableEnvironment tableEnv = context.getTableEnvironment();
        try {
            context.wrapClassLoader(() -> {
                try {
                    tableEnv.createFunction(functionName,context.getClassLoader().loadClass(functionClass).asSubclass(UserDefinedFunction.class));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (Throwable t) {
            // catch everything such that the query does not crash the executor
            throw new SqlExecutionException("Invalid SQL statement.", t);
        }
        return OperationUtil.OK;
    }
}
